package com.pugkung.filedownload.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigReader {
	private Logger logger = LogManager.getLogger(ConfigReader.class);
	private final String OUTPUTPATH_PROPERTY_KEYNAME = "outputPath";
	
	private String configFileName = "";
	private String outputPath = "";
	private List<String> urlList;
	
	public ConfigReader(String configFileName) {
		this.configFileName = configFileName;
	}
	
	public boolean loadConfigData() {
		InputStream configFile = null;
		Properties properties = new Properties();
		
		try {
			configFile = new FileInputStream(configFileName);
			properties.load(configFile);
		} catch (FileNotFoundException ex) {
			logger.error("Unable to read configFile: " + ex.getMessage());
			return false;
		} catch (IOException ex) {
			logger.error(ex.getMessage());
			return false;
		}
		
		loadOutputPathFromConfig(properties);
		loadURLsFromConfig(properties);
		
		return true;
	}
	
	protected void loadOutputPathFromConfig(Properties props) {
		String outputPath = "";
		
		outputPath = props.getProperty(OUTPUTPATH_PROPERTY_KEYNAME);
		
		if (outputPath == null || outputPath.equals("")) {
			outputPath = new File("").getAbsoluteFile().toString() + "/";
			logger.info(OUTPUTPATH_PROPERTY_KEYNAME + " property is not provided. " +
						"Output destination is set by default to: " + outputPath);
		}
		
		this.outputPath = outputPath;
	}
	
	protected void loadURLsFromConfig(Properties props) {
		Enumeration<Object> configEntries;
		String key, value;
		
		urlList = new ArrayList<String>();
		
		configEntries = props.keys();
		while (configEntries.hasMoreElements()) {
			key = (String) configEntries.nextElement();
			value = props.getProperty(key);
				
			if (!key.equals(OUTPUTPATH_PROPERTY_KEYNAME)) {
				urlList.add(value);
			}
		}
	}
	
	public String getOutputPath() {
		return outputPath;
	}
	
	public List<String> getURLs() {
		return urlList;
	}
}
