package com.pugkung.filedownload.test;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Test;

import com.pugkung.filedownload.main.FileDownloader;

public class FileDownloaderTester {

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
		
		FileDownloader fd = new FileDownloader("", "");
		assertEquals("", fd.getDomainName(testURL));
	}
	
	@Test
	public void TestFileDownloader_testDownloadSuccessful() throws URISyntaxException {
		String testURL = "https://www.wikipedia.org/portal/wikipedia.org/assets/img/Wikipedia-logo-v2.png";
		String outputPath = "";
		
		FileDownloader fd = new FileDownloader(testURL, outputPath);
		fd.run();

		File result = new File(outputPath + fd.generateOutputFileName(testURL));
		assertTrue(result.exists());
	}
	
	@Test
	public void TestFileDownloader_testDownloadFail() throws URISyntaxException {
		String testURL = "https://localhost/file/not/exist";
		String outputPath = "";
		
		FileDownloader fd = new FileDownloader(testURL, outputPath);
		fd.run();

		File result = new File(outputPath + fd.generateOutputFileName(testURL));
		assertFalse(result.exists());
	}
}
