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
import com.atlassian.connector.eclipse.ui.actions.AbstractResourceAction;
import com.atlassian.connector.eclipse.ui.commons.ResourceEditorBean;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("restriction")
public class CreateReviewFromResourcesAction extends AbstractResourceAction implements IActionDelegate {

	public CreateReviewFromResourcesAction() {
		super("Create Review Action");
	}

	// TODO jj test with subversive

	// TODO jj selection tree not visible if only one file???

	// TODO jj extended test after adapter refactoring
	// TODO jj code review after refactoring

	@Override
	protected void processResources(@NotNull List<ResourceEditorBean> selection, final Shell shell) {
		if (!TeamUiUtils.checkTeamConnectors()) {
			// no connectors at all
			return;
		}

		final Set<ITeamUiResourceConnector> connectors = new HashSet<ITeamUiResourceConnector>();

		// svn support only
		for (ResourceEditorBean resourceBean : selection) {
			ITeamUiResourceConnector connector = AtlassianTeamUiPlugin.getDefault()
					.getTeamResourceManager()
					.getTeamConnector(resourceBean.getResource());

			String message = null;

			if (connector == null) {
				message = "Cannot find Atlassian SCM Integration for '" + resourceBean.getResource().getName() + "'.";
			} else if (connector.getType() != TeamConnectorType.SVN) {
				message = "Cannot create review from non Subversion resource. Only Subversion is supported.";
			}

			if (message != null) {
				MessageDialog.openInformation(shell, CrucibleUiPlugin.PLUGIN_ID, message);
				return;
			}

			connectors.add(connector);
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

	private void openReviewWizard(final ResourceEditorBean selection, final ITeamUiResourceConnector connector,
			boolean isPostCommit, final Shell shell) {

		if (isPostCommit) {
			SelectCrucible21RepositoryPage selectRepositoryPage = new SelectCrucible21RepositoryPage() {
				@Override
				protected IWizard createWizard(TaskRepository taskRepository) {
					ReviewWizard wizard = new ReviewWizard(taskRepository, MiscUtil.buildHashSet(
							ReviewWizard.Type.ADD_SCM_RESOURCES, ReviewWizard.Type.ADD_COMMENT_TO_FILE));
					wizard.setRoots(Arrays.asList(selection.getResource()));
					wizard.setFilesCommentData(Arrays.asList(selection));
					return wizard;
				}
			};

			WizardDialog wd = new WizardDialog(WorkbenchUtil.getShell(), new CrucibleRepositorySelectionWizard(
					selectRepositoryPage));
			wd.setBlockOnOpen(true);
			wd.open();

		} else {

			Collection<UploadItem> uploadItems;
			try {
				uploadItems = connector.getUploadItemsForResources(new IResource[] { selection.getResource() },
						new NullProgressMonitor());
			} catch (CoreException e) {
				handleError(shell, "Cannot create UploadItem for '" + selection.getResource().getName() + "'.");
				return;
			}

			final List<UploadItem> items = new ArrayList<UploadItem>(uploadItems);

			SelectCrucibleRepositoryPage selectRepositoryPage = new SelectCrucibleRepositoryPage(
					SelectCrucibleRepositoryPage.ENABLED_CRUCIBLE_REPOSITORY_FILTER) {
				@Override
				protected IWizard createWizard(TaskRepository taskRepository) {
					ReviewWizard wizard = new ReviewWizard(taskRepository, MiscUtil.buildHashSet(
							ReviewWizard.Type.ADD_UPLOAD_ITEMS, ReviewWizard.Type.ADD_COMMENT_TO_FILE));

					wizard.setUploadItems(items);
					wizard.setFilesCommentData(Arrays.asList(selection));
					return wizard;
				}
			};

			// skip repository selection wizard page if there is only one repository on the list
			List<TaskRepository> taskRepositories = selectRepositoryPage.getTaskRepositories();
			WizardDialog wd = null;
			if (taskRepositories.size() != 1) {
				wd = new WizardDialog(shell, new RepositorySelectionWizard(selectRepositoryPage));
			} else {
				ReviewWizard wizard = new ReviewWizard(taskRepositories.get(0), MiscUtil.buildHashSet(
						ReviewWizard.Type.ADD_UPLOAD_ITEMS, ReviewWizard.Type.ADD_COMMENT_TO_FILE));
				wizard.setUploadItems(items);
				wd = new WizardDialog(shell, wizard);
			}
			wd.setBlockOnOpen(true);
			wd.open();
		}
	}

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
						openReviewWizard(resourcesList, isCrucible21Required[0], shell);
					}
				});

				return Status.OK_STATUS;
			}
		};

		analyzeResource.setUser(true);
		analyzeResource.schedule();
	}

	private void handleError(final Shell shell, String message) {
		StatusHandler.log(new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID, message));
		MessageDialog.openInformation(shell, CrucibleUiPlugin.PLUGIN_ID, message);
	}

	private void openReviewWizard(final List<IResource> resources, boolean isCrucible21Required, Shell shell) {

		if (isCrucible21Required) {
			SelectCrucible21RepositoryPage selectRepositoryPage = new SelectCrucible21RepositoryPage() {
				@Override
				protected IWizard createWizard(TaskRepository taskRepository) {
					ReviewWizard wizard = new ReviewWizard(taskRepository,
							MiscUtil.buildHashSet(ReviewWizard.Type.ADD_RESOURCES));
					wizard.setRoots(resources);
					return wizard;
				}
			};

			WizardDialog wd = new WizardDialog(shell, new CrucibleRepositorySelectionWizard(selectRepositoryPage));
			wd.setBlockOnOpen(true);
			wd.open();

		} else {

			SelectCrucibleRepositoryPage selectRepositoryPage = new SelectCrucibleRepositoryPage(
					SelectCrucibleRepositoryPage.ENABLED_CRUCIBLE_REPOSITORY_FILTER) {
				@Override
				protected IWizard createWizard(TaskRepository taskRepository) {
					ReviewWizard wizard = new ReviewWizard(taskRepository,
							MiscUtil.buildHashSet(ReviewWizard.Type.ADD_RESOURCES));
					wizard.setRoots(resources);
					return wizard;
				}
			};

			// skip repository selection wizard page if there is only one repository on the list
			List<TaskRepository> taskRepositories = selectRepositoryPage.getTaskRepositories();
			WizardDialog wd = null;
			if (taskRepositories.size() != 1) {
				wd = new WizardDialog(shell, new RepositorySelectionWizard(selectRepositoryPage));
			} else {
				ReviewWizard reviewWizard = new ReviewWizard(taskRepositories.get(0),
						MiscUtil.buildHashSet(ReviewWizard.Type.ADD_RESOURCES));
				reviewWizard.setRoots(resources);
				wd = new WizardDialog(shell, reviewWizard);
			}
			wd.setBlockOnOpen(true);
			wd.open();
		}
	}

	@Override
	protected void selectionChanged(IAction action, List<ResourceEditorBean> selection) {
	}
}
