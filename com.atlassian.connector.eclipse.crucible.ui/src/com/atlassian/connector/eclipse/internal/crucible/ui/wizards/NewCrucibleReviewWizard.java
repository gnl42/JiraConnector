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
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient.RemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewChangeJob;
import com.atlassian.connector.eclipse.ui.team.CustomRepository;
import com.atlassian.connector.eclipse.ui.team.ICustomChangesetLogEntry;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.wizards.NewTaskWizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Wizard for creating a new review
 * 
 * @author Thomas Ehrnhoefer
 */
public class NewCrucibleReviewWizard extends NewTaskWizard implements INewWizard {

	private class AddFilesAndPatchesToReviewJob extends CrucibleReviewChangeJob {
		private final Map<CustomRepository, SortedSet<ICustomChangesetLogEntry>> selectedLogEntries;

		private final String patchRepository;

		private final String patch;

		public AddFilesAndPatchesToReviewJob(String name, TaskRepository taskRepository,
				Map<CustomRepository, SortedSet<ICustomChangesetLogEntry>> selectedLogEntries, String patch,
				String patchRepository) {
			super(name, taskRepository);
			this.selectedLogEntries = selectedLogEntries;
			this.patch = patch;
			this.patchRepository = patchRepository;
		}

		@Override
		protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {
			try {
				createdReview = client.execute(new RemoteOperation<Review>(monitor, getTaskRepository()) {
					@Override
					public Review run(CrucibleServerFacade server, CrucibleServerCfg serverCfg, IProgressMonitor monitor)
							throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
						//add revisions
						for (CustomRepository customRepository : selectedLogEntries.keySet()) {
							monitor.beginTask("Adding revisions to repository " + customRepository.getUrl(),
									selectedLogEntries.get(customRepository).size());
							//collect revisions
							ArrayList<String> revisions = new ArrayList<String>();
							for (ICustomChangesetLogEntry logEntry : selectedLogEntries.get(customRepository)) {
								revisions.add(logEntry.getRevision());
							}
							//add revisions to review
							createdReview = server.addRevisionsToReview(serverCfg, createdReview.getPermId(),
									addChangeSetsPage.getRepositoryMappings().get(customRepository).getName(),
									revisions);
						}
						//add patch
						if (patch != null) {
							createdReview = server.addPatchToReview(serverCfg, createdReview.getPermId(),
									patchRepository, patch);
						}
						return createdReview;
					}
				});
			} catch (final CoreException e) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						addChangeSetsPage.setErrorMessage("Could not add revisions to review. See error log for details.");
						StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
								"Error adding revisions to Review", e));
					}
				});
				throw e;
			}
			createdReview = client.getReview(getTaskRepository(),
					CrucibleUtil.getTaskIdFromReview(detailsPage.getReview()), true, monitor);
			return Status.OK_STATUS;
		}
	}

	private CrucibleReviewDetailsPage detailsPage;

	private Review createdReview;

//	private CrucibleAddFilesPage addFilesPage;

	private CrucibleAddChangesetsPage addChangeSetsPage;

	private CrucibleAddPatchPage addPatchPage;

	private final SortedSet<ICustomChangesetLogEntry> preselectedLogEntries;

