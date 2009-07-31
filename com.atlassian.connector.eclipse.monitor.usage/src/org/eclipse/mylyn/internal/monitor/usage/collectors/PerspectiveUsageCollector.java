/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Leah Findlater - improvements
 *******************************************************************************/

package org.eclipse.mylyn.internal.monitor.usage.collectors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.monitor.core.collection.IUsageCollector;
import org.eclipse.mylyn.internal.monitor.core.collection.PercentUsageComparator;
import org.eclipse.mylyn.internal.monitor.ui.PerspectiveChangeMonitor;
import org.eclipse.mylyn.monitor.core.InteractionEvent;

/**
 * @author Mik Kersten
 * @author Leah Findlater TODO: put unclassified events in dummy perspective
 */
public class PerspectiveUsageCollector implements IUsageCollector {

	private final Map<String, Integer> perspectiveUsage = new HashMap<String, Integer>();

	private String currentPerspective = "";

	private int numUnassociatedEvents = 0;

	private int numEvents = 0;

	public void consumeEvent(InteractionEvent event, int userId) {
		numEvents++;
		if (event.getKind().equals(InteractionEvent.Kind.PREFERENCE)) {
			if (event.getDelta().equals(PerspectiveChangeMonitor.PERSPECTIVE_ACTIVATED)) {
				currentPerspective = event.getOriginId();
				if (!perspectiveUsage.containsKey(event.getOriginId())) {
					perspectiveUsage.put(event.getOriginId(), 1);
				}
			}
		}

		if (!perspectiveUsage.containsKey(currentPerspective)) {
			numUnassociatedEvents++;
			return;
		}

		perspectiveUsage.put(currentPerspective, perspectiveUsage.get(currentPerspective) + 1);
	}

	public List<String> getReport() {
		return getReport(true);
	}

	public String getReportTitle() {
		return "Perspective Usage";
	}

	public void exportAsCSVFile(String directory) {
		String filename = directory + File.separator + "PerspectiveUsage.csv";

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));

			// Write header
			writer.write("Perspective");
			writer.write(",");
			writer.write("Events");
			writer.newLine();

			// Write Data
			for (String perspective : perspectiveUsage.keySet()) {
				writer.write(perspective);
				writer.write(",");
				writer.write(new Integer(perspectiveUsage.get(perspective)).toString());
				writer.newLine();
			}

			writer.write("Unclassified");
			writer.write(",");
			writer.write(numUnassociatedEvents);
			writer.newLine();

			writer.flush();
			writer.close();

		} catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, "org.eclipse.mylyn.monitor.core", "Unable to write CVS file <" //$NON-NLS-1$//$NON-NLS-2$
					+ filename + ">", e)); //$NON-NLS-1$
		}

	}

	public List<String> getPlainTextReport() {
		return getReport(false);
	}

	private List<String> getReport(boolean html) {
		List<String> summaries = new ArrayList<String>();
		summaries.add("Perspectives (based on total user events, with " + numUnassociatedEvents
				+ " unclassified events)");
		summaries.add(" ");

		List<String> perspectiveUsageList = new ArrayList<String>();
		for (String perspective : perspectiveUsage.keySet()) {
			float perspectiveUse = 100 * perspectiveUsage.get(perspective) / (numEvents);
			String formattedPerspectiveUse = ("" + perspectiveUse);
			int indexOf2ndDecimal = formattedPerspectiveUse.indexOf('.') + 3;
			if (indexOf2ndDecimal <= formattedPerspectiveUse.length()) {
				formattedPerspectiveUse = formattedPerspectiveUse.substring(0, indexOf2ndDecimal);
			}
			String perspectiveName = perspective; // .substring(perspective.lastIndexOf(".")+1,
			// perspective.length());
			if (perspectiveName.contains("Perspective")) {
				perspectiveName = perspectiveName.substring(0, perspectiveName.indexOf("Perspective"));
			}
			perspectiveUsageList.add(formattedPerspectiveUse + "%: " + perspectiveName + " ("
					+ perspectiveUsage.get(perspective) + ")");
		}
		Collections.sort(perspectiveUsageList, new PercentUsageComparator());
		for (String perspectiveUsageSummary : perspectiveUsageList) {
			if (html) {
				summaries.add("<br>" + perspectiveUsageSummary);
			} else {
				summaries.add(perspectiveUsageSummary);
			}
		}

		if (perspectiveUsage.size() % 2 != 0) {
			summaries.add(" ");
		}
		return summaries;

	}
}
