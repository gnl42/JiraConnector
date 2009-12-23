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

package com.atlassian.connector.eclipse.internal.crucible.ui.actions;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.CrucibleRepositorySelectionWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.RepositorySelectionWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.ReviewWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.SelectCrucible21RepositoryPage;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.SelectCrucibleRepositoryPage;
import com.atlassian.connector.eclipse.team.ui.AtlassianTeamUiPlugin;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector;
import com.atlassian.connector.eclipse.team.ui.TeamConnectorType;
import com.atlassian.connector.eclipse.team.ui.TeamUiUtils;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector.State;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.internal.ui.actions.TeamAction;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("restriction")
public class CreateReviewFromResourcesAction extends TeamAction {

	@Override
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		if (!TeamUiUtils.checkTeamConnectors()) {
			// no connectors at all
			return;
		}

		final IResource[] resources = getSelectedResources();

		final Set<ITeamUiResourceConnector> connectors = new HashSet<ITeamUiResourceConnector>();

		// svn support only
		for (IResource resource : resources) {
			ITeamUiResourceConnector connector = AtlassianTeamUiPlugin.getDefault()
					.getTeamResourceManager()
					.getTeamConnector(resource);

			connectors.add(connector);

			String message = null;

			if (connector == null) {
				message = "Cannot find Atlassian SCM Integration for '" + resource.getName() + "'.";
			} else if (connector.getType() != TeamConnectorType.SVN) {
				message = "Cannot create review from non Subversion resource. Only Subversion is supported.";
			}

			if (message != null) {
				MessageDialog.openInformation(getShell(), CrucibleUiPlugin.PLUGIN_ID, message);
				return;
			}
		}

		if (connectors.size() > 1) {
			MessageDialog.openInformation(getShell(), CrucibleUiPlugin.PLUGIN_ID,
					"Cannot create review for more than one SCM provider at once.");
			return;
		} else if (connectors.size() == 0) {
			MessageDialog.openInformation(getShell(), CrucibleUiPlugin.PLUGIN_ID,
					"Cannot create review. No Atlassian SCM provider found.");
			return;
		}

		final boolean[] isCrucible21Required = { false };

		Job analyzeResource = new Job("Analyzing selected resources") {

			public IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Analyzing selected resources", IProgressMonitor.UNKNOWN);
				try {
					ITeamUiResourceConnector teamConnector = connectors.iterator().next();
					Collection<IResource> allResources = teamConnector.getResourcesByFilterRecursive(resources,
							ITeamUiResourceConnector.State.SF_ALL);
					for (IResource resource : allResources) {
						if (teamConnector.isResourceAcceptedByFilter(resource, State.SF_VERSIONED)
								&& !teamConnector.isResourceAcceptedByFilter(resource, State.SF_ANY_CHANGE)) {
							// versioned and committed file found (without any local change)
							// we need Crucible 2.1 to add such file to the review
							isCrucible21Required[0] = true;
							break;
						}
					}
				} finally {
					monitor.done();
				}

				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						openReviewWizard(resources, isCrucible21Required[0]);
					}
				});

				return Status.OK_STATUS;
			}
		};

		analyzeResource.setUser(true);
		analyzeResource.schedule();

	}

	private void openReviewWizard(final IResource[] resources, boolean isCrucible21Required) {

		// TODO jj no wizard if there is single repository

		if (isCrucible21Required) {
			SelectCrucible21RepositoryPage selectRepositoryPage = new SelectCrucible21RepositoryPage() {
				@Override
				protected IWizard createWizard(TaskRepository taskRepository) {
					ReviewWizard wizard = new ReviewWizard(taskRepository,
							MiscUtil.buildHashSet(ReviewWizard.Type.ADD_RESOURCES));
					wizard.setRoots(Arrays.asList(resources));
					return wizard;
				}
			};

			WizardDialog wd = new WizardDialog(WorkbenchUtil.getShell(), new CrucibleRepositorySelectionWizard(
					selectRepositoryPage));
			wd.setBlockOnOpen(true);
			wd.open();

		} else {
			SelectCrucibleRepositoryPage selectRepositoryPage = new SelectCrucibleRepositoryPage(
					SelectCrucibleRepositoryPage.ENABLED_CRUCIBLE_REPOSITORY_FILTER) {
				@Override
				protected IWizard createWizard(TaskRepository taskRepository) {
					ReviewWizard wizard = new ReviewWizard(taskRepository,
							MiscUtil.buildHashSet(ReviewWizard.Type.ADD_RESOURCES));
					wizard.setRoots(Arrays.asList(resources));
					return wizard;
				}
			};

			WizardDialog wd = new WizardDialog(WorkbenchUtil.getShell(), new RepositorySelectionWizard(
					selectRepositoryPage));
			wd.setBlockOnOpen(true);
			wd.open();
		}

	}

}