//	public NewCrucibleReviewWizard(TaskRepository taskRepository, ITaskMapping taskSelection) {
//		super(taskRepository, taskSelection);
//		setWindowTitle("New");
//		setNeedsProgressMonitor(true);
//		this.preselectedLogEntries = new TreeSet<ICustomChangesetLogEntry>();
//	}

	public NewCrucibleReviewWizard(TaskRepository taskRepository, SortedSet<ICustomChangesetLogEntry> selectedLogEntries) {
		super(taskRepository, null);
		setWindowTitle("New");
		setNeedsProgressMonitor(true);
		this.preselectedLogEntries = selectedLogEntries == null ? new TreeSet<ICustomChangesetLogEntry>()
				: selectedLogEntries;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// ignore
		super.init(workbench, selection);
	}

	@Override
	protected ITaskMapping getInitializationData() {
		// ignore
		return super.getInitializationData();
	}

	@Override
	public void addPages() {
		detailsPage = new CrucibleReviewDetailsPage(getTaskRepository());
		addPage(detailsPage);
		/*
		 * The addFilesPage is disabled because adding files to a review is not supported by API.
		 */
//		addFilesPage = new CrucibleAddFilesPage(getTaskRepository());
//		addPage(addFilesPage);
		addChangeSetsPage = new CrucibleAddChangesetsPage(getTaskRepository(), preselectedLogEntries);
		addPage(addChangeSetsPage);

		addPatchPage = new CrucibleAddPatchPage(getTaskRepository());
		addPage(addPatchPage);
	}

	@Override
	public boolean performFinish() {
		//create review if not yet done
		if (createdReview == null) {
			createReview();
		}

		String patch = addPatchPage.hasPatch() ? null : addPatchPage.getPatch();
		String patchRepository = addPatchPage.hasPatch() ? null : addPatchPage.getPatchRepository();
		addFilesAndPatchToReview(addChangeSetsPage.getSelectedLogEntries(), patch, patchRepository);

		final IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				final ITask task = TasksUi.getRepositoryModel().createTask(getTaskRepository(),
						CrucibleUtil.getTaskIdFromPermId(createdReview.getPermId().getId()));
				try {
					TaskData taskData = CrucibleUiUtil.getClient(createdReview).getTaskData(getTaskRepository(),
							task.getTaskId(), monitor);
					CrucibleCorePlugin.getRepositoryConnector().updateTaskFromTaskData(getTaskRepository(), task,
							taskData);
					TasksUiInternal.getTaskList().addTask(task, null);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							TasksUiUtil.openTask(task);
						}
					});
				} catch (CoreException e) {
					// ignore
				}
			}
		};
		try {
			getContainer().run(true, false, runnable);
		} catch (Exception e) {
			detailsPage.setErrorMessage("Could not open created review. Please refresh tasklist.");
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, "Error opening review", e));
		}

		return createdReview != null;
	}

	private void addFilesAndPatchToReview(
			final Map<CustomRepository, SortedSet<ICustomChangesetLogEntry>> selectedLogEntries, final String patch,
			final String patchRepository) {
		if (selectedLogEntries.size() == 0 && patch == null) {
			return;
		}
		final AddFilesAndPatchesToReviewJob job = new AddFilesAndPatchesToReviewJob("Add revisions to review",
				getTaskRepository(), selectedLogEntries, patch, patchRepository);
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				job.run(monitor);
			}
		};
		try {
			getContainer().run(true, true, runnable);
		} catch (Exception e) {
			detailsPage.setErrorMessage("Could not add revisions to review. See error log for details.");
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, "Error adding revisions to Review",
					e));
		}
	}

	private void createReview() {
		detailsPage.applyTo();
		final CrucibleReviewChangeJob job = new CrucibleReviewChangeJob("Create new review", getTaskRepository()) {
			@Override
			protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {
				try {
					createdReview = client.execute(new RemoteOperation<Review>(monitor, getTaskRepository()) {
						@Override
						public Review run(CrucibleServerFacade server, CrucibleServerCfg serverCfg,
								IProgressMonitor monitor) throws CrucibleLoginException, RemoteApiException,
								ServerPasswordNotProvidedException {
							Review tempReview = server.createReview(serverCfg, detailsPage.getReview());
							detailsPage.getReview().setPermId(tempReview.getPermId());
							server.setReviewers(serverCfg, tempReview.getPermId(),
									CrucibleUiUtil.getUserNamesFromUsers(detailsPage.getReviewers()));
							return tempReview;
						}
					});
				} catch (final CoreException e) {
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							detailsPage.setErrorMessage("Could not create review. See error log for details.");
							StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
									"Error creating Review", e));
						}
					});
					throw e;
				}
				createdReview = client.getReview(getTaskRepository(),
						CrucibleUtil.getTaskIdFromReview(detailsPage.getReview()), true, monitor);
				return Status.OK_STATUS;
			}
		};
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				job.run(monitor);
			}
		};
		try {
			getContainer().run(true, true, runnable);
		} catch (Exception e) {
			detailsPage.setErrorMessage("Could not create review. See error log for details.");
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, "Error creating Review", e));
		}
	}
}
