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

import com.atlassian.connector.eclipse.internal.core.AtlassianCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.CrucibleRepositorySelectionWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.ReviewWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.SelectCrucible21RepositoryPage;
import com.atlassian.connector.eclipse.team.ui.AtlassianTeamUiPlugin;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.team.internal.ui.actions.TeamAction;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

@SuppressWarnings("restriction")
public class CreatePostCommitReviewFileAction extends TeamAction {

	// TODO jj change action label (needs merge with single file post-commit review creation?)

	@Override
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {

		final IResource[] resources = getSelectedResources();

		if (resources != null && resources.length > 0) {
			Job createReview = new LocalProceedWithReviewCreationJob(resources);
			createReview.setUser(true);
			createReview.schedule();
		} else {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
					"Nothing selected. Cannot create review."));
		}
	}

	private void showDirtyFilesMessage() {
		MessageBox mb = new MessageBox(WorkbenchUtil.getShell(), SWT.OK | SWT.ICON_INFORMATION);
		mb.setText(AtlassianCorePlugin.PRODUCT_NAME);
		mb.setMessage("Your selection contains at least one uncommitted change.\nPlease commit your changes first.");
		mb.open();
	}

	private void openReviewWizard(final IResource[] resources) {
		SelectCrucible21RepositoryPage selectRepositoryPage = new SelectCrucible21RepositoryPage() {
			@Override
			protected IWizard createWizard(TaskRepository taskRepository) {
				ReviewWizard wizard = new ReviewWizard(taskRepository,
						MiscUtil.buildHashSet(ReviewWizard.Type.ADD_SCM_RESOURCES));
				wizard.setRoots(Arrays.asList(resources));
				return wizard;
			}
		};

		WizardDialog wd = new WizardDialog(WorkbenchUtil.getShell(), new CrucibleRepositorySelectionWizard(
				selectRepositoryPage));
		wd.setBlockOnOpen(true);
		wd.open();
	}

	public class LocalProceedWithReviewCreationJob extends Job {

		private final IResource[] resources;

		public LocalProceedWithReviewCreationJob(IResource[] resources) {
			super("Create Review Job");
			this.resources = resources;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Checking workspace changes", resources.length);
			try {

				// TODO jj empty directories should not proceed

				for (IResource root : resources) {
					final ITeamUiResourceConnector teamConnector = AtlassianTeamUiPlugin.getDefault()
							.getTeamResourceManager()
							.getTeamConnector(resources[0]);
					// TODO jj test it against different connectors (subersion and CVS at once)
					if (teamConnector != null) {
						if (teamConnector.getResourcesByFilterRecursive(new IResource[] { root },
								ITeamUiResourceConnector.State.SF_ANY_CHANGE).size() > 0) {
							// uncommitted change detected, show message box and return
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									showDirtyFilesMessage();
								}
							});
							return Status.OK_STATUS;
						}
					} else {
						StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
								"Cannot find team connector for selected resources."));
						return Status.OK_STATUS;
					}
					if (monitor.isCanceled()) {
						return Status.OK_STATUS;
					}
					monitor.worked(1);
				}
			} finally {
				monitor.done();
			}

			// there are no dirty files, proceed with processing
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					openReviewWizard(resources);
				}
			});
			return Status.OK_STATUS;
		}
	}
}
