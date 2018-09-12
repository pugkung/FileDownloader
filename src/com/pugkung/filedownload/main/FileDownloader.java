package com.pugkung.filedownload.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class FileDownloader implements Runnable{
	
	public static enum DownloaderStatus {
		COMPLETE,
		IO_ERROR,
		URL_ERROR
	}
	
	private DownloaderStatus result;
	
	private String sourceURI;
	private String outputDirectory;
	private String outputFilePath;
	
	protected Logger logger = LogManager.getLogger(FileDownloader.class);
	
	public FileDownloader(String sourceURI,String outputDirectory) {
		this.sourceURI = sourceURI;
		this.outputDirectory = outputDirectory;
		
		logger = LogManager.getLogger(FileDownloadClient.class);
	}
	
	public DownloaderStatus downloadFile(String sourceURI, String outputDir) {
		throw new RuntimeException("downloadFile(string, string) need to be implemented.");
	}

	@Override
	public void run() {
		outputFilePath = outputDirectory + generateOutputFileName(sourceURI);
		result = downloadFile(sourceURI, outputFilePath);
	}
	
	public String generateOutputFileName(String uri) {
		String tempURI = uri.substring(uri.indexOf("://")+3);
		tempURI = tempURI.replace("/", "_").replace(".", "_");
		return tempURI;
	}
	
	public DownloaderStatus getResultCd() {
		return result;
	}
	
}
