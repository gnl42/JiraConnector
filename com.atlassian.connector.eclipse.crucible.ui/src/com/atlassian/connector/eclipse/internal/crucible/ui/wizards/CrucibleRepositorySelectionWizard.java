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

package com.atlassian.connector.eclipse.internal.crucible.ui.wizards;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleRemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleVersionInfo;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Jacek Jaroczynski
 */
public class CrucibleRepositorySelectionWizard extends RepositorySelectionWizard {

	public CrucibleRepositorySelectionWizard(SelectCrucible21RepositoryPage page) {
		super(page);
		page.setWizard(this);
	}

	public void updateRepoVersion(WizardPage currentPage, final TaskRepository repo) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
				CrucibleClient client = connector.getClientManager().getClient(repo);
				if (client != null) {
					monitor.beginTask("Retrieving version of " + repo.getRepositoryLabel(), IProgressMonitor.UNKNOWN);
					try {
						boolean isVersion21orGreater = client.execute(new CrucibleRemoteOperation<Boolean>(monitor,
								repo) {

							@Override
							public Boolean run(CrucibleServerFacade2 server, ConnectionCfg serverCfg,
									IProgressMonitor monitor) throws RemoteApiException,
									ServerPasswordNotProvidedException {
								CrucibleVersionInfo version = server.getServerVersion(serverCfg);
								return version.isVersion21OrGreater();
							}
						});

						repo.setProperty(SelectCrucible21RepositoryPage.IS_VERSION_2_1,
								String.valueOf(isVersion21orGreater));
					} catch (CoreException e) {
						StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
								"Failed to retrieve repository version for " + repo.getRepositoryLabel(), e));
					}
				}
			}
		};
		try {
			getContainer().run(true, true, runnable);
		} catch (Exception ex) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
					"Failed to retrieve repository version", ex));
		}
		if (repo.getProperty(SelectCrucible21RepositoryPage.IS_VERSION_2_1) == null) {
			currentPage.setErrorMessage("Failed to retrieve repository version");
		}
	}

}
