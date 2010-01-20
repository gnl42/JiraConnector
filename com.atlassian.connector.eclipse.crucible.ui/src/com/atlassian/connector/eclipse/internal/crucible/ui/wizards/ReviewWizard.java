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
import com.atlassian.connector.eclipse.internal.core.jobs.JobWithStatus;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.TaskRepositoryUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleRemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleTeamUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewChangeJob;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.AddCommentRemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.AddDecoratedResourcesToReviewJob;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.AddResourcesToReviewJob;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.AddUploadItemsToReviewJob;
import com.atlassian.connector.eclipse.team.ui.AtlassianTeamUiPlugin;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.connector.eclipse.team.ui.ICustomChangesetLogEntry;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector;
import com.atlassian.connector.eclipse.team.ui.LocalStatus;
import com.atlassian.connector.eclipse.team.ui.ScmRepository;
import com.atlassian.connector.eclipse.ui.commons.DecoratedResource;
import com.atlassian.connector.eclipse.ui.commons.ResourceEditorBean;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * Wizard for creating a new review
 * 
 * @author Thomas Ehrnhoefer
 * @author Pawel Niewiadomski
 * @author Jacek Jaroczynski
 */
@SuppressWarnings("restriction")
public class ReviewWizard extends NewTaskWizard implements INewWizard {

	public enum Type {
		ADD_CHANGESET, ADD_PATCH, ADD_WORKSPACE_PATCH, ADD_SCM_RESOURCES, ADD_UPLOAD_ITEMS, ADD_RESOURCES, ADD_COMMENT_TO_FILE;
	}

	private class AddChangesetsToReviewJob extends CrucibleReviewChangeJob {
		private final class AddChangesetsToReviewOperation extends CrucibleRemoteOperation<Review> {
			private AddChangesetsToReviewOperation(IProgressMonitor monitor, TaskRepository taskRepository) {
				super(monitor, taskRepository);
			}

			@Override
			public Review run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
					throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
				SubMonitor submonitor = SubMonitor.convert(monitor);
				Review review = null;
				Map<String, List<String>> repositoriesWithRevisions = MiscUtil.buildHashMap();

				for (SortedSet<ICustomChangesetLogEntry> entries : selectedLogEntries.values()) {
					for (ICustomChangesetLogEntry entry : entries) {
						String[] files = entry.getChangedFiles();
						if (files == null || files.length == 0) {
							continue;
						}
						for (String file : files) {
							Map.Entry<String, String> sourceRepository = TaskRepositoryUtil.getMatchingSourceRepository(
									TaskRepositoryUtil.getScmRepositoryMappings(getTaskRepository()),
									entry.getRepository().getRootPath() + '/' + file);
							if (sourceRepository != null) {
								if (!repositoriesWithRevisions.containsKey(sourceRepository.getValue())) {
									repositoriesWithRevisions.put(sourceRepository.getValue(), new ArrayList<String>());
								}
								repositoriesWithRevisions.get(sourceRepository.getValue()).add(entry.getRevision());
							}
						}
					}
				}

				submonitor.setWorkRemaining(repositoriesWithRevisions.size());
				for (String repository : repositoriesWithRevisions.keySet()) {
					//add changeset to review
					review = server.addRevisionsToReview(serverCfg, crucibleReview.getPermId(), repository,
							repositoriesWithRevisions.get(repository));
					submonitor.worked(1);
				}
				return review;
			}
		}

		private final Map<ScmRepository, SortedSet<ICustomChangesetLogEntry>> selectedLogEntries;

		public AddChangesetsToReviewJob(String name, TaskRepository taskRepository,
				Map<ScmRepository, SortedSet<ICustomChangesetLogEntry>> selectedLogEntries) {
			super(name, taskRepository);
			this.selectedLogEntries = selectedLogEntries;
		}

