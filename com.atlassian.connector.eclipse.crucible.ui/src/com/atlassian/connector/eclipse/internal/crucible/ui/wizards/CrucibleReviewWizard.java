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
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleRemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewChangeJob;
import com.atlassian.connector.eclipse.ui.team.ICustomChangesetLogEntry;
import com.atlassian.connector.eclipse.ui.team.RepositoryInfo;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
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
public class CrucibleReviewWizard extends NewTaskWizard implements INewWizard {

	public enum Type {
		EMPTY, ADD_CHANGESET, ADD_PATCH, ALL, UNDEFINED;
	}

	private class AddFilesAndPatchesToReviewJob extends CrucibleReviewChangeJob {
		private final Map<RepositoryInfo, SortedSet<ICustomChangesetLogEntry>> selectedLogEntries;

		private final String patchRepository;

		private final String patch;

		public AddFilesAndPatchesToReviewJob(String name, TaskRepository taskRepository,
				Map<RepositoryInfo, SortedSet<ICustomChangesetLogEntry>> selectedLogEntries, String patch,
				String patchRepository) {
			super(name, taskRepository);
			this.selectedLogEntries = selectedLogEntries;
			this.patch = patch;
			this.patchRepository = patchRepository;
		}

		@Override
		protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {
			try {
				crucibleReview = client.execute(new CrucibleRemoteOperation<Review>(monitor, getTaskRepository()) {
					@Override
					public Review run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
							throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
						//add revisions
						if (selectedLogEntries != null) {
							for (RepositoryInfo repository : selectedLogEntries.keySet()) {
								monitor.beginTask("Adding revisions to repository " + repository.getScmPath(),
										selectedLogEntries.get(repository).size());
								//collect revisions
								ArrayList<String> revisions = new ArrayList<String>();
								for (ICustomChangesetLogEntry logEntry : selectedLogEntries.get(repository)) {
									revisions.add(logEntry.getRevision());
								}
								//add revisions to review
								crucibleReview = server.addRevisionsToReview(serverCfg, crucibleReview.getPermId(),
										addChangeSetsPage.getRepositoryMappings().get(repository).getName(), revisions);
							}
						}
						//add patch
						if (patch != null && patch.length() > 0 && patchRepository != null
								&& patchRepository.length() > 0 && !patchAddedToReview) {
							crucibleReview = server.addPatchToReview(serverCfg, crucibleReview.getPermId(),
									patchRepository, patch);
							patchAddedToReview = true;
						}
						return crucibleReview;
					}
				});
			} catch (final CoreException e) {
				creationProcessStatus.add(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
						"Error adding revisions to Review", e));
				throw e;
			}
			String taskID;
			if (crucibleReview != null) {
				taskID = CrucibleUtil.getTaskIdFromReview(crucibleReview);
			} else if (detailsPage != null) {
				taskID = CrucibleUtil.getTaskIdFromReview(detailsPage.getReview());
			} else {
				taskID = null;
			}
			if (taskID != null) {
				crucibleReview = client.getReview(getTaskRepository(), taskID, true, monitor);
			}
			return Status.OK_STATUS;
		}
	}

	private boolean patchAddedToReview;

	private CrucibleReviewDetailsPage detailsPage;

	private Review crucibleReview;

	private MultiStatus creationProcessStatus;

