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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.mylyn.internal.monitor.core.collection.messages"; //$NON-NLS-1$

	static {
		// load message values from bundle file
		reloadMessages();
	}

	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String CommandUsageCollector_Command_Usage;

	public static String DataOverviewCollector_active_use;

	public static String DataOverviewCollector_CSV_ACTIVE_USE;

	public static String DataOverviewCollector_CSV_ELAPSED_USE;

	public static String DataOverviewCollector_CSV_END;

	public static String DataOverviewCollector_CSV_EVENTS;

	public static String DataOverviewCollector_CSV_START;

	public static String DataOverviewCollector_CSV_USER;

	public static String DataOverviewCollector_Data_Overview;

	public static String DataOverviewCollector_events;

	public static String DataOverviewCollector__h4_Data_Overview_h4_;

	public static String DataOverviewCollector_Number_of_Users_;

	public static String DataOverviewCollector_TO_PERIOD_OF_HOURS;

	public static String SummaryCollector_END_DATE;

	public static String SummaryCollector_Number_of_commands_;

	public static String SummaryCollector_Number_of_events_;

	public static String SummaryCollector_Number_of_preference_changes;

	public static String SummaryCollector_Number_of_selections_;

	public static String SummaryCollector_Start_date_;

	public static String SummaryCollector_Summary;

	public static String ViewUsageCollector_View_Usage;
}
