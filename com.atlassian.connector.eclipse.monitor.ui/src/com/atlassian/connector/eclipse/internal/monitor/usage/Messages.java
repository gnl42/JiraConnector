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

package com.atlassian.connector.eclipse.internal.monitor.usage;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.atlassian.connector.eclipse.internal.monitor.usage.messages"; //$NON-NLS-1$

	public static String MonitorUiPlugin_failed_to_start;

	public static String EnabledMonitoringNoticeDialog_learn_more;

	public static String EnabledMonitoringNoticeDialog_to_disable;

	public static String EnabledMonitoringNoticeDialog_title;

	public static String EnabledMonitoringNoticeDialog_please_consider_uploading;

	public static String UsageDataPreferencePage_collectors;

	public static String UsageDataPreferencePage_description;

	public static String UsageDataPreferencePage_sent_to_following_recipients;

	public static String UsageDataPreferencePage_submission_every;

	public static String UsageDataPreferencePage_days;

	public static String UsageDataPreferencePage_monitoring_and_submission;

	public static String UsageDataPreferencePage_enable_monitoring;

	public static String UsageSubmissionWizard_title;

	public static String UsageSummaryEditorWizardPage_title;

	public static String UsageSummaryEditorWizardPage_description;

	public static String UsageSummaryEditorWizardPage_this_will_run_in_background;

	public static String UsageSummaryEditorWizardPage_show_usage_for;

	public static String UsageSummaryEditorWizardPage_use_of_perspectives;

	public static String UsageSummaryEditorWizardPage_use_of_views;

	public static String UsageUploadWizardPage_page_title;

	public static String UsageUploadWizardPage_title;

	public static String UsageUploadWizardPage_description;

	public static String UsageUploadWizardPage_recipients;

	public static String UsageUploadWizardPage_usage_file_location;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
