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

package com.atlassian.connector.eclipse.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.security.storage.EncodingUtils;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

public class MigrateToSecureStorageJob extends Job {

	private final String kind;

	public MigrateToSecureStorageJob(String kind) {
		super("Migrating passwords to secure storage");
		this.kind = kind;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		Set<TaskRepository> repos = TasksUiPlugin.getRepositoryManager().getRepositories(kind);
		if (repos != null) {
			for (TaskRepository repo : repos) {
				migrateToSecureStorage(repo);
			}
		}
		return Status.OK_STATUS;
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	public static boolean migrateToSecureStorage(TaskRepository repository) {
		if (repository.getProperty(ITasksCoreConstants.PROPERTY_USE_SECURE_STORAGE) == null
				&& !repository.getUrl().equals("local")) { //$NON-NLS-1$
			try {
				Map<String, String> map = Platform.getAuthorizationInfo(new URL(repository.getUrl()), "", "Basic");//$NON-NLS-1$ //$NON-NLS-2$
				if (map != null) {
					ISecurePreferences securePreferences = SecurePreferencesFactory.getDefault().node(
							ITasksCoreConstants.ID_PLUGIN);
					securePreferences = securePreferences.node(EncodingUtils.encodeSlashes(repository.getUrl()));
					for (String key : map.keySet()) {
						String value = map.get(key);
						if (value != null) {
							securePreferences.put(key, value, key.endsWith(".password")); //$NON-NLS-1$
						}
					}
					try {
						Platform.flushAuthorizationInfo(new URL(repository.getUrl()), "", "Basic"); //$NON-NLS-1$ //$NON-NLS-2$
					} catch (MalformedURLException ex) {
						Platform.flushAuthorizationInfo(
								new URL("http://eclipse.org/mylyn"), repository.getUrl(), "Basic"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				repository.setProperty(ITasksCoreConstants.PROPERTY_USE_SECURE_STORAGE, "true"); //$NON-NLS-1$ 
				return true;
			} catch (Exception e) {
				StatusHandler.log(new Status(IStatus.ERROR, ITasksCoreConstants.ID_PLUGIN, NLS.bind(
						"Could not migrate credentials to secure storage for {0}", repository.getUrl()), e));
			}
		}
		return false;
	}
}
