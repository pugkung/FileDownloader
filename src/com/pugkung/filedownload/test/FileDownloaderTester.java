package com.pugkung.filedownload.test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.pugkung.filedownload.main.FileDownloader.DownloaderStatus;
import com.pugkung.filedownload.main.URLDownloader;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({URL.class, URLConnection.class, FileUtils.class})
public class FileDownloaderTester {
	@Rule
	public TemporaryFolder tempFolder= new TemporaryFolder();
	
	@InjectMocks
	URLDownloader fd;
	
	@Test
	public void TestFileDownloader_stripHostNamefromLink() throws URISyntaxException {
		String testURL = "http://sub.test.com/some/path";
		String testURL2 = "ftp://10.123.234.254:23/public/file";
		String testURL3 = "https://www.hello-world.com/temp";
		
		URLDownloader fd = new URLDownloader("", "");
		assertEquals("sub_test_com", fd.getDomainNameFromURL(testURL));
		assertEquals("10_123_234_254", fd.getDomainNameFromURL(testURL2));
		assertEquals("hello-world_com", fd.getDomainNameFromURL(testURL3));
	}
	
	@Test
	public void TestFileDownloader_stripFilePathfromLink() throws URISyntaxException {
		String testURL = "http://sub.test.com/some/path/afile.txt";
		String testURL2 = "ftp://10.123.234.254:23/public/";
		
		URLDownloader fd = new URLDownloader("", "");
		assertEquals("_some_path_afile.txt", fd.getFilePathFromURL(testURL));
		assertEquals("_public_", fd.getFilePathFromURL(testURL2));
	}
	
	@Test(expected = URISyntaxException.class)
	public void TestFileDownloader_tryParseInvalidURL() throws URISyntaxException {
		String testURL = "http://sub.test.com/some/path/f i l e w i t h s p a c e";
		
		URLDownloader fd = new URLDownloader(testURL, "");
		assertEquals("", fd.getDomainNameFromURL(testURL));
	}
	
	@Test(expected = URISyntaxException.class)
	public void TestFileDownloader_tryParseInvalidHost() throws URISyntaxException {
		String testURL = "ftp://257.255.253.251/folder/";
		
		URLDownloader fd = new URLDownloader(testURL, "");
		assertEquals("", fd.getDomainNameFromURL(testURL));
	}
	
	@Test
	public void TestFileDownloader_testRunner() throws Exception {
		String testURL = "http://localhost/testfile.out";
		String outputPath = "";
		
		fd = spy(new URLDownloader(testURL, outputPath));
		
		File result = new File(outputPath + fd.generateOutputFileName(testURL));
		result.deleteOnExit();
		
		doNothing().when(fd).downloadFromURL(any(URL.class), eq(result));
		fd.run();
		
		verify(fd, times(1)).downloadFromURL(any(URL.class), eq(result));
		assertEquals(fd.getResultCd(),DownloaderStatus.COMPLETE);
	}
	
	@Test
	public void TestFileDownloader_testDownloadSuccessful() throws Exception {
		String testURL = "http://localhost/testfile.out";
		String outputPath = "";
		
		fd = spy(new URLDownloader(testURL, outputPath));
		
		File result = new File(outputPath + fd.generateOutputFileName(testURL));
		result.deleteOnExit();
		String outputFile = outputPath + fd.generateOutputFileName(testURL);
		
		doAnswer(new Answer(){
			public File answer(InvocationOnMock invocation) throws Throwable {
				result.createNewFile();
				return result;
			}}).when(fd).downloadFromURL(any(URL.class), eq(result));
		
		DownloaderStatus status = fd.downloadFile(testURL, outputFile);
		
		assertTrue(result.exists());
		assertEquals(status, DownloaderStatus.COMPLETE);
	}
	
	@Test
	public void TestFileDownloader_testDownloadFail() throws IOException {
		String testURL = "https://localhost/file/not/exist";
		String outputPath = "";
		
		fd = spy(new URLDownloader(testURL, outputPath));
		File result = mock(File.class);
		result.deleteOnExit();
		
	    doThrow(IOException.class).when(fd).downloadFromURL(any(URL.class), any(File.class));
		
		DownloaderStatus status = fd.downloadFile(testURL, outputPath);
		
		assertEquals(status,DownloaderStatus.IO_ERROR);
		assertFalse(result.exists());
	}
	
	@Test
	public void TestFileDownloader_connectionDropped() throws Exception {
		String testURL = "http://localhost/testfile.out";
		String outputPath = "";
		
		fd = spy(new URLDownloader(testURL, outputPath));
		
		String outputFile = outputPath + fd.generateOutputFileName(testURL);
		File result = new File(outputFile);
		result.deleteOnExit();
		
		URL mockURL = PowerMockito.mock(URL.class);
		HttpURLConnection mockConnection = PowerMockito.mock(HttpURLConnection.class);
		SocketTimeoutException expectedException = new SocketTimeoutException();
		
		PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
		PowerMockito.when(mockURL.openConnection()).thenReturn(mockConnection);
		PowerMockito.when(mockConnection.getResponseCode()).thenThrow(expectedException);
		
		DownloaderStatus status = fd.downloadFile(testURL, outputFile);
		
		verify(fd, times(1)).downloadFromURL(any(URL.class), eq(result));
		assertEquals(status,DownloaderStatus.IO_ERROR);
		assertFalse(result.exists());
	}
	
	@Test
	public void TestFileDownloader_testLargeFile() throws Exception {
		String testURL = "http://localhost/testfile.out";
		String outputPath = "";
		
		fd = spy(new URLDownloader(testURL, outputPath));
		
		String outputFile = outputPath + fd.generateOutputFileName(testURL);
		File result = new File(outputFile);
		//File result = PowerMockito.mock(File.class);
		result.deleteOnExit();
		
		URL mockURL = PowerMockito.mock(URL.class);
		URLConnection mockConnection = PowerMockito.mock(URLConnection.class);
		
		PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
		PowerMockito.when(mockURL.openConnection()).thenReturn(mockConnection);
		
		// Generate very large file as data stream
		RandomAccessFile f = new RandomAccessFile("t", "rw");
        f.setLength(1024 * 1024 * 1024);
        
        ByteArrayInputStream is = new ByteArrayInputStream(f.readLine().getBytes());
        		
		doReturn(is).when(mockConnection).getInputStream();
		
		fd.downloadFromURL(mockURL, result);
		
		assertTrue(result.exists());
	}
}
