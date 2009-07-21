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

package org.eclipse.mylyn.internal.monitor.core.collection;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mylyn.monitor.core.InteractionEvent;

/**
 * @author Mik Kersten
 */
public class DelegatingUsageCollector implements IUsageCollector {

	protected List<IUsageScanner> scanners = new ArrayList<IUsageScanner>();

	public void addScanner(IUsageScanner aScanner) {
		scanners.add(aScanner);
	}

	private List<IUsageCollector> delegates = new ArrayList<IUsageCollector>();

	private String reportTitle = ""; //$NON-NLS-1$

	public List<IUsageCollector> getDelegates() {
		return delegates;
	}

	public void setDelegates(List<IUsageCollector> delegates) {
		this.delegates = delegates;
	}

	public void consumeEvent(InteractionEvent event, int userId) {
		for (IUsageCollector collector : delegates) {
			collector.consumeEvent(event, userId);
		}
	}

	public List<String> getReport() {
		List<String> combinedReports = new ArrayList<String>();
		for (IUsageCollector collector : delegates) {
			combinedReports.add("<h3>" + collector.getReportTitle() + "</h3>"); //$NON-NLS-1$ //$NON-NLS-2$
			combinedReports.addAll(collector.getReport());
		}
		return combinedReports;
	}

	public void exportAsCSVFile(String directory) {

	}

	public String getReportTitle() {
		return reportTitle;
	}

	public void setReportTitle(String reportTitle) {
		this.reportTitle = reportTitle;
	}

	public List<String> getPlainTextReport() {
		List<String> combinedReports = new ArrayList<String>();
		for (IUsageCollector collector : delegates) {
			combinedReports.add(collector.getReportTitle());
			combinedReports.addAll(collector.getPlainTextReport());
		}
		return combinedReports;
	}
}