		@Override
		protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {
			client.execute(new AddChangesetsToReviewOperation(monitor, getTaskRepository()));
			return Status.OK_STATUS;
		}
	}

	private class AddPatchToReviewJob extends CrucibleReviewChangeJob {
		private final String patchRepository;

		private final String patch;

		public AddPatchToReviewJob(String name, TaskRepository taskRepository, String patch, String patchRepository) {
			super(name, taskRepository);
			this.patch = patch;
			this.patchRepository = patchRepository;
		}

		@Override
		protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {
			client.execute(new CrucibleRemoteOperation<Review>(monitor, getTaskRepository()) {
				@Override
				public Review run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
						throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {

					return server.addPatchToReview(serverCfg, crucibleReview.getPermId(), patchRepository, patch);
				}
			});
			return Status.OK_STATUS;
		}
	}

	private CrucibleReviewDetailsPage detailsPage;

	private Review crucibleReview;

	private CrucibleAddChangesetsPage addChangeSetsPage;

	private CrucibleAddPatchPage addPatchPage;

	private WorkspacePatchSelectionPage addWorkspacePatchPage;

	private DefineRepositoryMappingsPage defineMappingPage;

	private ResourceSelectionPage resourceSelectionPage;

	private final Set<Type> types;

	private SortedSet<ICustomChangesetLogEntry> preselectedLogEntries;

	private String previousPatch;

	private String previousPatchRepository;

	private final List<IResource> selectedWorkspaceResources = new ArrayList<IResource>();

	private IResource[] previousWorkspaceSelection;

	private List<UploadItem> uploadItems;

	private List<ResourceEditorBean> versionedCommentsToAdd = new ArrayList<ResourceEditorBean>();

	public ReviewWizard(TaskRepository taskRepository, Set<Type> types) {
		super(taskRepository, null);
		setWindowTitle("New Crucible Review");
		setNeedsProgressMonitor(true);
		this.types = types;
		this.selectedWorkspaceResources.addAll(Arrays.asList((IResource[]) ResourcesPlugin.getWorkspace()
				.getRoot()
				.getProjects()));
	}

	public ReviewWizard(Review review, Set<Type> types) {
		this(CrucibleUiUtil.getCrucibleTaskRepository(review), types);
		this.crucibleReview = review;
	}

	public ReviewWizard(Review review, Type type) {
		this(review, new HashSet<Type>(Arrays.asList(type)));
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
		if (types.contains(Type.ADD_CHANGESET)) {
			addChangeSetsPage = new CrucibleAddChangesetsPage(getTaskRepository(), preselectedLogEntries);
			addPage(addChangeSetsPage);
		}
		if (types.contains(Type.ADD_PATCH)) {
			addPatchPage = new CrucibleAddPatchPage(getTaskRepository());
			addPage(addPatchPage);
		}

		// pre-commit
		if (types.contains(Type.ADD_WORKSPACE_PATCH)) {
			addWorkspacePatchPage = new WorkspacePatchSelectionPage(getTaskRepository(), selectedWorkspaceResources);
			addPage(addWorkspacePatchPage);
		}

		// post-commit for editor selection
		if (types.contains(Type.ADD_SCM_RESOURCES)) {

			if (selectedWorkspaceResources.size() > 0 && selectedWorkspaceResources.get(0) != null) {

				// single SCM integration selection supported
				final ITeamUiResourceConnector teamConnector = AtlassianTeamUiPlugin.getDefault()
						.getTeamResourceManager()
						.getTeamConnector(selectedWorkspaceResources.get(0));
				if (teamConnector == null) {
					MessageDialog.openInformation(getShell(), CrucibleUiPlugin.PRODUCT_NAME,
							"Cannot find Atlassian SCM Integration for '" + selectedWorkspaceResources.get(0).getName()
									+ "'.");
				} else {
					boolean missingMapping = false;
					Collection<String> scmPaths = new ArrayList<String>();
					// TODO use job below if there are plenty of resource (currently it is used for single resource)
					for (IResource resource : selectedWorkspaceResources) {
						try {
							LocalStatus status = teamConnector.getLocalRevision(resource);
							if (status.getScmPath() != null && status.getScmPath().length() > 0) {
								String scmPath = teamConnector.getLocalRevision(resource.getProject()).getScmPath();

								if (TaskRepositoryUtil.getMatchingSourceRepository(
										TaskRepositoryUtil.getScmRepositoryMappings(getTaskRepository()), scmPath) == null) {
									// we need to see mapping page
									missingMapping = true;
									scmPaths.add(scmPath);
								}

							}
						} catch (CoreException e) {
							// resource is probably not under version control
							// skip
						}
					}

					if (missingMapping) {
						defineMappingPage = new DefineRepositoryMappingsPage(scmPaths, getTaskRepository());
						addPage(defineMappingPage);
					}
				}
			}
		}

		// mixed review
		if (types.contains(Type.ADD_RESOURCES)) {
			resourceSelectionPage = new ResourceSelectionPage(getTaskRepository(), selectedWorkspaceResources);
			addPage(resourceSelectionPage);
		}

		//only add details page if review is not already existing
		if (crucibleReview == null) {
			detailsPage = new CrucibleReviewDetailsPage(getTaskRepository(), types.contains(Type.ADD_COMMENT_TO_FILE));
			addPage(detailsPage);
		}
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

		setErrorMessage(null);

		// create review if not yet done, if it fails abort immediately
		if (crucibleReview == null && detailsPage != null) {
			IStatus result = createAndStoreReview(detailsPage.getReview(),
					CrucibleUiUtil.getUsernamesFromUsers(detailsPage.getReviewers()));

			if (!result.isOK()) {
				StatusHandler.log(result);
				setErrorMessage(result.getMessage());
				return false;
			}
		}

		if (detailsPage != null) {
			// save project selection
			CrucibleRepositoryConnector.updateLastSelectedProject(getTaskRepository(), detailsPage.getSelectedProject());

			// save checkbox selections
			CrucibleRepositoryConnector.updateAllowAnyoneOption(getTaskRepository(), detailsPage.isAllowAnyoneToJoin());
			CrucibleRepositoryConnector.updateStartReviewOption(getTaskRepository(),
					detailsPage.isStartReviewImmediately());
		}

		// create patch review
		if (addPatchPage != null) {
			String patchToAdd = addPatchPage.hasPatch() ? addPatchPage.getPatch() : null;
			String patchRepositoryToAdd = addPatchPage.hasPatch() ? addPatchPage.getPatchRepository() : null;

			if (patchToAdd != null && patchRepositoryToAdd != null && !patchToAdd.equals(previousPatch)
					&& !patchRepositoryToAdd.equals(previousPatchRepository)) {
				final JobWithStatus job = new AddPatchToReviewJob("Add patch to review", getTaskRepository(),
						patchToAdd, patchRepositoryToAdd);

				IStatus result = runJobInContainer(job);
				if (result.isOK()) {
					previousPatch = patchToAdd;
					previousPatchRepository = patchRepositoryToAdd;
				} else {
					StatusHandler.log(result);
					setErrorMessage(result.getMessage());
					return false;
				}
			}
		}

		// TODO jj remove unused code
		// create pre-commit review
		if (addWorkspacePatchPage != null) {
			final IResource[] selection = addWorkspacePatchPage.getSelection();

			if (selection != null && selection.length > 0 && !Arrays.equals(selection, previousWorkspaceSelection)
					&& addWorkspacePatchPage.getSelectedTeamResourceConnector() != null) {
				final Collection<UploadItem> uploadItems = new ArrayList<UploadItem>();

				JobWithStatus getItemsJob = new JobWithStatus("Prepare upload items for review") {
					@Override
					public void runImpl(IProgressMonitor monitor) throws CoreException {
						uploadItems.addAll(addWorkspacePatchPage.getSelectedTeamResourceConnector()
								.getUploadItemsForResources(selection, monitor));
					}
				};

				IStatus result = runJobInContainer(getItemsJob);
				if (result.isOK() && uploadItems.size() > 0) {
					final JobWithStatus job = new AddUploadItemsToReviewJob(crucibleReview, uploadItems);

					result = runJobInContainer(job);
					if (result.isOK()) {
						previousWorkspaceSelection = selection;
					} else {
						StatusHandler.log(result);
						setErrorMessage(result.getMessage());
						return false;
					}
				}
			}
		}

		// create review from changeset
		if (addChangeSetsPage != null) {
			final Map<ScmRepository, SortedSet<ICustomChangesetLogEntry>> changesetsToAdd = addChangeSetsPage.getSelectedChangesets();

			if (changesetsToAdd != null && changesetsToAdd.size() > 0) {
				final CrucibleReviewChangeJob job = new AddChangesetsToReviewJob("Add changesets to review",
						getTaskRepository(), changesetsToAdd);

				IStatus result = runJobInContainer(job);
				if (!result.isOK()) {
					StatusHandler.log(result);
					setErrorMessage(result.getMessage());
					return false;
				}
			}
		}

		// create review from editor selection (post-commit)
		if (types.contains(Type.ADD_SCM_RESOURCES)) {
			if (selectedWorkspaceResources != null) {
				final JobWithStatus job = new AddResourcesToReviewJob(crucibleReview,
						selectedWorkspaceResources.toArray(new IResource[selectedWorkspaceResources.size()]));

				IStatus result = runJobInContainer(job);
				if (!result.isOK()) {
					StatusHandler.log(result);
					setErrorMessage(result.getMessage());
					return false;
				}
			}
		}

		// create review from editor selection (pre-commit)
		if (types.contains(Type.ADD_UPLOAD_ITEMS)) {
			if (uploadItems.size() > 0) {
				final JobWithStatus job = new AddUploadItemsToReviewJob(crucibleReview, uploadItems);

				IStatus result = runJobInContainer(job);
				if (!result.isOK()) {
					StatusHandler.log(result);
					setErrorMessage(result.getMessage());
					return false;
				}
			}
		}

		// create review from workbench selection (post- and pre-commit)
		if (resourceSelectionPage != null && types.contains(Type.ADD_RESOURCES)) {
			final List<DecoratedResource> resources = resourceSelectionPage.getSelection();
			if (resources != null && resources.size() > 0) {
				IStatus result = runJobInContainer(new AddDecoratedResourcesToReviewJob(crucibleReview,
						resourceSelectionPage.getTeamResourceConnector(), resources));
				if (!result.isOK()) {
					StatusHandler.log(result);
					setErrorMessage(result.getMessage());
					return false;
				}
			}
		}

		if (crucibleReview != null && types.contains(Type.ADD_COMMENT_TO_FILE) && detailsPage.getComment().length() > 0) {
			final String commentText = detailsPage.getComment();
			JobWithStatus job = new CrucibleReviewChangeJob("Add versioned comments to review", getTaskRepository()) {
				@Override
				protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {
					for (ResourceEditorBean resourceEditor : versionedCommentsToAdd) {
						CrucibleFile crucibleFile = CrucibleTeamUiUtil.getCrucibleFileFromResource(
								resourceEditor.getResource(), crucibleReview);
						AddCommentRemoteOperation operation = new AddCommentRemoteOperation(getTaskRepository(),
								crucibleReview, client, crucibleFile, commentText, monitor);
						operation.setCommentLines(resourceEditor.getLineRange());
						client.execute(operation);
					}

					return Status.OK_STATUS;
				}
			};

			IStatus result = runJobInContainer(job);
			if (!result.isOK()) {
				StatusHandler.log(result);
				setErrorMessage(result.getMessage());
				return false;
			}
		}

		EnumSet<CrucibleAction> crucibleActions = null;
		try {
			crucibleActions = crucibleReview.getActions();
		} catch (ValueNotYetInitialized e) {
			StatusHandler.log(new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID, "Failed to get allowed actions",
					e));
		}

		if (crucibleActions == null) {
			crucibleActions = EnumSet.noneOf(CrucibleAction.class);
		}

		if (crucibleReview != null && detailsPage != null && detailsPage.isStartReviewImmediately()
				&& crucibleActions.contains(CrucibleAction.SUBMIT)) {

			IStatus result = startAndUpdateReview();
			if (!result.isOK()) {
				StatusHandler.log(result);
				setErrorMessage(result.getMessage());
				return false;
			}

			if (crucibleActions.contains(CrucibleAction.APPROVE)) {
				result = approveAndUpdateReview();
				if (!result.isOK()) {
					StatusHandler.log(result);
					setErrorMessage(result.getMessage());
					return false;
				}
			}
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
		} catch (InvocationTargetException e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, "Unable to refresh review.", e));
		} catch (InterruptedException e) {
			// don't care
		}

		return true;
	}

	private IStatus startAndUpdateReview() {
		final JobWithStatus startReviewJob = new CrucibleReviewChangeJob("Start review immediately",
				getTaskRepository()) {
			@Override
			protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {
				client.execute(new CrucibleRemoteOperation<Review>(monitor, getTaskRepository()) {
					@Override
					public Review run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
							throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
						return server.submitReview(serverCfg, crucibleReview.getPermId());
					}
				});
				return Status.OK_STATUS;
			}
		};

		return runJobInContainer(startReviewJob);
	}

	private IStatus approveAndUpdateReview() {
		// if possible, approve review
		final CrucibleReviewChangeJob approveReviewJob = new CrucibleReviewChangeJob("Approve Review",
				getTaskRepository()) {
			@Override
			protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {
				client.execute(new CrucibleRemoteOperation<Review>(monitor, getTaskRepository()) {
					@Override
					public Review run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
							throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
						return server.approveReview(serverCfg, crucibleReview.getPermId());
					}
				});
				return Status.OK_STATUS;
			}
		};

		return runJobInContainer(approveReviewJob);
	}

	private IStatus runJobInContainer(final JobWithStatus job) {
		IStatus status = runInJobContainer0(job);
		if (status.isOK()) {
			runInJobContainer0(new CrucibleReviewChangeJob("Refresh Review", getTaskRepository(), false, false) {
				@Override
				protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {
					// Update review after every change because otherwise some data will be missing (in this case we miss actions)
					crucibleReview = client.getReview(getTaskRepository(),
							CrucibleUtil.getTaskIdFromReview(crucibleReview), true, monitor);
					return Status.OK_STATUS;
				}
			});
		}
		return status;
	}

	private IStatus runInJobContainer0(final JobWithStatus job) {
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

	private IStatus createAndStoreReview(final Review newReview, final Set<String> reviewers) {
		final CrucibleReviewChangeJob job = new CrucibleReviewChangeJob("Create new review", getTaskRepository()) {
			@Override
			protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {
				SubMonitor submonitor = SubMonitor.convert(monitor, "Create new review", 2);

				Review updatedReview = client.execute(new CrucibleRemoteOperation<Review>(submonitor.newChild(1),
						getTaskRepository()) {
					@Override
					public Review run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
							throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
						Review tempReview = server.createReview(serverCfg, newReview);
						if (tempReview != null) {
							server.setReviewers(serverCfg, tempReview.getPermId(), reviewers);
						}
						return tempReview;
					}
				});

				if (updatedReview == null) {
					// WTF? No error and null
					return new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, "Server didn't return review");
				}

				crucibleReview = client.getReview(getTaskRepository(), CrucibleUtil.getTaskIdFromReview(updatedReview),
						true, submonitor.newChild(1));

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
			return new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, "Error creating Review", e);
		}

		return job.getStatus();
	}

	private void setErrorMessage(String message) {
		IWizardPage page = getContainer().getCurrentPage();
		if (page instanceof WizardPage) {
			((WizardPage) page).setErrorMessage(message != null ? message.replace("\n", " ") : null);
		}
	}

	public void setLogEntries(SortedSet<ICustomChangesetLogEntry> logEntries) {
		this.preselectedLogEntries = logEntries;
	}

	public void setRoots(List<IResource> list) {
		this.selectedWorkspaceResources.clear();
		this.selectedWorkspaceResources.addAll(list);
	}

	public void setUploadItems(List<UploadItem> uploadItems) {
		this.uploadItems = uploadItems;
	}

	public void setFilesCommentData(List<ResourceEditorBean> list) {
		this.versionedCommentsToAdd = list;
	}

}
