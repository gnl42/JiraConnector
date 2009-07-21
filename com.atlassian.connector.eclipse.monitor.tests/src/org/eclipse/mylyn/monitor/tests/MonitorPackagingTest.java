/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.monitor.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.mylyn.context.tests.AbstractContextTest;
import org.eclipse.mylyn.internal.commons.core.ZipFileUtil;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;

/**
 * @author Mik Kersten
 */
public class MonitorPackagingTest extends AbstractContextTest {

	public void testCreateUploadPackage() throws IOException, InterruptedException {
		UiUsageMonitorPlugin.getDefault().getInteractionLogger().stopMonitoring();

		File monitorFile = UiUsageMonitorPlugin.getDefault().getMonitorLogFile();

		List<File> files = new ArrayList<File>();
		files.add(monitorFile);
		// files.add(logFile);

		File zipFile = new File(ContextCorePlugin.getContextStore().getContextDirectory().getParentFile()
				+ "/mylarUpload.zip");

		ZipFileUtil.createZipFile(zipFile, files);

		// MylarMonitorPlugin.getDefault().startLog();
		UiUsageMonitorPlugin.getDefault().getInteractionLogger().startMonitoring();

		// pretend to upload
		Thread.sleep(1000);

		zipFile = new File(ContextCorePlugin.getContextStore().getContextDirectory().getParentFile()
				+ "/mylarUpload.zip");

		// Open the ZIP file
		ZipFile zf = new ZipFile(zipFile);

		int numEntries = 0;

		// Enumerate each entry
		for (Enumeration<? extends ZipEntry> entries = zf.entries(); entries.hasMoreElements();) {
			numEntries++;
			String zipEntryName = ((ZipEntry) entries.nextElement()).getName();
			assertTrue("Unknown Entry: " + zipEntryName, zipEntryName.compareTo(monitorFile.getName()) == 0);// ||
			// zipEntryName.compareTo(logFile.getName())
			// ==
			// 0);
		}
		assertEquals("Results not correct size", 1, numEntries);

		// check the length of the zip
		// long fileLength = monitorFile.length() + logFile.length();
		// if(monitorFile.length() != 0 || logFile.length() != 0)
		// assertTrue("Zip didn't help", fileLength > zipFile.length());

		// delete it
		zipFile.delete();
	}

	public void testCreateLargeUploadPackage() throws IOException, InterruptedException {

		for (int i = 0; i < 20000; i++) {
			MonitorUiPlugin.getDefault().notifyInteractionObserved(mockSelection());
		}
		testCreateUploadPackage();
	}

}
