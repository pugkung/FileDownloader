package com.pugkung.filedownload.main;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;

public class URLDownloader extends FileDownloader {
	
	private final int CONNECTION_TIMEOUT_LIMIT = 10000;
	private final int READ_TIMEOUT_LIMIT = 10000;

	public URLDownloader(String sourceURI, String outputDirectory) {
		super(sourceURI, outputDirectory);
		
		logger = LogManager.getLogger(URLDownloader.class);
	}
	
	@Override
	public DownloaderStatus downloadFile(String sourceURI, String outputFilePath){
		File outputFile = new File(outputFilePath);
		try {
			long startTime, finishTime;
			startTime = System.currentTimeMillis();
			logger.info("Start downloading: " + sourceURI);
			
			URL url = new URL(sourceURI);
			downloadFromURL(url, outputFile);
			
			finishTime = System.currentTimeMillis();
			logger.info("Task finished: " + sourceURI + " (" + (finishTime - startTime) +  "ms)");
			return DownloaderStatus.COMPLETE;
		} catch (IOException ex) {
			logger.error("Problem occurred while downloading: " + ex.getMessage());
			logger.info("Cleaning up: " + outputFilePath);
			outputFile.delete();
			return DownloaderStatus.IO_ERROR;
        }
	}
	
	public void downloadFromURL(URL url, File outputFile) throws IOException {
		FileUtils.copyURLToFile(url, outputFile, CONNECTION_TIMEOUT_LIMIT, READ_TIMEOUT_LIMIT);
	}
	
	public String getDomainNameFromURL(String url) throws URISyntaxException {
	    URI uri = new URI(url);
	    String domain = uri.getHost();
	    if (domain == null) {
	    	throw new URISyntaxException(url, "Cannot Read Hostname from URL");
	    }
	    //Strip leading 'www.' from domain name if exist
	    domain = domain.startsWith("www.") ? domain.substring(4) : domain;
	    domain = domain.replace(".", "_");
	    return domain;
	}
	
	public String getFilePathFromURL(String url) throws URISyntaxException {
	    URI uri = new URI(url);
	    String path = uri.getPath().replace("/", "_");
	    return path;
	}

}
