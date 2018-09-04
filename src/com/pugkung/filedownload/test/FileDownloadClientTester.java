package com.pugkung.filedownload.test;

import com.pugkung.filedownload.main.*;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class FileDownloadClientTester {

	@Test
	public void TestFileDownloaderClient_downloadSingleFile() throws URISyntaxException {
		String configFile = "src/test/resources/downloadSingleFile.properties";
		ConfigReader config = new ConfigReader(configFile);
		config.loadConfigData();
		FileDownloader fd = new FileDownloader(config.getURLs().get(0), config.getOutputPath());
		String expectedFile = config.getOutputPath() + fd.generateOutputFileName(config.getURLs().get(0));
		
		FileDownloadClient.main(new String[] {configFile});
		
		File file = new File(expectedFile);
		assertTrue(file.exists());
		assertEquals(FileDownloadClient.ExitStatus.NORMAL, FileDownloadClient.getExitStatusCode());
	}
	
	@Test
	public void TestFileDownloaderClient_downloadMultipleSources() throws URISyntaxException {
		String configFile = "src/test/resources/downloadMultipleSources.properties";
		ConfigReader config = new ConfigReader(configFile);
		config.loadConfigData();
		List<String> expectedFiles = new ArrayList<String>();
		for (int i=0; i<config.getURLs().size(); i++) {
			FileDownloader fd = new FileDownloader(config.getURLs().get(i), config.getOutputPath());
			expectedFiles.add(config.getOutputPath() + fd.generateOutputFileName(config.getURLs().get(i)));
		}
		
		FileDownloadClient.main(new String[] {configFile});
		
		for (String output : expectedFiles) {
			File file = new File(output);
			assertTrue(file.exists());
		}
		assertEquals(FileDownloadClient.ExitStatus.NORMAL, FileDownloadClient.getExitStatusCode());
	}
	
	@Test
	public void TestFileDownloaderClient_downloadMixedFailSources() throws URISyntaxException {
		String configFile = "src/test/resources/downloadMixedFailSources.properties";
		ConfigReader config = new ConfigReader(configFile);
		config.loadConfigData();
		List<String> expectedFiles = new ArrayList<String>();
		for (int i=0; i<config.getURLs().size(); i++) {
			FileDownloader fd = new FileDownloader(config.getURLs().get(i), config.getOutputPath());
			expectedFiles.add(config.getOutputPath() + fd.generateOutputFileName(config.getURLs().get(i)));
		}
		
		FileDownloadClient.main(new String[] {configFile});
		
		//Based on test config file, item#1 and item#2 is not exist, so only 2 files is expected to be created
		int outputCount = 0;
		for (int i=0;i<expectedFiles.size();i++) {
			File file = new File(expectedFiles.get(i));
			if(file.exists()) {
				outputCount++;
			}
		}
		
		assertEquals(2, outputCount);
		assertEquals(FileDownloadClient.ExitStatus.NORMAL, FileDownloadClient.getExitStatusCode());
	}
	
	@Test
	public void TestFileDownloaderClient_downloadLargeFile() throws URISyntaxException {
		//Test execute this onto VM/local server to reduce traffic time
		String configFile = "src/test/resources/downloadLargeFile.properties";
		ConfigReader config = new ConfigReader(configFile);
		config.loadConfigData();
		FileDownloader fd = new FileDownloader(config.getURLs().get(0), config.getOutputPath());
		String expectedFile = config.getOutputPath() + fd.generateOutputFileName(config.getURLs().get(0));
		
		FileDownloadClient.main(new String[] {configFile});
		
		File file = new File(expectedFile);
		assertTrue(file.exists());
		assertEquals(FileDownloadClient.ExitStatus.NORMAL, FileDownloadClient.getExitStatusCode());
	}
	
	@Test
	public void TestFileDownloaderClient_nonExistingURL() throws URISyntaxException {
		String configFile = "src/test/resources/nonExistingURL.properties";
		ConfigReader config = new ConfigReader(configFile);
		config.loadConfigData();
		FileDownloader fd = new FileDownloader(config.getURLs().get(0), config.getOutputPath());
		String expectedFile = config.getOutputPath() + fd.generateOutputFileName(config.getURLs().get(0));
		
		FileDownloadClient.main(new String[] {configFile});
		
		File file = new File(expectedFile);
		assertFalse(file.exists());
	}
	
	@Test(expected = URISyntaxException.class)
	public void TestFileDownloaderClient_invalidURLAsInput() throws URISyntaxException {
		String configFile = "src/test/resources/invalidURL.properties";
		ConfigReader config = new ConfigReader(configFile);
		config.loadConfigData();
		FileDownloader fd = new FileDownloader(config.getURLs().get(0), config.getOutputPath());
		String expectedFile = config.getOutputPath() + fd.generateOutputFileName(config.getURLs().get(0));
		
		FileDownloadClient.main(new String[] {configFile});
		
		File file = new File(expectedFile);
		assertFalse(file.exists());
		assertEquals(FileDownloadClient.ExitStatus.NO_URL_PROVIDED, FileDownloadClient.getExitStatusCode());
	}
	
	@Test
	public void TestFileDownloaderClient_noSourceURLAsInput() throws URISyntaxException {
		String configFile = "src/test/resources/empty.properties";
		ConfigReader config = new ConfigReader(configFile);
		config.loadConfigData();
		List<String> expectedFiles = new ArrayList<String>();
		for (int i=0; i<config.getURLs().size(); i++) {
			FileDownloader fd = new FileDownloader(config.getURLs().get(i), config.getOutputPath());
			expectedFiles.add(config.getOutputPath() + fd.generateOutputFileName(config.getURLs().get(i)));
		}
		
		FileDownloadClient.main(new String[] {configFile});
		
		assertEquals(0, expectedFiles.size());
		assertEquals(FileDownloadClient.ExitStatus.NO_URL_PROVIDED, FileDownloadClient.getExitStatusCode());
	}
	
	@Test
	public void TestFileDownloaderClient_invalidConfigFile() {
		String configFile = "src/test/resources/missingConfigFile";
		
		FileDownloadClient.main(new String[] {configFile});
		assertEquals(FileDownloadClient.ExitStatus.MISSING_CONFIGURATION, FileDownloadClient.getExitStatusCode());
	}
	

}
