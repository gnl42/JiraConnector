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

package org.eclipse.mylyn.monitor.reports.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Path;
import org.eclipse.mylyn.internal.monitor.core.collection.DataOverviewCollector;
import org.eclipse.mylyn.internal.monitor.core.collection.IUsageCollector;
import org.eclipse.mylyn.internal.monitor.usage.InteractionEventLogger;
import org.eclipse.mylyn.internal.monitor.usage.ReportGenerator;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;
import org.eclipse.mylyn.monitor.core.AbstractMonitorLog;
import org.eclipse.mylyn.monitor.tests.MonitorTestsPlugin;

/**
 * 
 * @author Gail Murphy
 */
public class DataOverviewCollectorTest extends TestCase {

	private DataOverviewCollector dataOverviewCollector = null;

	public void testNumberOfUsers() {
		assertTrue(dataOverviewCollector.getNumberOfUsers() == 2);
	}

	public void testActiveUse() {
		long activeUse = dataOverviewCollector.getActiveUseOfUser(1);
		assertTrue("User 1 Use", getHoursOfDuration(activeUse) == 0);
		activeUse = dataOverviewCollector.getActiveUseOfUser(2);
		assertTrue("User 2 Use", getHoursOfDuration(activeUse) == 0);

	}

	public void testTimePeriodOfUse() {
		long durationOfUse = dataOverviewCollector.getDurationUseOfUser(1);
		assertTrue("User 1 duration", getHoursOfDuration(durationOfUse) == 24);
		durationOfUse = dataOverviewCollector.getDurationUseOfUser(2);
		assertTrue("User 2 duration", getHoursOfDuration(durationOfUse) == 24);
	}

	public void testSizeOfHistory() {
		int size = dataOverviewCollector.getSizeOfHistory(1);
		assertTrue("User 1 size", size == 21);
		size = dataOverviewCollector.getSizeOfHistory(2);
		assertTrue("User 2 size", size == 21);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		List<File> interactionHistoryFiles = new ArrayList<File>();

		// Access two interaction history files that are copies of each other
		File firstInteractionHistoryFile = FileTool.getFileInPlugin(MonitorTestsPlugin.getDefault(), new Path(
				"testdata/USAGE-1.1.1-usage-1-2005-12-05-1-1-1.zip"));
		interactionHistoryFiles.add(firstInteractionHistoryFile);
		File secondInteractionHistoryFile = FileTool.getFileInPlugin(MonitorTestsPlugin.getDefault(), new Path(
				"testdata/USAGE-1.1.1-usage-2-2005-12-05-1-1-1.zip"));
		interactionHistoryFiles.add(secondInteractionHistoryFile);

		// Initialize fake logger
		File logFile = new File("test-log.xml");
		logFile.delete();
		AbstractMonitorLog logger = new InteractionEventLogger(logFile);
		logger.startMonitoring();

		// Prepare collectors
		List<IUsageCollector> collectors = new ArrayList<IUsageCollector>();
		dataOverviewCollector = new DataOverviewCollector("test-");
		collectors.add(dataOverviewCollector);

		ReportGenerator generator = new ReportGenerator(UiUsageMonitorPlugin.getDefault().getInteractionLogger(),
				collectors);
		generator.forceSyncForTesting(true);
		generator.getStatisticsFromInteractionHistories(interactionHistoryFiles, null);

		// cleanup
		logFile.delete();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private long getHoursOfDuration(long duration) {
		long timeInSeconds = duration / 1000;
		long hours = timeInSeconds / 3600;
		return hours;
	}

}
