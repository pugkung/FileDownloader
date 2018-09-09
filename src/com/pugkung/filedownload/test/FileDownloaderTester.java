package com.pugkung.filedownload.test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

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

import com.pugkung.filedownload.main.FileDownloader;
import com.pugkung.filedownload.main.FileDownloader.DownloaderStatus;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({URL.class})
public class FileDownloaderTester {
	@Rule
	  public TemporaryFolder tempFolder= new TemporaryFolder();
	
	@InjectMocks
	FileDownloader fd;
	
	@Test
	public void TestFileDownloader_stripHostNamefromLink() throws URISyntaxException {
		String testURL = "http://sub.test.com/some/path";
		String testURL2 = "ftp://10.123.234.254:23/public/file";
		String testURL3 = "https://www.hello-world.com/temp";
		
		FileDownloader fd = new FileDownloader("", "");
		assertEquals("sub_test_com", fd.getDomainName(testURL));
		assertEquals("10_123_234_254", fd.getDomainName(testURL2));
		assertEquals("hello-world_com", fd.getDomainName(testURL3));
	}
	
	@Test
	public void TestFileDownloader_stripFilePathfromLink() throws URISyntaxException {
		String testURL = "http://sub.test.com/some/path/afile.txt";
		String testURL2 = "ftp://10.123.234.254:23/public/";
		
		FileDownloader fd = new FileDownloader("", "");
		assertEquals("_some_path_afile.txt", fd.getSourceFilePath(testURL));
		assertEquals("_public_", fd.getSourceFilePath(testURL2));
	}
	
	@Test(expected = URISyntaxException.class)
	public void TestFileDownloader_tryParseInvalidURL() throws URISyntaxException {
		String testURL = "http://sub.test.com/some/path/f i l e w i t h s p a c e";
		
		FileDownloader fd = new FileDownloader(testURL, "");
		assertEquals("", fd.getDomainName(testURL));
	}
	
	@Test(expected = URISyntaxException.class)
	public void TestFileDownloader_tryParseInvalidHost() throws URISyntaxException {
		String testURL = "ftp://257.255.253.251/folder/";
		
		FileDownloader fd = new FileDownloader(testURL, "");
		assertEquals("", fd.getDomainName(testURL));
	}
	
	@Test
	public void TestFileDownloader_testDownloadSuccessful() throws Exception {
		String testURL = "http://localhost/testfile.out";
		String outputPath = "";
		
		fd = spy(new FileDownloader(testURL, outputPath));
		
		File result = new File(outputPath + fd.generateOutputFileName(testURL));
		result.deleteOnExit();
		
		doAnswer(new Answer(){
            public File answer(InvocationOnMock invocation) throws Throwable {
                result.createNewFile();
                return result;
            }}).when(fd).copyURLToFile(any(URL.class), eq(result));
		
		fd.run();
		
		verify(fd, times(1)).copyURLToFile(any(URL.class), eq(result));
		assertEquals(fd.getResultCd(),DownloaderStatus.COMPLETE);
		assertTrue(result.exists());
	}
	
	@Test
	public void TestFileDownloader_testDownloadFail() throws IOException {
		String testURL = "https://localhost/file/not/exist";
		String outputPath = "";
		
		fd = spy(new FileDownloader(testURL, outputPath));
		File result = mock(File.class);
		result.deleteOnExit();
		URL mockURL = PowerMockito.mock(URL.class);
		
	    doNothing().when(fd).copyURLToFile(eq(mockURL), any(File.class));
		
		fd.run();
		
		verify(fd, times(0)).copyURLToFile(any(URL.class), eq(result));
		assertEquals(fd.getResultCd(),DownloaderStatus.IO_ERROR);
	}
	
	
	@Test
	public void TestFileDownloader_connectionDropped() throws Exception {
		String testURL = "http://localhost/testfile.out";
		String outputPath = "";
		
		fd = spy(new FileDownloader(testURL, outputPath));
		
		File result = new File(outputPath + fd.generateOutputFileName(testURL));
		result.deleteOnExit();
		
		URL mockURL = PowerMockito.mock(URL.class);
		HttpURLConnection mockConnection = PowerMockito.mock(HttpURLConnection.class);
		SocketTimeoutException expectedException = new SocketTimeoutException();
		
		PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
		PowerMockito.when(mockURL.openConnection()).thenReturn(mockConnection);
		PowerMockito.when(mockConnection.getResponseCode()).thenThrow(expectedException);
		
		doNothing().when(fd).copyURLToFile(eq(mockURL), eq(result));
		
		fd.run();
		
		verify(fd, times(1)).copyURLToFile(any(URL.class), eq(result));
		assertEquals(fd.getResultCd(),DownloaderStatus.IO_ERROR);
		assertFalse(result.exists());
	}
}
