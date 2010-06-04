/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.commons.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.progress.UIJob;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

public class MigrateToSecureStorageJob extends UIJob {

	private final String kind;

	public MigrateToSecureStorageJob(String kind) {
		super("Migrating passwords to secure storage");
		this.kind = kind;
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		Set<TaskRepository> repos = TasksUiPlugin.getRepositoryManager().getRepositories(kind);
		if (repos != null) {
			for (TaskRepository repo : repos) {
				migrateToSecureStorage(repo);
			}
		}
		return Status.OK_STATUS;
	}

	@SuppressWarnings({ "deprecation", "restriction" })
	public static boolean migrateToSecureStorage(TaskRepository repository) {
		if (repository.getProperty(ITasksCoreConstants.PROPERTY_USE_SECURE_STORAGE) == null
				&& !"local".equals(repository.getUrl())) { //$NON-NLS-1$
			try {
				AuthenticationCredentials creds = repository.getCredentials(AuthenticationType.REPOSITORY), httpCreds = repository.getCredentials(AuthenticationType.HTTP), proxyCreds = repository.getCredentials(AuthenticationType.PROXY);
				boolean savePassword = repository.getSavePassword(AuthenticationType.REPOSITORY), httpSavePassword = repository.getSavePassword(AuthenticationType.HTTP), proxySavePassword = repository.getSavePassword(AuthenticationType.PROXY);

				repository.setProperty(ITasksCoreConstants.PROPERTY_USE_SECURE_STORAGE, "true"); //$NON-NLS-1$

				if (creds != null) {
					repository.setCredentials(AuthenticationType.REPOSITORY, creds, savePassword);
				}

				if (httpCreds != null) {
					repository.setCredentials(AuthenticationType.HTTP, httpCreds, httpSavePassword);
				}

				if (proxyCreds != null) {
					repository.setCredentials(AuthenticationType.PROXY, proxyCreds, proxySavePassword);
				}

				try {
					Platform.flushAuthorizationInfo(new URL(repository.getUrl()), "", "Basic"); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (MalformedURLException ex) {
					Platform.flushAuthorizationInfo(new URL("http://eclipse.org/mylyn"), repository.getUrl(), "Basic"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				return true;
			} catch (Exception e) {
				StatusHandler.log(new Status(IStatus.ERROR, ITasksCoreConstants.ID_PLUGIN, NLS.bind(
						"Could not migrate credentials to secure storage for {0}", repository.getUrl()), e));
			}
		}
		return false;
	}

}
