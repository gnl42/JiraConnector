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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleVersionInfo;
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
 * @author Wojciech Seliga
 */
public class CrucibleRepositorySelectionWizard extends RepositorySelectionWizard {

	public CrucibleRepositorySelectionWizard(SelectCrucible21RepositoryPage page) {
		super(page);
		page.setWizard(this);
	}

	public void updateRepoVersion(WizardPage currentPage, final TaskRepository repo) {
		final CrucibleVersionInfo[] crucibleVersionInfo = new CrucibleVersionInfo[1];
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
				CrucibleClient client = connector.getClientManager().getClient(repo);
				if (client != null) {
					monitor.beginTask("Retrieving version of " + repo.getRepositoryLabel(), IProgressMonitor.UNKNOWN);
					try {
						crucibleVersionInfo[0] = client.updateVersionInfo(monitor, repo);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			}
		};
		try {
			getContainer().run(true, true, runnable);
		} catch (Exception ex) {
			currentPage.setErrorMessage("Failed to retrieve repository version");
			StatusHandler.log(new Status(IStatus.INFO, CrucibleUiPlugin.PLUGIN_ID,
					"Failed to retrieve repository version", ex));
		}
	}

}
