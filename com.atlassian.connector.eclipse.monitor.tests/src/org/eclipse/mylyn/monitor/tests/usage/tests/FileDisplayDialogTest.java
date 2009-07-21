/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Meghan Allen - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.monitor.tests.usage.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Path;
import org.eclipse.mylyn.context.tests.support.FileTool;
import org.eclipse.mylyn.internal.monitor.usage.FileDisplayDialog;
import org.eclipse.mylyn.monitor.tests.MonitorTestsPlugin;

/**
 * @author Meghan Allen
 */
public class FileDisplayDialogTest extends TestCase {

	private static final long TWO_SECONDS = 2 * 1000;

	File monitorFile;

	@Override
	protected void setUp() throws Exception {
		monitorFile = FileTool.getFileInPlugin(MonitorTestsPlugin.getDefault(), new Path("testdata/monitor-log.xml"));
	}

	@Override
	protected void tearDown() throws Exception {

	}

	public void testGetContents() throws FileNotFoundException {
		long startTime = Calendar.getInstance().getTimeInMillis();
		FileDisplayDialog.getContents(monitorFile);
		long endTime = Calendar.getInstance().getTimeInMillis();

		assertTrue(endTime - startTime <= TWO_SECONDS);
	}

}
