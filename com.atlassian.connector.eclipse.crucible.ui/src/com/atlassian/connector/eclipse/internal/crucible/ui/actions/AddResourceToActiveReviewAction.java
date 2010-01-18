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

import com.atlassian.connector.eclipse.internal.core.jobs.JobWithStatus;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.AddDecoratedResourcesToReviewJob;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.RefreshReviewAndTaskListJob;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.CrucibleRepositorySelectionWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.RepositorySelectionWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.ResourceSelectionPage;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.ReviewWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.SelectCrucible21RepositoryPage;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.SelectCrucibleRepositoryPage;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.ResourceSelectionPage.DecoratedResource;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector;
import com.atlassian.connector.eclipse.ui.commons.ResourceEditorBean;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.widgets.Shell;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Action to add a file to the active review
 * 
 * @author Shawn Minto
 * @author Thomas Ehrnhoefer
 * @author Pawel Niewiadomski
 */
public class AddResourceToActiveReviewAction extends AbstractReviewFromResourcesAction {

	public AddResourceToActiveReviewAction() {
		super("Add Files to Active Review");
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (!super.updateSelection(selection)) {
			return false;
		}

		Review review = getActiveReview();
		if (selection == null || selection.size() == 0 || review == null) {
			return false;
		}

		try {
			if (!getActiveReview().getActions().contains(CrucibleAction.MODIFY_FILES)) {
				return false;
			}
		} catch (ValueNotYetInitialized e) {
			// don't care, just enable
		}
		return true;
	}

	protected Review getActiveReview() {
		return CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
	}

	@SuppressWarnings("restriction")
	protected void openReviewWizard(final ResourceEditorBean selection, final ITeamUiResourceConnector connector,
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

	protected void openReviewWizard(final List<IResource> resources, boolean isCrucible21Required, Shell shell) {
		WizardDialog wd = null;
		Wizard wizard = new Wizard() {
			private ResourceSelectionPage resourceSelectionPage;

			@Override
			public void addPages() {
				resourceSelectionPage = new ResourceSelectionPage(
						CrucibleUiUtil.getCrucibleTaskRepository(getActiveReview()), resources);
				addPage(resourceSelectionPage);
			}

			@Override
			public boolean performFinish() {
				setErrorMessage(null);

				final List<DecoratedResource> resources = resourceSelectionPage.getSelection();
				if (resources != null && resources.size() > 0) {
					Review review = getActiveReview();
					IStatus result = runJobInContainer(new AddDecoratedResourcesToReviewJob(review,
							resourceSelectionPage.getTeamResourceConnector(), resources));
					if (!result.isOK()) {
						StatusHandler.log(result);
						setErrorMessage(result.getMessage());
						return false;
					} else {
						runJobInContainer(new RefreshReviewAndTaskListJob(review));
					}
				}

				return true;
			}

			private void setErrorMessage(String message) {
				IWizardPage page = getContainer().getCurrentPage();
				if (page instanceof WizardPage) {
					((WizardPage) page).setErrorMessage(message != null ? message.replace("\n", " ") : null);
				}
			}

			private IStatus runJobInContainer(final JobWithStatus job) {
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						job.run(monitor);
					}
				};
				try {
					getContainer().run(true, true, runnable);
				} catch (Exception e) {
					return new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, String.format("Job \"%s\" failed",
							job.getName()), e);
				}
				return job.getStatus();
			}

		};
		wd = new WizardDialog(shell, wizard);
		wd.setBlockOnOpen(true);
		wd.open();
	}

	/*final AddResourcesToReviewJob job = new AddResourcesToReviewJob(getActiveReview(), getSelectedResources());
	job.addJobChangeListener(new JobChangeAdapter() {
		@Override
		public void done(IJobChangeEvent event) {
			final IStatus status = job.getStatus();
			if (!status.isOK()) {
				StatusHandler.log(status);

				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						MessageBox mb = new MessageBox(WorkbenchUtil.getShell(), SWT.OK | SWT.ICON_INFORMATION);
						mb.setText(AtlassianCorePlugin.PRODUCT_NAME);
						String message = NLS.bind(
								"Failed to add selected resources to active review. Error message was: \n\n{0}",
								status.getMessage());
						if (status.getMessage().contains("does not exist")) {
							message += "\n\nCheck if your mappings are correct in:\n"
									+ "Preferences -> Atlassian -> Repository Mappings.";
						}
						mb.setMessage(message);
						mb.open();
					}
				});
			} else {

			}
		}
	});
	job.setPriority(Job.INTERACTIVE);
	job.schedule();*/
}
