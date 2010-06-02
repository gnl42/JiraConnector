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

package com.atlassian.connector.eclipse.internal.jira.ui;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.mylyn.tasks.ui.TaskRepositoryLocationUiFactory;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.atlassian.connector.eclipse.internal.commons.ui.MigrateToSecureStorageJob;
import com.atlassian.connector.eclipse.internal.jira.core.IJiraConstants;
import com.atlassian.connector.eclipse.internal.jira.core.JiraClientFactory;
import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;

/**
 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 * @author Steffen Pingel
 */
public class JiraUiPlugin extends AbstractUIPlugin {

	public static final String ID_PLUGIN = "com.atlassian.connector.eclipse.jira.ui"; //$NON-NLS-1$

	public static final String PRODUCT_NAME = "Atlassian JIRA Connector"; //$NON-NLS-1$

	private static JiraUiPlugin instance;

	public static JiraUiPlugin getDefault() {
		return instance;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.mylyn.jira", path); //$NON-NLS-1$
	}

	public JiraUiPlugin() {
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		reg.put("icons/obj16/comment.gif", getImageDescriptor("icons/obj16/comment.gif")); //$NON-NLS-1$ //$NON-NLS-2$
		reg.put("icons/obj16/jira.png", getImageDescriptor("icons/obj16/jira.png")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		JiraClientFactory.getDefault().setTaskRepositoryLocationFactory(new TaskRepositoryLocationUiFactory(), false);
		TasksUi.getRepositoryManager().addListener(JiraClientFactory.getDefault());

		if (!getPreferenceStore().getBoolean(IJiraConstants.PREFERENCE_SECURE_STORAGE_MIGRATED)) {
			Job migrateJob = new MigrateToSecureStorageJob(JiraCorePlugin.CONNECTOR_KIND);
			migrateJob.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					super.done(event);
					getPreferenceStore().setValue(IJiraConstants.PREFERENCE_SECURE_STORAGE_MIGRATED, Boolean.TRUE);
				}
			});
			migrateJob.schedule();
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		TasksUi.getRepositoryManager().removeListener(JiraClientFactory.getDefault());
		JiraClientFactory.getDefault().logOutFromAll();
		instance = null;
		super.stop(context);
	}

}
