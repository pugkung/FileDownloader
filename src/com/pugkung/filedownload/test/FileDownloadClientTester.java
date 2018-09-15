package com.pugkung.filedownload.test;

import com.pugkung.filedownload.main.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest(FileDownloadClient.class)
public class FileDownloadClientTester {
	
	@InjectMocks
	FileDownloadClient fdc;
	URLDownloader fd;
	
	@Test
	public void TestFileDownloaderClient_downloadSingleFile() throws Exception {
		String testURL = "http://localhost/file.out";
		String testOutputPath = "./";
		
		fdc = spy(new FileDownloadClient());
		ConfigReader config = mock(ConfigReader.class);
		File mockFile = mock(File.class);
		
		List<String> urlList = new ArrayList<String>();
		urlList.add(testURL);
		
		fd = spy(new URLDownloader(urlList.get(0), testOutputPath));
		File expectedFile = new File(testOutputPath + fd.generateOutputFileName(testURL));
		expectedFile.deleteOnExit();
		
		PowerMockito.whenNew(FileDownloadClient.class).withAnyArguments().thenReturn(fdc);
		PowerMockito.whenNew(URLDownloader.class).withAnyArguments().thenReturn(fd);
		PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(mockFile);
		doReturn(config).when(fdc).loadConfigFile(any(String.class));
		when(config.getOutputPath()).thenReturn(testOutputPath);
		when(config.getURLs()).thenReturn(urlList);
		
		doAnswer(new Answer(){
			public File answer(InvocationOnMock invocation) throws Throwable {
				expectedFile.createNewFile();
				return expectedFile;
			}}).when(fd).downloadFromURL(any(URL.class), eq(expectedFile));
		
		FileDownloadClient.main(new String[] {});
		
		verify(fdc, times(1)).distributeURLsToDownloaderThread(anyList(), any(String.class));
		verify(fdc, times(1)).executeDownloaderThread(any(String.class), any(String.class));
		assertTrue(expectedFile.exists());
	}
	
	@Test
	public void TestFileDownloaderClient_downloadMultipleSources() throws Exception {
		String testURL1 = "http://localhost/file.out";
		String testURL2 = "ftp://some.src/download.test";
		String testURL3 = "https://dl.somehost.net/some_file.txt";
		String testURL4 = "http://10.192.11.35/www/testfile.out";
		String testOutputPath = "./";
		
		fdc = spy(new FileDownloadClient());
		ConfigReader config = mock(ConfigReader.class);
		File mockFile = mock(File.class);
		
		List<String> urlList = new ArrayList<String>();
		urlList.add(testURL1);
		urlList.add(testURL2);
		urlList.add(testURL3);
		urlList.add(testURL4);
		
		fd = spy(new URLDownloader(urlList.get(0), testOutputPath));
		List<File> expectedFiles = new ArrayList<File>();
		
		for (String item : urlList) {
			File expectedFile = new File(testOutputPath + fd.generateOutputFileName(item));
			expectedFile.deleteOnExit();
			expectedFiles.add(expectedFile);
		}
		
		PowerMockito.whenNew(FileDownloadClient.class).withAnyArguments().thenReturn(fdc);
		PowerMockito.whenNew(URLDownloader.class).withAnyArguments().thenAnswer(
				new Answer<Object>() {
					public URLDownloader answer(InvocationOnMock invocation) throws Throwable {
						// spy with different input url on each executed URLDownloader threads
						String targetURL = (String) invocation.getArguments()[0];
						fd = spy(new URLDownloader(targetURL, testOutputPath));
						
						doAnswer(new Answer(){
							public File answer(InvocationOnMock invocation) throws Throwable {
								// return file with filename based on the incoming url
								Object[] arguments = invocation.getArguments();
								File expectedFile = (File) arguments[1];
								expectedFile.createNewFile();
								return expectedFile;
							}}).when(fd).downloadFromURL(any(URL.class), any(File.class));
						
						return fd;
					}
				});
		
		PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(mockFile);
		doReturn(config).when(fdc).loadConfigFile(any(String.class));
		when(config.getOutputPath()).thenReturn(testOutputPath);
		when(config.getURLs()).thenReturn(urlList);
		
		FileDownloadClient.main(new String[] {});
		
		verify(fdc, times(1)).distributeURLsToDownloaderThread(anyList(), any(String.class));
		verify(fdc, times(4)).executeDownloaderThread(any(String.class), any(String.class));
		for (File item : expectedFiles) {
			assertTrue(item.exists());
		}
	}
	