//	private CrucibleAddFilesPage addFilesPage;

	private CrucibleAddChangesetsPage addChangeSetsPage;

	private CrucibleAddPatchPage addPatchPage;

	private final SortedSet<ICustomChangesetLogEntry> preselectedLogEntries;

	private Map<RepositoryInfo, SortedSet<ICustomChangesetLogEntry>> changesetsToAdd;

	private String patchRepositoryToAdd;

	private String patchToAdd;

	private Type wizardType = Type.UNDEFINED;

	private CrucibleTypeSelectionPage typeSelectionPage;

	public CrucibleReviewWizard(TaskRepository taskRepository) {
		this(taskRepository, null, Type.UNDEFINED);
	}

	public CrucibleReviewWizard(TaskRepository taskRepository, SortedSet<ICustomChangesetLogEntry> selectedLogEntries) {
		this(taskRepository, selectedLogEntries, selectedLogEntries != null ? Type.ADD_CHANGESET : Type.UNDEFINED);
	}

	public CrucibleReviewWizard(TaskRepository taskRepository, SortedSet<ICustomChangesetLogEntry> selectedLogEntries,
			Type type) {
		super(taskRepository, null);
		setWindowTitle("New Crucible Review");
		setNeedsProgressMonitor(true);
		this.preselectedLogEntries = selectedLogEntries == null ? new TreeSet<ICustomChangesetLogEntry>()
				: selectedLogEntries;
		wizardType = type;
	}

	public CrucibleReviewWizard(Review review, Type type) {
		this(CrucibleUiUtil.getCrucibleTaskRepository(review), null, type);
		this.crucibleReview = review;
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
		//do not use page selection if wizard type is already set
		if (wizardType == Type.UNDEFINED) {
			typeSelectionPage = new CrucibleTypeSelectionPage();
			addPage(typeSelectionPage);
		}
		if (wizardType == Type.UNDEFINED) {
			wizardType = Type.ALL;
		}
		/*
		 * The addFilesPage is disabled because adding files to a review is not supported by API.
		 */
//		addFilesPage = new CrucibleAddFilesPage(getTaskRepository());
//		(addFilesPage);
		if (wizardType == Type.ADD_CHANGESET || wizardType == Type.ALL) {
			addChangeSetsPage = new CrucibleAddChangesetsPage(getTaskRepository(), preselectedLogEntries, this);
			addPage(addChangeSetsPage);
		}

		if (wizardType == Type.ADD_PATCH || wizardType == Type.ALL) {
			addPatchPage = new CrucibleAddPatchPage(getTaskRepository(), this);
			addPage(addPatchPage);
		}
		//only add details page if review is not already existing
		if (crucibleReview == null) {
			detailsPage = new CrucibleReviewDetailsPage(getTaskRepository(), this);
			addPage(detailsPage);
		}
	}

	private boolean patchAlreadyAdded() {
		if (patchToAdd == null || patchRepositoryToAdd == null) {
			return false;
		}
		if (addPatchPage == null || addPatchPage.getPatch() == null || addPatchPage.getPatchRepository() == null) {
			return false;
		}
		return patchToAdd.equals(addPatchPage.getPatch())
				&& patchRepositoryToAdd.equals(addPatchPage.getPatchRepository());
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page instanceof CrucibleTypeSelectionPage) {
			wizardType = ((CrucibleTypeSelectionPage) page).getType();
			if (wizardType == Type.ADD_CHANGESET || wizardType == Type.ALL) {
				return addChangeSetsPage;
			} else if (wizardType == Type.ADD_PATCH) {
				return addPatchPage;
			} else if (wizardType == Type.EMPTY) {
				return detailsPage;
			}
		}
		if (page instanceof CrucibleAddChangesetsPage) {
			if (wizardType == Type.ALL) {
				return addPatchPage;
			}
			return detailsPage;
		}
		if (page instanceof CrucibleAddPatchPage) {
			return detailsPage;
		}
		return null;
	}

	@Override
	public boolean canFinish() {
		if (detailsPage != null) {
			return detailsPage.isPageComplete();
		}
		return super.canFinish();
	}

	@Override
	public boolean performFinish() {
		creationProcessStatus = new MultiStatus(CrucibleUiPlugin.PLUGIN_ID, IStatus.OK,
				"Error during review creation. See error log for details.", null);
		IWizardPage currentPage = getContainer().getCurrentPage();
		if (currentPage instanceof WizardPage) {
			((WizardPage) currentPage).setErrorMessage(null);
		}
		//create review if not yet done
		if (crucibleReview == null && detailsPage != null) {
			createReview();
			if (crucibleReview == null) {
				if (currentPage instanceof WizardPage) {
					((WizardPage) currentPage).setErrorMessage(creationProcessStatus.getMessage());
					StatusHandler.log(creationProcessStatus);
				}
				return false;
			}
		}

		//if patch has changed, add it again
		if (!patchAlreadyAdded()) {
			patchAddedToReview = false;
		}
		if (addPatchPage != null) {
			patchToAdd = addPatchPage.hasPatch() ? addPatchPage.getPatch() : null;
			patchRepositoryToAdd = addPatchPage.hasPatch() ? addPatchPage.getPatchRepository() : null;
		}
		if (addChangeSetsPage != null) {
			changesetsToAdd = addChangeSetsPage.getSelectedChangesets();
		}
		if (wizardType == Type.ADD_CHANGESET || wizardType == Type.ADD_PATCH) {
			addFilesAndPatchToReview(changesetsToAdd, patchToAdd, patchRepositoryToAdd);
		}
		try {
			if (crucibleReview != null && detailsPage != null && detailsPage.startImmediately()
					&& crucibleReview.getActions().contains(CrucibleAction.SUBMIT)) {
				startReview();
			}
		} catch (ValueNotYetInitialized e1) {
			//ignore
		}

		if (creationProcessStatus.getSeverity() != IStatus.OK) {
			if (currentPage instanceof WizardPage) {
				((WizardPage) currentPage).setErrorMessage(creationProcessStatus.getMessage());
				StatusHandler.log(creationProcessStatus);
			}
			return false;
		}

		final IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				final ITask task = TasksUi.getRepositoryModel().createTask(getTaskRepository(),
						CrucibleUtil.getTaskIdFromPermId(crucibleReview.getPermId().getId()));
				try {
					TaskData taskData = CrucibleUiUtil.getClient(crucibleReview).getTaskData(getTaskRepository(),
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
			if (currentPage instanceof WizardPage) {
				((WizardPage) currentPage).setErrorMessage("Could not open created review. Refresh tasklist.");
			}
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, "Error opening review", e));
		}
		return crucibleReview != null && creationProcessStatus.getSeverity() == IStatus.OK;
	}

	private void startReview() {
		try {
			//submit review
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					CrucibleReviewChangeJob job = new CrucibleReviewChangeJob("submit review", getTaskRepository()) {
						@Override
						protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {
							try {
								crucibleReview = client.execute(new CrucibleRemoteOperation<Review>(monitor,
										getTaskRepository()) {
									@Override
									public Review run(CrucibleServerFacade2 server, ConnectionCfg serverCfg,
											IProgressMonitor monitor) throws CrucibleLoginException,
											RemoteApiException, ServerPasswordNotProvidedException {
										return server.submitReview(serverCfg, crucibleReview.getPermId());
									}
								});
							} catch (final CoreException e) {
								creationProcessStatus.add(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
										"Error starting review", e));
								throw e;
							}
							crucibleReview = client.getReview(getTaskRepository(),
									CrucibleUtil.getTaskIdFromReview(detailsPage.getReview()), true, monitor);

							return Status.OK_STATUS;
						}
					};
					job.run(monitor);
				}
			});
			//if possible, approve review
			if (crucibleReview.getActions().contains(CrucibleAction.APPROVE)) {
				getContainer().run(true, true, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						CrucibleReviewChangeJob job = new CrucibleReviewChangeJob("approve review", getTaskRepository()) {
							@Override
							protected IStatus execute(CrucibleClient client, IProgressMonitor monitor)
									throws CoreException {
								try {
									crucibleReview = client.execute(new CrucibleRemoteOperation<Review>(monitor,
											getTaskRepository()) {
										@Override
										public Review run(CrucibleServerFacade2 server, ConnectionCfg serverCfg,
												IProgressMonitor monitor) throws CrucibleLoginException,
												RemoteApiException, ServerPasswordNotProvidedException {
											return server.approveReview(serverCfg, crucibleReview.getPermId());
										}
									});
								} catch (final CoreException e) {
									creationProcessStatus.add(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
											"Error starting review", e));
									throw e;
								}
								crucibleReview = client.getReview(getTaskRepository(),
										CrucibleUtil.getTaskIdFromReview(detailsPage.getReview()), true, monitor);
								return Status.OK_STATUS;
							}
						};
						job.run(monitor);
					}
				});

			}
		} catch (ValueNotYetInitialized e) {
			// ignore
		} catch (Exception e) {
			creationProcessStatus.add(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, "Error starting review", e));
		}
	}

	private void addFilesAndPatchToReview(
			final Map<RepositoryInfo, SortedSet<ICustomChangesetLogEntry>> selectedLogEntries, final String patch,
			final String patchRepository) {
		if ((selectedLogEntries == null || selectedLogEntries.size() == 0) && patch == null) {
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
			creationProcessStatus.add(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
					"Error adding revisions to review", e));
		}
	}

	private void createReview() {
		detailsPage.applyTo();
		final CrucibleReviewChangeJob job = new CrucibleReviewChangeJob("Create new review", getTaskRepository()) {
			@Override
			protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {
				try {
					crucibleReview = client.execute(new CrucibleRemoteOperation<Review>(monitor, getTaskRepository()) {
						@Override
						public Review run(CrucibleServerFacade2 server, ConnectionCfg serverCfg,
								IProgressMonitor monitor) throws CrucibleLoginException, RemoteApiException,
								ServerPasswordNotProvidedException {
							Review tempReview = server.createReview(serverCfg, detailsPage.getReview());
							detailsPage.getReview().setPermId(tempReview.getPermId());
							server.setReviewers(serverCfg, tempReview.getPermId(),
									CrucibleUiUtil.getUsernamesFromUsers(detailsPage.getReviewers()));
							return tempReview;
						}
					});
				} catch (final CoreException e) {
					creationProcessStatus.add(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
							"Error creating review", e));
					throw e;
				}
				crucibleReview = client.getReview(getTaskRepository(),
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
			creationProcessStatus.add(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, "Error creating Review", e));
		}
	}

	public void updateCache(WizardPage currentPage) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
				CrucibleClient client = connector.getClientManager().getClient(getTaskRepository());
				if (client != null) {
					try {
						client.updateRepositoryData(monitor, getTaskRepository());
					} catch (CoreException e) {
						StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
								"Failed to update repository data", e));
					}
				}
			}
		};
		try {
			getContainer().run(true, true, runnable);
		} catch (Exception ex) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, "Failed to update repository data",
					ex));
		}
		if (!CrucibleUiUtil.hasCachedData(getTaskRepository())) {
			currentPage.setErrorMessage("Could not retrieve available projects and users from server.");
		}
	}
}
