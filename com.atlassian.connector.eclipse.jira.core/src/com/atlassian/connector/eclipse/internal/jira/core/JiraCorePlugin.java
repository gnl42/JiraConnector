/*******************************************************************************
 * Copyright (c) 2004, 2009 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.axis.AxisEngine;
import org.apache.axis.AxisProperties;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;

import com.atlassian.connector.eclipse.internal.jira.core.service.JiraAuthenticationException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraRemoteMessageException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraServiceUnavailableException;

/**
 * @author Brock Janiczak
 */
public class JiraCorePlugin extends Plugin {

	public static final String ID_PLUGIN = "com.atlassian.connector.eclipse.internal.jira.core"; //$NON-NLS-1$

	private static JiraCorePlugin plugin;

	private static JiraClientManager clientManager;

	public final static String CONNECTOR_KIND = "jira"; //$NON-NLS-1$

	public final static String LABEL = NLS.bind(Messages.JiraCorePlugin_JIRA_description, "3.4"); //$NON-NLS-1$

	private static boolean initialized;

	/**
	 * The constructor.
	 */
	public JiraCorePlugin() {
		super();
		plugin = this;

		// disable Axis attachment support, see bug 197819
		AxisProperties.setProperty(AxisEngine.PROP_ATTACHMENT_IMPLEMENTATION, "org.eclipse.mylyn.does.not.exist"); //$NON-NLS-1$
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		File serverCache = getStateLocation().append("serverCache").toFile(); //$NON-NLS-1$
		initialize(serverCache);
	}

	public static void initialize(File serverCacheDirectory) {
		if (initialized) {
			throw new IllegalStateException("Already initialized"); //$NON-NLS-1$
		}
		initialized = true;

		// Turn off logging for the Attachment check. We don't want or need soap
		// with attachments
		Logger logger = Logger.getLogger("org.apache.axis.utils.JavaUtils"); //$NON-NLS-1$
		logger.setLevel(Level.SEVERE);

		clientManager = new JiraClientManager(serverCacheDirectory);
		clientManager.start();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);

		if (clientManager != null) {
			clientManager.stop();
		}
		plugin = null;
		clientManager = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static JiraCorePlugin getDefault() {
		return plugin;
	}

	public static JiraClientManager getClientManager() {
		if (!initialized) {
			throw new IllegalStateException("Not yet initialized"); //$NON-NLS-1$
		}
		return clientManager;
	}

	public static IStatus toStatus(TaskRepository repository, Throwable e) {
		String url = repository.getRepositoryUrl();
		if (e instanceof JiraAuthenticationException) {
			return RepositoryStatus.createLoginError(url, ID_PLUGIN);
		} else if (e instanceof JiraServiceUnavailableException) {
			return new RepositoryStatus(url, IStatus.ERROR, ID_PLUGIN, RepositoryStatus.ERROR_IO, e.getMessage(), e);
		} else if (e instanceof JiraRemoteMessageException) {
			return RepositoryStatus.createHtmlStatus(url, IStatus.ERROR, ID_PLUGIN, RepositoryStatus.ERROR_REPOSITORY,
					e.getMessage(), ((JiraRemoteMessageException) e).getHtmlMessage());
		} else if (e instanceof JiraException) {
			return new RepositoryStatus(url, IStatus.ERROR, ID_PLUGIN, RepositoryStatus.ERROR_REPOSITORY,
					e.getMessage(), e);
		} else {
			return RepositoryStatus.createInternalError(ID_PLUGIN, "Unexpected error", e); //$NON-NLS-1$
		}
	}

}
