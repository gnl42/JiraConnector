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

package org.eclipse.mylyn.internal.monitor.reports;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Mik Kersten
 */
public class MonitorReportsPlugin extends AbstractUIPlugin {

	public static final String REPORT_SUMMARY_ID = "org.eclipse.mylyn.monitor.ui.reports.ui.actions.monitorSummaryReport";

	public static final String REPORT_USERS_ID = "org.eclipse.mylyn.monitor.ui.reports.ui.actions.monitorUsersReport";

	public static final String SHARED_TASK_DATA_ROOT_DIR = "org.eclipse.mylyn.monitor.ui.reports.preferences.sharedTaskDataRootDir";

	public static final String ID_PLUGIN = "org.eclipse.mylyn.monitor.ui.reports";

	private static MonitorReportsPlugin plugin;

	public MonitorReportsPlugin() {
		plugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	public static MonitorReportsPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.mylyn.internal.monitor.reports", path);
	}

	/**
	 * Returns the root directory of the shared location where task data files are stored. Returns "" if the preference
	 * has not been set.
	 */
	public String getRootSharedDataDirectory() {
		return getPreferenceStore().getString(SHARED_TASK_DATA_ROOT_DIR);
	}
}
