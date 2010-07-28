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
import com.atlassian.connector.eclipse.team.ui.AtlassianTeamUiPlugin;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector.State;
import com.atlassian.connector.eclipse.ui.actions.AbstractResourceAction;
import com.atlassian.connector.eclipse.ui.commons.ResourceEditorBean;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractReviewFromResourcesAction extends AbstractResourceAction {

	protected AbstractReviewFromResourcesAction(String text) {
		super(text);
	}

	@Override
	protected void processResources(@NotNull List<ResourceEditorBean> selection, final Shell shell) {

		final Set<ITeamUiResourceConnector> connectors = new HashSet<ITeamUiResourceConnector>();

		for (ResourceEditorBean resourceBean : selection) {
			ITeamUiResourceConnector connector = AtlassianTeamUiPlugin.getDefault()
					.getTeamResourceManager()
					.getTeamConnector(resourceBean.getResource());

			if (connector != null) {
				connectors.add(connector);
			} else {
				connectors.add(new LocalTeamResourceConnector());
			}
		}

		if (connectors.size() > 1) {
			MessageDialog.openInformation(shell, CrucibleUiPlugin.PLUGIN_ID,
					"Cannot create review for more than one SCM provider at once.");
			return;
		} else if (connectors.size() == 0) {
			MessageDialog.openInformation(shell, CrucibleUiPlugin.PLUGIN_ID,
					"Cannot create review. No Atlassian SCM provider found.");
			return;
		}

		if (selection.size() > 1
				|| (selection.size() == 1 && selection.get(0) != null && selection.get(0).getLineRange() == null)) {
			// process workbench selection
			processWorkbenchSelection(selection, connectors.iterator().next(), shell);
		} else if (selection.size() == 1 && selection.get(0) != null && selection.get(0).getLineRange() != null) {
			// process editor selection
			processEditorSelection(selection.get(0), connectors.iterator().next(), shell);
		} else {
			// we should not be here
			handleError(shell, "Cannot determine selection.");
			return;
		}

	}

	private void processEditorSelection(ResourceEditorBean selection, ITeamUiResourceConnector connector, Shell shell) {
		boolean isPostCommit = false;

		if (connector.isResourceAcceptedByFilter(selection.getResource(), State.SF_VERSIONED)
				&& !connector.isResourceAcceptedByFilter(selection.getResource(), State.SF_ANY_CHANGE)) {
			// versioned and committed file found (without any local change)
			// we need Crucible 2.1 to add such file to the review
			isPostCommit = true;
		}

		openReviewWizard(selection, connector, isPostCommit, shell);
	}

	protected abstract void openReviewWizard(ResourceEditorBean selection, ITeamUiResourceConnector connector,
			boolean isPostCommit, Shell shell);

	private void processWorkbenchSelection(List<ResourceEditorBean> selection,
			final ITeamUiResourceConnector teamConnector, final Shell shell) {

		final List<IResource> resourcesList = new ArrayList<IResource>();

		for (ResourceEditorBean resultBean : selection) {
			resourcesList.add(resultBean.getResource());
		}

		final IResource[] resourcesArray = resourcesList.toArray(new IResource[resourcesList.size()]);

		Job analyzeResource = new Job("Analyzing selected resources") {

			public IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Analyzing selected resources for Crucible compatibility", IProgressMonitor.UNKNOWN);

				final boolean[] isCrucible21Required = { false };

				try {
					Collection<IResource> allResources = teamConnector.getResourcesByFilterRecursive(resourcesArray,
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
						openReviewWizard(teamConnector, resourcesList, isCrucible21Required[0], shell);
					}
				});

				return Status.OK_STATUS;
			}
		};

		analyzeResource.setUser(true);
		analyzeResource.schedule();
	}

	@Override
	protected boolean updateSelection(IStructuredSelection structuredSelection) {
		if (!super.updateSelection(structuredSelection)) {
			return false;
		}

		List<ResourceEditorBean> selection = getSelectionData();

		if (selection == null) {
			return false;
		}

		for (ResourceEditorBean resourceBean : selection) {
			IResource resource = resourceBean.getResource();
			if (resource instanceof IProject && !((IProject) resource).isOpen()) {
				return false;
			}
		}

		return true;
	}

	protected void handleError(final Shell shell, String message) {
		StatusHandler.log(new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID, message));
		MessageDialog.openInformation(shell, CrucibleUiPlugin.PLUGIN_ID, message);
	}

	protected abstract void openReviewWizard(final ITeamUiResourceConnector teamConnector,
			final List<IResource> resources, boolean isCrucible21Required, Shell shell);

}
