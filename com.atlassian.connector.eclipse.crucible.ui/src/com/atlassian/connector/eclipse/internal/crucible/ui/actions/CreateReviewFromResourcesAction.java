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

import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.CrucibleRepositorySelectionWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.RepositorySelectionWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.ReviewWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.SelectCrucibleVersionOrNewerRepositoryPage;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.SelectCrucibleRepositoryPage;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector;
import com.atlassian.connector.eclipse.ui.commons.ResourceEditorBean;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.widgets.Shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Jacek Jaroczynski
 */
@SuppressWarnings("restriction")
public class CreateReviewFromResourcesAction extends AbstractReviewFromResourcesAction {

	public CreateReviewFromResourcesAction() {
		super("Create Review Action");
	}

	protected void openReviewWizard(final ResourceEditorBean selection, final ITeamUiResourceConnector connector,
			boolean isPostCommit, final Shell shell) {

		if (isPostCommit) {
			SelectCrucibleVersionOrNewerRepositoryPage selectRepositoryPage = new SelectCrucibleVersionOrNewerRepositoryPage() {
				@Override
				protected IWizard createWizard(TaskRepository taskRepository) {
					ReviewWizard wizard = new ReviewWizard(taskRepository, MiscUtil.buildHashSet(
							ReviewWizard.Type.ADD_SCM_RESOURCES, ReviewWizard.Type.ADD_COMMENT_TO_FILE));
					wizard.setRoots(connector, Arrays.asList(selection.getResource()));
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
				wizard.setFilesCommentData(Arrays.asList(selection));
				wd = new WizardDialog(shell, wizard);
			}
			wd.setBlockOnOpen(true);
			wd.open();
		}
	}

	protected void openReviewWizard(final ITeamUiResourceConnector teamConnector, final List<IResource> resources,
			boolean isCrucible21Required, Shell shell) {

		if (isCrucible21Required) {
			SelectCrucibleVersionOrNewerRepositoryPage selectRepositoryPage = new SelectCrucibleVersionOrNewerRepositoryPage() {
				@Override
				protected IWizard createWizard(TaskRepository taskRepository) {
					ReviewWizard wizard = new ReviewWizard(taskRepository,
							MiscUtil.buildHashSet(ReviewWizard.Type.ADD_RESOURCES));
					wizard.setRoots(teamConnector, resources);
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
					wizard.setRoots(teamConnector, resources);
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
				reviewWizard.setRoots(teamConnector, resources);
				wd = new WizardDialog(shell, reviewWizard);
			}
			wd.setBlockOnOpen(true);
			wd.open();
		}
	}

}
