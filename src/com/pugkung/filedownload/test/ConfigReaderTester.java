package com.pugkung.filedownload.test;

import com.pugkung.filedownload.main.ConfigReader;
import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConfigReaderTester {

	@Test
	public void TestConfigReader_readOutputPathFromConfig() {
		String configFile = "src/test/resources/downloadSingleFile.properties";
		ConfigReader cr  = new ConfigReader(configFile);
		boolean success = cr.loadConfigData();
		
		assertTrue(success);
		assertNotEquals("", cr.getOutputPath());
	}
	
	@Test
	public void TestConfigReader_readURLsFromConfig() {
		String configFile = "src/test/resources/downloadMultipleSources.properties";
		ConfigReader cr  = new ConfigReader(configFile);
		boolean success = cr.loadConfigData();
		
		assertTrue(success);
		assertNotEquals(0, cr.getURLs().size());
	}
	
	@Test
	public void TestConfigReader_outputPathIsNotProvided() {
		String configFile = "src/test/resources/noOutputPath.properties";
		ConfigReader cr  = new ConfigReader(configFile);
		boolean success = cr.loadConfigData();
		String expectedPath = new File("").getAbsoluteFile().toString() + "/";
		
		assertTrue(success);
		assertEquals(expectedPath, cr.getOutputPath());
	}
	
	@Test
	public void TestConfigReader_urlIsNotProvided() {
		String configFile = "src/test/resources/empty.properties";
		ConfigReader cr  = new ConfigReader(configFile);
		boolean success = cr.loadConfigData();
		
		assertTrue(success);
		assertEquals(0, cr.getURLs().size());
	}
	
	@Test
	public void TestConfigReader_configFileNotExist() {
		String configFile = "src/test/resources/missingConfigFile";
		ConfigReader cr  = new ConfigReader(configFile);
		boolean success = cr.loadConfigData();
		
		assertFalse(success);;
	}
}
