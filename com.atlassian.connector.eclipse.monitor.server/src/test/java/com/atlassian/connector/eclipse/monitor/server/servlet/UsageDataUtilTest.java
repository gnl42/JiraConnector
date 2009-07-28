package com.atlassian.connector.eclipse.monitor.server.servlet;

import java.io.IOException;
import java.net.URL;

import junit.framework.Assert;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.eclipse.mylyn.monitor.core.UserInteractionEvent;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.connector.eclipse.monitor.server.servlet.UsageDataUtil.UserInteractionEventCallback;

public class UsageDataUtilTest {

	private URL zipUrl;
	private final String zipName = "c29dfb31-108f-4d4b-a861-86cabef65054.707263666982297556.zip";
	private FileItem zipItem;
	
	private final String xmlName = "processed-monitor-log1.xml";
	
	@Before
	public void setUp() throws IOException {
		zipUrl = getClass().getClassLoader().getResource(zipName);
		Assert.assertNotNull(zipUrl);
		
		zipItem = new DiskFileItem("", "application/zip", false, zipName, 0xffffff, null);
		IOUtils.copy(getClass().getClassLoader().getResourceAsStream(zipName), zipItem.getOutputStream());
	}
	
	@Test
	public void testProcessFile() throws IOException {
		
		final int[] countFiles = { 0 };
		UsageDataUtil.processFile(zipItem, new UserInteractionEventCallback() {
			public boolean visit(UserInteractionEvent uie) {
				++countFiles[0];
				return true;
			}
		});
		Assert.assertEquals(18015, countFiles[0]);
	}
	
	@Test
	public void testProcessStream() throws IOException {
		
		final int[] countFiles = { 0 };
		UsageDataUtil.processStream(getClass().getClassLoader().getResourceAsStream(xmlName), new UserInteractionEventCallback() {
			public boolean visit(UserInteractionEvent uie) {
				++countFiles[0];
				return true;
			}
		}, "uid");
		Assert.assertEquals(18015, countFiles[0]);
		
	}
}
