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

package com.atlassian.connector.eclipse.internal.monitor.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.atlassian.connector.eclipse.internal.monitor.core.messages"; //$NON-NLS-1$

	public static String MonitorCorePlugin_failed_to_start;

	public static String MonitorCorePlugin_cant_create_log_file;

	public static String UsageDataUploadJob_no_network;

	public static String UsageDataUploadJob_unknown_exception;

	public static String UsageDataUploadJob_error_uploading;

	public static String UsageDataUploadJob_invalid_uid;

	public static String UsageDataUploadJob_proxy_authentication;

	public static String UsageDataUploadJob_30;

	public static String UsageDataUploadJob_uploading_usage_stats;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
