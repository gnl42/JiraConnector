/*******************************************************************************
 * Copyright (c) 2007 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.core;

import java.io.File;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylar.internal.jira.core.service.JiraAuthenticationException;
import org.eclipse.mylar.internal.jira.core.service.JiraException;
import org.eclipse.mylar.internal.jira.core.service.JiraRemoteMessageException;
import org.eclipse.mylar.internal.jira.core.service.JiraServiceUnavailableException;
import org.eclipse.mylar.tasks.core.IMylarStatusConstants;
import org.eclipse.mylar.tasks.core.MylarStatus;
import org.eclipse.mylar.tasks.core.RepositoryStatus;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.osgi.framework.BundleContext;

/**
 * @author Brock Janiczak
 */
public class JiraCorePlugin extends Plugin {
	public static final String ID = "org.eclipse.mylar.internal.jira.core"; //$NON-NLS-1$

	// The shared instance.
	private static JiraCorePlugin plugin;

	// Resource bundle.
	private ResourceBundle resourceBundle;

	private JiraClientManager serverManager;

	/**
	 * The constructor.
	 */
	public JiraCorePlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		File serverCache = getStateLocation().append("serverCache").toFile(); //$NON-NLS-1$

		// Turn off logging for the Attachment check. We don't want or need soap
		// with attachments
		Logger logger = Logger.getLogger("org.apache.axis.utils.JavaUtils");
		logger.setLevel(Level.SEVERE);

		serverManager = new JiraClientManager(serverCache);
		serverManager.start();
	}

// /**
// * @return
// */
// private PasswordPrompter getPasswordPrompter() {
// return new PasswordPrompter() {
//
// public String getPassword(URL baseURL, String username) {
// Map<?, ?> authenticationInfo = Platform.getAuthorizationInfo(baseURL, "jira",
// "");
// // String username = (String)
// // authenticationInfo.get("com.gbst.jira.core.username");
// // //$NON-NLS-1$
// String password = (String)
// authenticationInfo.get("com.gbst.jira.core.password"); //$NON-NLS-1$
// return password;
// }
//
// };
// }

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);

		if (serverManager != null) {
			serverManager.stop();
		}
		plugin = null;
		resourceBundle = null;
		serverManager = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static JiraCorePlugin getDefault() {
		return plugin;
	}

	public JiraClientManager getServerManager() {
		return serverManager;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = JiraCorePlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle.getBundle("com.gbst.jira.core.JiraCorePluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}

	public static void log(int severity, String message, Throwable e) {
		getDefault().getLog().log(new Status(severity, ID, -1, message, e));
	}

	public static IStatus toStatus(TaskRepository repository, Throwable e) {
		String url = repository.getUrl();
		if (e instanceof JiraAuthenticationException) {
			return RepositoryStatus.createLoginError(url, ID);
		} else if (e instanceof JiraServiceUnavailableException) {
			return new RepositoryStatus(url, IStatus.ERROR, ID, IMylarStatusConstants.IO_ERROR, e.getMessage(), e);
		} else if (e instanceof JiraRemoteMessageException) {
			return RepositoryStatus.createHtmlStatus(url, IStatus.ERROR, ID, IMylarStatusConstants.REPOSITORY_ERROR, e
					.getMessage(), ((JiraRemoteMessageException) e).getHtmlMessage());
		} else if (e instanceof JiraException) {
			return new RepositoryStatus(url, IStatus.ERROR, ID, IMylarStatusConstants.REPOSITORY_ERROR, e.getMessage(), e);
		} else {
			return MylarStatus.createInternalError(ID, "Unexpected error", e);
		}
	}

}
