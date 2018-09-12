package com.pugkung.filedownload.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class FileDownloadClient {
	
	private final static String DEFAULT_CONFIG_FILENAME = "config.properties";
	
	public static enum ExitStatus {
		NORMAL,
		MISSING_CONFIGURATION,
		NO_URL_PROVIDED
	}
	
	private static Logger logger;
	private static ExitStatus exitStatusCode;
	private static List<Thread> threads;
	
	public static void main(String args[]) {
		
		FileDownloadClient client = new FileDownloadClient();
		logger = LogManager.getLogger(FileDownloadClient.class);
		threads = new ArrayList<Thread>();
		
		
		String configLocation;
		if (args.length > 0) {
			configLocation = args[0];
		}
		else {
			configLocation = DEFAULT_CONFIG_FILENAME;
		}
		
		ConfigReader config;
		try {
			config = client.loadConfigFile(configLocation);
			config.loadConfigData();
		} catch (FileNotFoundException ex) {
			exitStatusCode = ExitStatus.MISSING_CONFIGURATION;
			return;
		}
		
		String outputPath = config.getOutputPath();
		List<String> urlList = config.getURLs();
		
		if (!urlList.isEmpty()) {
			client.distributeURLsToDownloaderThread(urlList, outputPath);
			client.waitForAllThreads();
			
			logger.info("All files have been processed.");
			exitStatusCode = ExitStatus.NORMAL;
		}
		else {
			logger.info("No URL was provided in config file.");
			exitStatusCode = ExitStatus.NO_URL_PROVIDED;
		}
	}
	
	public ConfigReader loadConfigFile(String configLocation) throws FileNotFoundException {
		File configFile = new File(configLocation);
		
		if (configFile.exists()) {
			logger.info("Read config file from: " + configLocation);
			return new ConfigReader(configLocation);
		}
		else {
			logger.error("Unable to locate configuration file: " + configLocation);
			throw new FileNotFoundException();
		}
	}
	
	public void distributeURLsToDownloaderThread(List<String> urlList, String outputPath) {
		for (String item : urlList) {
			executeDownloaderThread(item, outputPath);
		}
	}
	
	public void executeDownloaderThread(String targetURL, String outputPath) {
		URLDownloader fd = new URLDownloader(targetURL, outputPath);
		Thread t = new Thread(fd);
		t.start();
		threads.add(t);
	}
	
	public void waitForAllThreads() {
		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException ex) {
				logger.error(ex.getMessage());
			}
		}
	}
	
	public ExitStatus getExitStatusCode() {
		return exitStatusCode;
	}
}