	@Test
	public void TestFileDownloaderClient_downloadMixedFailSources() throws Exception {
		String testURL1 = "http://localhost/file.out";
		String testURL2 = "ftp://dl.somehost/mock.file";
		String testURLFail1 = "invalid_host/file";
		String testURLFail2 = "http://test/unreachable.host";
		String testOutputPath = "./";
		
		fdc = spy(new FileDownloadClient());
		ConfigReader config = mock(ConfigReader.class);
		File mockFile = mock(File.class);
		
		List<String> urlList = new ArrayList<String>();
		urlList.add(testURL1);
		urlList.add(testURL2);
		urlList.add(testURLFail1);
		urlList.add(testURLFail2);
		
		fd = spy(new URLDownloader(urlList.get(0), testOutputPath));
		List<File> expectedFiles = new ArrayList<File>();
		
		for (String item : urlList) {
			File expectedFile = new File(testOutputPath + fd.generateOutputFileName(item));
			expectedFile.deleteOnExit();
			expectedFiles.add(expectedFile);
		}
		
		PowerMockito.whenNew(FileDownloadClient.class).withAnyArguments().thenReturn(fdc);
		PowerMockito.whenNew(URLDownloader.class).withAnyArguments().thenAnswer(
				new Answer<Object>() {
					public URLDownloader answer(InvocationOnMock invocation) throws Throwable {
						// spy url for each executed URLDownloader threads
						String targetURL = (String) invocation.getArguments()[0];
						fd = spy(new URLDownloader(targetURL, testOutputPath));
						
						// simulate testURLFail2 to not response any file
						if (targetURL.equals(testURLFail2)) {
							return fd;
						}
						
						doAnswer(new Answer(){
							public File answer(InvocationOnMock invocation) throws Throwable {
								// return file with filename based on the incoming url
								Object[] arguments = invocation.getArguments();
								File expectedFile = (File) arguments[1];
								expectedFile.createNewFile();
								return expectedFile;
							}}).when(fd).downloadFromURL(any(URL.class), any(File.class));
						
						return fd;
					}
				});
		
		PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(mockFile);
		doReturn(config).when(fdc).loadConfigFile(any(String.class));
		when(config.getOutputPath()).thenReturn(testOutputPath);
		when(config.getURLs()).thenReturn(urlList);
		
		FileDownloadClient.main(new String[] {});
		
		verify(fdc, times(1)).distributeURLsToDownloaderThread(anyList(), any(String.class));
		verify(fdc, times(4)).executeDownloaderThread(any(String.class), any(String.class));
		
		// verify that only 2 of 4 file has been successfully downloaded
		int outputFileCount = 0;
		for (File item : expectedFiles) {
			if(item.exists()) {
				outputFileCount++;
			}
		}
		assertEquals(outputFileCount, 2);
	}
	
	@Test
	public void TestFileDownloaderClient_nonExistingURL() throws Exception {
		String testURL = "http://257.257.257.257/should/not/exist";
		String testOutputPath = "./";
		
		fdc = spy(new FileDownloadClient());
		ConfigReader config = mock(ConfigReader.class);
		File mockFile = mock(File.class);
		
		List<String> urlList = new ArrayList<String>();
		urlList.add(testURL);
		
		fd = spy(new URLDownloader(urlList.get(0), testOutputPath));
		File expectedFile = new File(testOutputPath + fd.generateOutputFileName(testURL));
		expectedFile.deleteOnExit();
		
		PowerMockito.whenNew(FileDownloadClient.class).withAnyArguments().thenReturn(fdc);
		PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(mockFile);
		doReturn(config).when(fdc).loadConfigFile(any(String.class));
		when(config.getOutputPath()).thenReturn(testOutputPath);
		when(config.getURLs()).thenReturn(urlList);
		
		FileDownloadClient.main(new String[] {});
		
		verify(fdc, times(1)).distributeURLsToDownloaderThread(anyList(), any(String.class));
		verify(fdc, times(1)).executeDownloaderThread(any(String.class), any(String.class));
		assertFalse(expectedFile.exists());
	}
	
	@Test
	public void TestFileDownloaderClient_invalidURLAsInput() throws Exception {
		String testURL = "not_an_url";
		String testOutputPath = "./";
		
		fdc = spy(new FileDownloadClient());
		ConfigReader config = mock(ConfigReader.class);
		File mockFile = mock(File.class);
		
		List<String> urlList = new ArrayList<String>();
		urlList.add(testURL);
		
		fd = spy(new URLDownloader(urlList.get(0), testOutputPath));
		File expectedFile = new File(testOutputPath + fd.generateOutputFileName(testURL));
		expectedFile.deleteOnExit();
		
		PowerMockito.whenNew(FileDownloadClient.class).withAnyArguments().thenReturn(fdc);
		PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(mockFile);
		doReturn(config).when(fdc).loadConfigFile(any(String.class));
		when(config.getOutputPath()).thenReturn(testOutputPath);
		when(config.getURLs()).thenReturn(urlList);
		
		FileDownloadClient.main(new String[] {});
		
		verify(fdc, times(1)).distributeURLsToDownloaderThread(anyList(), any(String.class));
		verify(fdc, times(1)).executeDownloaderThread(any(String.class), any(String.class));
		assertFalse(expectedFile.exists());
	}
	
	@Test
	public void TestFileDownloaderClient_noSourceURLAsInput() throws Exception {
		fdc = spy(new FileDownloadClient());
		ConfigReader config = mock(ConfigReader.class);
		File mockFile = mock(File.class);
		mockFile.deleteOnExit();
		
		List<String> urlList = new ArrayList<String>();
		
		PowerMockito.whenNew(FileDownloadClient.class).withAnyArguments().thenReturn(fdc);
		PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(mockFile);
		doReturn(config).when(fdc).loadConfigFile(any(String.class));
		when(config.getOutputPath()).thenReturn("");
		when(config.getURLs()).thenReturn(urlList);
		
		FileDownloadClient.main(new String[] {});
		
		verify(fdc, times(0)).distributeURLsToDownloaderThread(anyList(), any(String.class));
		verify(fdc, times(0)).executeDownloaderThread(any(String.class), any(String.class));
		assertEquals(FileDownloadClient.ExitStatus.NO_URL_PROVIDED, fdc.getExitStatusCode());
	}
	
	@Test
	public void TestFileDownloaderClient_invalidConfigFile() {
		FileDownloadClient client = new FileDownloadClient();
		String configFile = "missing.config";
		
		FileDownloadClient.main(new String[] {configFile});
		assertEquals(FileDownloadClient.ExitStatus.MISSING_CONFIGURATION, client.getExitStatusCode());
	}
}
