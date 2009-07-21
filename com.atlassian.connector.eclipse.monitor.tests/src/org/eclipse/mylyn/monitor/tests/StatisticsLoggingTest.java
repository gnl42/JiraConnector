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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.mylyn.context.tests.ContextTest;
import org.eclipse.mylyn.internal.monitor.core.collection.IUsageCollector;
import org.eclipse.mylyn.internal.monitor.core.collection.InteractionEventSummary;
import org.eclipse.mylyn.internal.monitor.core.collection.SummaryCollector;
import org.eclipse.mylyn.internal.monitor.usage.InteractionEventLogger;
import org.eclipse.mylyn.internal.monitor.usage.ReportGenerator;

/**
 * @author Mik Kersten
 */
public class StatisticsLoggingTest extends ContextTest {

	private File logFile;

	private InteractionEventLogger logger;

	private ReportGenerator report;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		logFile = new File("test-log.xml");
		logFile.delete();
		logger = new InteractionEventLogger(logFile);
		logger.startMonitoring();
		List<IUsageCollector> collectors = new ArrayList<IUsageCollector>();
		collectors.add(new SummaryCollector());
		report = new ReportGenerator(logger, collectors);
		report.forceSyncForTesting(true);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		logFile.delete();
	}

	public void testFileReading() {
		logger.interactionObserved(mockSelection());
		mockUserDelay();
		logger.interactionObserved(mockSelection());
		logger.stopMonitoring();

		report.getStatisticsFromInteractionHistory(logFile, new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				List<InteractionEventSummary> summary = report.getLastParsedSummary().getSingleSummaries();
				assertEquals(1, summary.size());
				InteractionEventSummary first = summary.get(0);
				assertEquals(2, first.getUsageCount());
			}
		});

	}

	/**
	 * Delay enough to make replicated events different
	 */
	private void mockUserDelay() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException ie) {
			;
		}
	}

}
