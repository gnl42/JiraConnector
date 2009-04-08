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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient.RemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewChangeJob;
import com.atlassian.connector.eclipse.ui.team.CustomChangeSetLogEntry;
import com.atlassian.connector.eclipse.ui.team.CustomRepository;
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
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.NewTaskWizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;
import java.util.SortedSet;

/**
 * Wizard for creating a new review
 * 
 * @author Thomas Ehrnhoefer
 */
public class NewCrucibleReviewWizard extends NewTaskWizard implements INewWizard {
	private CrucibleReviewDetailsPage detailsPage;

	private Review createdReview;

	private CrucibleAddFilesPage addFilesPage;

	private CrucibleAddChangesetsPage addChangeSetsPage;

	public NewCrucibleReviewWizard(TaskRepository taskRepository, ITaskMapping taskSelection) {
		super(taskRepository, taskSelection);
		setWindowTitle("New");
		setNeedsProgressMonitor(true);
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
		addChangeSetsPage = new CrucibleAddChangesetsPage(getTaskRepository());
		addPage(addChangeSetsPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page instanceof CrucibleReviewDetailsPage) {
			createReview();
		}
		return super.getNextPage(page);
	}

	@Override
	public boolean performFinish() {
		//create review if not yet done
		if (createdReview == null) {
			createReview();
		}

		addFilesToReview(addChangeSetsPage.getSelectedLogEntries());

//		//add files if there are some to add
//		addFilesPage.getFileInfo...
//		
//		vorher: page fuer changesets zeug und andre page raus daweil

		//TODO open editor....needs some work since task is not yet available (backgorund sync job needs to finish

		return createdReview != null;
	}

	private void addFilesToReview(final Map<CustomRepository, SortedSet<CustomChangeSetLogEntry>> selectedLogEntries) {
		if (selectedLogEntries.size() == 0) {
			return;
		}
		final CrucibleReviewChangeJob job = new CrucibleReviewChangeJob("Add revisions to review", getTaskRepository()) {
			@Override
			protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {
				try {
					createdReview = client.execute(new RemoteOperation<Review>(monitor, getTaskRepository()) {
						@Override
						public Review run(CrucibleServerFacade server, CrucibleServerCfg serverCfg,
								IProgressMonitor monitor) throws CrucibleLoginException, RemoteApiException,
								ServerPasswordNotProvidedException {
							for (CustomRepository customRepository : selectedLogEntries.keySet()) {
								monitor.beginTask("Adding revisions to repository " + customRepository.getUrl(),
										selectedLogEntries.get(customRepository).size());
								//collect revisions
								ArrayList<String> revisions = new ArrayList<String>();
								for (CustomChangeSetLogEntry logEntry : selectedLogEntries.get(customRepository)) {
									revisions.add(logEntry.getRevision());
								}
								//add revisions to review
								createdReview = server.addRevisionsToReview(serverCfg, createdReview.getPermId(),
										addChangeSetsPage.getRepositoryMappings().get(customRepository).getName(),
										revisions);
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
		};
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
