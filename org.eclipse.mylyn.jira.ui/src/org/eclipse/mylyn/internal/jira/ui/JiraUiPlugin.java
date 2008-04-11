/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.mylyn.internal.jira.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.ui.TaskRepositoryLocationUiFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 * @author Steffen Pingel
 */
public class JiraUiPlugin extends AbstractUIPlugin {

	public static final String ID_PLUGIN = "org.eclipse.mylyn.jira.ui";

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
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.mylyn.jira", path);
	}

	public JiraUiPlugin() {
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		reg.put("icons/obj16/comment.gif", getImageDescriptor("icons/obj16/comment.gif"));
		reg.put("icons/obj16/jira.png", getImageDescriptor("icons/obj16/jira.png"));
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		JiraClientFactory.getDefault().setTaskRepositoryLocationFactory(new TaskRepositoryLocationUiFactory(), false);
		TasksUiPlugin.getRepositoryManager().addListener(JiraClientFactory.getDefault());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		TasksUiPlugin.getRepositoryManager().removeListener(JiraClientFactory.getDefault());
		JiraClientFactory.getDefault().logOutFromAll();
		instance = null;
		super.stop(context);
	}

}
