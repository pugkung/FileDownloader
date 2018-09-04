package com.pugkung.filedownload.main;

import java.io.File;
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
	
	private static ExitStatus exitStatusCode;
	
	public static void main(String args[]) {
		Logger logger = LogManager.getLogger(FileDownloadClient.class);
		ConfigReader config;
		String configLocation;
		File configFile;
		
		if (args.length > 0) {
			configLocation = args[0];
		}
		else {
			configLocation = DEFAULT_CONFIG_FILENAME;
		}
		configFile = new File(configLocation);
		
		if (configFile.exists()) {
			logger.info("Read config file from: " + configLocation);
			config = new ConfigReader(configLocation);
			config.loadConfigData();
		}
		else {
			logger.error("Unable to locate configuration file: " + configLocation);
			exitStatusCode = ExitStatus.MISSING_CONFIGURATION;
			return;
		}
		
		String outputPath = config.getOutputPath();
		List<String> urlList = config.getURLs();
		
		List<Thread> threads = new ArrayList<Thread>();
		if (!urlList.isEmpty()) {
			for (String item : urlList) {
				FileDownloader fd = new FileDownloader(item, outputPath);
				Thread t = new Thread(fd);
				t.start();
				threads.add(t);
			}
			
			// Wait all threads to finish
			for (Thread t : threads) {
				try {
					t.join();
				} catch (InterruptedException ex) {
					logger.error(ex.getMessage());
				}
			}
			
			logger.info("All files have been processed.");
			exitStatusCode = ExitStatus.NORMAL;
		}
		else {
			logger.info("No URL was provided in config file.");
			exitStatusCode = ExitStatus.NO_URL_PROVIDED;
		}
	}
	
	public static ExitStatus getExitStatusCode() {
		return exitStatusCode;
	}
}
