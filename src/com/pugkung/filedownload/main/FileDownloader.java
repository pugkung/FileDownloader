package com.pugkung.filedownload.main;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileDownloader implements Runnable{
	
	private final int CONNECTION_TIMEOUT_LIMIT = 10000;
	private final int READ_TIMEOUT_LIMIT = 10000;
	
	private File outputFile;
	private String sourceURL;
	private String targetFilePath;
	
	private Logger logger = LogManager.getLogger(FileDownloader.class);
	
	public FileDownloader(String sourceURL,String targetFilePath) {
		this.sourceURL = sourceURL;
		this.targetFilePath = targetFilePath;
	}
	
	protected void getFileFromURL(String sourceURL, String targetFilePath) {
		String outputFileName = "";
		try {
			outputFileName = targetFilePath + generateOutputFileName(sourceURL);
			outputFile = new File(outputFileName);
			
			long startTime = System.currentTimeMillis();
			URL url = new URL(sourceURL);
			
			logger.info("Start downloading: " + sourceURL);
			FileUtils.copyURLToFile(url, outputFile, CONNECTION_TIMEOUT_LIMIT, READ_TIMEOUT_LIMIT);
			
			long finishTime = System.currentTimeMillis();
			logger.info("Download complete: " + sourceURL + " (" + (finishTime - startTime) +  "ms)");
		} catch (IOException ex) {
			logger.error("Problem occurred while downloading: " + ex.getMessage());
			logger.info("Cleaning up: " + outputFileName);
			outputFile.delete();
        } catch (URISyntaxException ex) {
        	logger.error("Unable to generate output file: " + ex.getMessage());
		}
	}

	@Override
	public void run() {
		getFileFromURL(sourceURL, targetFilePath);
	}
	
	public String generateOutputFileName(String url) throws URISyntaxException {
		return getDomainName(url) + getSourceFilePath(url);
	}
	
	public String getDomainName(String url) throws URISyntaxException {
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
	
	public String getSourceFilePath(String url) throws URISyntaxException {
	    URI uri = new URI(url);
	    String path = uri.getPath().replace("/", "_");
	    return path;
	}
}