package com.pugkung.filedownload.test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;
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
@PrepareForTest({URLConnection.class})
public class URLDownloaderTester {
	
	@InjectMocks
	URLDownloader fd;
	
	@Test
	public void TestURLDownloader_testRunner() throws Exception {
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
	public void TestURLDownloader_testDownloadSuccessful() throws Exception {
		String testURL = "http://localhost/testfile.out";
		String outputPath = "";
		
		fd = spy(new URLDownloader(testURL, outputPath));
		
		File expectedFile = new File(outputPath + fd.generateOutputFileName(testURL));
		expectedFile.deleteOnExit();
		String outputFile = outputPath + fd.generateOutputFileName(testURL);
		
		doAnswer(new Answer<File>(){
			public File answer(InvocationOnMock invocation) throws Throwable {
				expectedFile.createNewFile();
				return expectedFile;
			}}).when(fd).downloadFromURL(any(URL.class), eq(expectedFile));
		
		DownloaderStatus status = fd.downloadFile(testURL, outputFile);
		
		assertTrue(expectedFile.exists());
		assertEquals(status, DownloaderStatus.COMPLETE);
	}
	
	@Test
	public void TestURLDownloader_testDownloadFail() throws IOException {
		String testURL = "https://localhost/file/not/exist";
		String outputPath = "";
		
		fd = spy(new URLDownloader(testURL, outputPath));
		File expectedFile = mock(File.class);
		expectedFile.deleteOnExit();
		
	    doThrow(IOException.class).when(fd).downloadFromURL(any(URL.class), any(File.class));
		
		DownloaderStatus status = fd.downloadFile(testURL, outputPath);
		
		assertEquals(status,DownloaderStatus.IO_ERROR);
		assertFalse(expectedFile.exists());
	}
	
	@Test
	public void TestURLDownloader_connectionDropped() throws Exception {
		String testURL = "http://localhost/testfile.out";
		String outputPath = "";
		
		fd = spy(new URLDownloader(testURL, outputPath));
		
		String outputFile = outputPath + fd.generateOutputFileName(testURL);
		File expectedFile = new File(outputFile);
		expectedFile.deleteOnExit();
		
		URL mockURL = PowerMockito.mock(URL.class);
		HttpURLConnection mockConnection = PowerMockito.mock(HttpURLConnection.class);
		SocketTimeoutException expectedException = new SocketTimeoutException();
		
		PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
		PowerMockito.when(mockURL.openConnection()).thenReturn(mockConnection);
		PowerMockito.when(mockConnection.getResponseCode()).thenThrow(expectedException);
		
		DownloaderStatus status = fd.downloadFile(testURL, outputFile);
		
		verify(fd, times(1)).downloadFromURL(any(URL.class), eq(expectedFile));
		assertEquals(status,DownloaderStatus.IO_ERROR);
		assertFalse(expectedFile.exists());
	}
	
	@Test
	public void TestURLDownloader_testLargeFile() throws Exception {
		String testURL = "http://localhost/testfile.out";
		String outputPath = "";
		
		fd = spy(new URLDownloader(testURL, outputPath));
		
		String outputFile = outputPath + fd.generateOutputFileName(testURL);
		File expectedFile = new File(outputFile);
		expectedFile.deleteOnExit();
		
		URL mockURL = PowerMockito.mock(URL.class);
		
		// Generate very large file as data stream
		String mockFileName = "mockFile.in";
		RandomAccessFile f = new RandomAccessFile(mockFileName, "rw");
        f.setLength(1024 * 1024 * 1024);
        
        doAnswer(new Answer<RandomAccessFile>() {
			public RandomAccessFile answer(InvocationOnMock invocation) throws Throwable {
				RandomAccessFile mockFile = new RandomAccessFile(expectedFile,"rw");
				try {
					mockFile.seek(0);
			        for(int i = 0; i < f.length(); i++) {
			        	mockFile.write(f.read());
			        }
			        } catch(IOException ex) {
			        	//it's EOF
			        }
				return mockFile;
			}}).when(fd).downloadFromURL(any(URL.class), eq(expectedFile));
		
		fd.downloadFromURL(mockURL, expectedFile);
		
		f.close();
		assertTrue(expectedFile.exists());
		
		// cleanup test data
		File file1 = new File(mockFileName);
		File file2 = new File(outputFile);
		file1.deleteOnExit();
		file2.deleteOnExit();
	}
}
