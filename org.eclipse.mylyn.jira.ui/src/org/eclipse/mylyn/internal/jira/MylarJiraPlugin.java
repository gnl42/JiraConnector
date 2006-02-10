/*******************************************************************************
 * Copyright (c) 2006 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class for the Jira integration plugin
 * 
 * @author Wesley Coelho (initial integration patch)
 * @author Mik Kersten
 */
public class MylarJiraPlugin extends AbstractUIPlugin {

	private static MylarJiraPlugin plugin;

	public final static String JIRA_REPOSITORY_KIND = "jira";

	public final static String JIRA_CLIENT_LABEL = "JIRA";

	public final static String TITLE_MESSAGE_DIALOG = "Mylar JIRA Client";

	/** Repository address + Issue Prefix + Issue key = the issue's web address */
	public final static String ISSUE_URL_PREFIX = "/browse/";

	/** Repository address + Filter Prefix + Issue key = the filter's web address */
	public final static String FILTER_URL_PREFIX = "/secure/IssueNavigator.jspa?mode=hide&requestId=";

	public MylarJiraPlugin() {
		plugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);

	}

	public void stop(BundleContext context) throws Exception {

		JiraServerFacade.getDefault().logOut();

		super.stop(context);
		plugin = null;
	}

	public static MylarJiraPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.mylar.sandbox.jira", path);
	}
}
