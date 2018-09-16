package com.pugkung.filedownload.test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import com.pugkung.filedownload.main.URLDownloader;

public class LargeFileDownloadTest {

	@InjectMocks
	URLDownloader fd;
	
	@Test
	public void TestURLDownloader_testLargeFile() throws Exception {
		String testURL = "http://localhost/testfile.out";
		String outputPath = "";
		
		fd = spy(new URLDownloader(testURL, outputPath));
		
		String outputFile = outputPath + fd.generateOutputFileName(testURL);
		File expectedFile = new File(outputFile);
		expectedFile.deleteOnExit();
		
		URL mockURL = PowerMockito.mock(URL.class);
		
		// Generate very large file as data stream
		String mockFileName = "mockFile.in";
		RandomAccessFile f = new RandomAccessFile(mockFileName, "rw");
        f.setLength(1024 * 1024 * 1024);
        
        doAnswer(new Answer<RandomAccessFile>() {
			public RandomAccessFile answer(InvocationOnMock invocation) throws Throwable {
				RandomAccessFile mockFile = new RandomAccessFile(expectedFile,"rw");
				try {
					mockFile.seek(0);
			        for(int i = 0; i < f.length(); i++) {
			        	mockFile.write(f.read());
			        }
			        } catch(IOException ex) {
			        	//it's EOF
			        }
				return mockFile;
			}}).when(fd).downloadFromURL(any(URL.class), eq(expectedFile));
		
		fd.downloadFromURL(mockURL, expectedFile);
		
		f.close();
		assertTrue(expectedFile.exists());
		
		// cleanup test data
		File file1 = new File(mockFileName);
		File file2 = new File(outputFile);
		file1.deleteOnExit();
		file2.deleteOnExit();
	}
}
