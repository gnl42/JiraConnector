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

package com.atlassian.connector.eclipse.internal.crucible.ui;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.connector.eclipse.internal.core.client.RemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleClientManager;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClientData;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.OpenVirtualFileJob;
import com.atlassian.connector.eclipse.internal.crucible.ui.util.EditorUtil;
import com.atlassian.connector.eclipse.team.ui.AtlassianTeamUiPlugin;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector;
import com.atlassian.connector.eclipse.team.ui.TeamUiResourceManager;
import com.atlassian.connector.eclipse.team.ui.TeamUiUtils;
import com.atlassian.connector.eclipse.team.ui.exceptions.UnsupportedTeamProviderException;
import com.atlassian.theplugin.commons.crucible.api.model.BasicProject;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleVersionInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ExtendedCrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.commons.util.StringUtil;
import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for the UI
 * 
 * @author Shawn Minto
 * @author Wojciech Seliga
 */
public final class CrucibleUiUtil {

	private CrucibleUiUtil() {
	}

	public static TaskRepository getCrucibleTaskRepository(String repositoryUrl) {
		if (repositoryUrl != null) {
			return TasksUi.getRepositoryManager().getRepository(CrucibleCorePlugin.CONNECTOR_KIND, repositoryUrl);
		}
		return null;
	}

	public static ITask getCrucibleTask(TaskRepository taskRepository, String taskId) {
		if (taskRepository != null && taskId != null) {
			return TasksUi.getRepositoryModel().getTask(taskRepository, taskId);
		}
		return null;
	}

	@Nullable
	public static TaskRepository getCrucibleTaskRepository(Review review) {
		if (review != null) {
			String repositoryUrl = review.getServerUrl();
			if (repositoryUrl != null) {
				return getCrucibleTaskRepository(repositoryUrl);
			}
		}
		return null;
	}

	public static CrucibleClient getClient(Review review) {
		CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
		return connector.getClientManager().getClient(getCrucibleTaskRepository(review));
	}

	@Nullable
	public static ITask getCrucibleTask(Review review) {
		if (review != null) {
			TaskRepository taskRepository = getCrucibleTaskRepository(review);
			String taskId = CrucibleUtil.getTaskIdFromPermId(review.getPermId().getId());
			if (taskRepository != null && taskId != null) {
				return getCrucibleTask(taskRepository, taskId);
			}
		}

		return null;
	}

	public static boolean hasCurrentUserCompletedReview(Review review) {
		String currentUser = getCurrentUsername(review);
		return CrucibleUtil.isUserCompleted(currentUser, review);
	}

	public static String getCurrentUsername(Review review) {
		return getCurrentUsername(CrucibleUiUtil.getCrucibleTaskRepository(review));
	}

	public static User getCurrentCachedUser(TaskRepository repository) {
		return getCachedUser(getCurrentUsername(repository), repository);
	}

	public static User getCurrentCachedUser(Review review) {
		TaskRepository repository = CrucibleUiUtil.getCrucibleTaskRepository(review);
		return getCachedUser(getCurrentUsername(repository), repository);
	}

	private static boolean hasReviewerCompleted(Review review, String username) {
		for (Reviewer r : review.getReviewers()) {
			if (r.getUsername().equals(username)) {
				return r.isCompleted();
			}
		}
		return false;
	}

	public static Reviewer createReviewerFromCachedUser(Review review, User user) {
		boolean completed = hasReviewerCompleted(review, user.getUsername());
		return new Reviewer(user.getUsername(), user.getDisplayName(), completed);
	}

	public static String getCurrentUsername(TaskRepository repository) {
		/*
		 * String currentUser = CrucibleCorePlugin.getRepositoryConnector() .getClientManager() .getClient(repository)
		 * .getUserName();
		 */
		return repository.getUserName();
	}

	public static User getCachedUser(String userName, TaskRepository repository) {
		for (User user : getCachedUsers(repository)) {
			if (userName.equals(user.getUsername())) {
				return user;
			}
		}
		return null;
	}

	public static boolean isUserReviewer(String userName, Review review) {
		for (Reviewer reviewer : review.getReviewers()) {
			if (reviewer.getUsername().equals(userName)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isCurrentUserReviewer(Review review) {
		return isUserReviewer(CrucibleUiUtil.getCurrentUsername(review), review);
	}

	public static boolean isFilePartOfActiveReview(CrucibleFile crucibleFile) {
		Review activeReview = CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
		return isFilePartOfReview(crucibleFile, activeReview);
	}

	public static boolean isFilePartOfReview(CrucibleFile crucibleFile, Review review) {
		if (review == null || crucibleFile == null || crucibleFile.getCrucibleFileInfo() == null
				|| crucibleFile.getCrucibleFileInfo().getFileDescriptor() == null) {
			return false;
		}
		for (CrucibleFileInfo fileInfo : review.getFiles()) {
			if (fileInfo != null
					&& fileInfo.getFileDescriptor() != null
					&& fileInfo.getFileDescriptor()
							.getUrl()
							.equals(crucibleFile.getCrucibleFileInfo().getFileDescriptor().getUrl())
					&& fileInfo.getFileDescriptor()
							.getRevision()
							.equals(crucibleFile.getCrucibleFileInfo().getFileDescriptor().getRevision())) {
				return true;
			}
		}
		return false;
	}

	public static Set<User> getCachedUsers(Review review) {
		return getCachedUsers(getCrucibleTaskRepository(review));
	}

	public static Set<User> getCachedUsers(TaskRepository repository) {
		final CrucibleClientManager clientManager = CrucibleCorePlugin.getRepositoryConnector().getClientManager();
		final CrucibleClientData clientData = clientManager.getCrucibleClientData(repository);
		final Set<User> users;
		if (clientData == null) {
			users = new HashSet<User>();
		} else {
			users = clientData.getCachedUsers();
		}
		return users;
	}

	public static Set<Repository> getCachedRepositories(TaskRepository repository) {
		CrucibleClientData clientData = CrucibleCorePlugin.getRepositoryConnector()
				.getClientManager()
				.getCrucibleClientData(repository);
		Set<Repository> repositories;
		if (clientData == null) {
			repositories = new HashSet<Repository>();
		} else {
			repositories = clientData.getCachedRepositories();
		}
		return repositories;
	}

	/**
	 * 
	 * @param repository
	 * @return <code>null</code> when such information has not been yet cached
	 */
	@Nullable
	public static CrucibleVersionInfo getCrucibleVersionInfo(TaskRepository repository) {
		CrucibleClientData clientData = CrucibleCorePlugin.getRepositoryConnector()
				.getClientManager()
				.getCrucibleClientData(repository);

		return clientData == null ? null : clientData.getVersionInfo();
	}

	public static Collection<BasicProject> getCachedProjects(TaskRepository repository) {
		CrucibleClientData clientData = CrucibleCorePlugin.getRepositoryConnector()
				.getClientManager()
				.getCrucibleClientData(repository);
		if (clientData == null) {
			return Collections.emptyList();
		} else {
			return clientData.getCachedProjects();
		}
	}

	@Nullable
	public static BasicProject getCachedProject(TaskRepository repository, String projectKey) {
		CrucibleClientData clientData = CrucibleCorePlugin.getRepositoryConnector()
				.getClientManager().getCrucibleClientData(repository);

		if (clientData == null) {
			return null;
		}
		return clientData.getCrucibleProject(projectKey);
	}

	public static Collection<User> getUsersFromUsernames(TaskRepository taskRepository, Collection<String> usernames) {
		Set<User> users = CrucibleUiUtil.getCachedUsers(taskRepository);
		Set<User> result = MiscUtil.buildHashSet();
		for (User user : users) {
			if (usernames.contains(user.getUsername())) {
				result.add(user);
			}
		}
		return result;
	}

	/**
	 * 
	 * @return <code>null</code> when there is no cached information about allowed reviewers
	 */
	@Nullable
	public static Collection<User> getAllowedReviewers(TaskRepository taskRepository, String projectKey) {
		BasicProject project = getCachedProject(taskRepository, projectKey);
		if (project instanceof ExtendedCrucibleProject) {
			ExtendedCrucibleProject extendedProject = (ExtendedCrucibleProject) project;
			return getUsersFromUsernames(taskRepository, extendedProject.getAllowedReviewers());
		}
		return null;

	}

	public static boolean canModifyComment(Review review, Comment comment) {
		return CrucibleUtil.canAddCommentToReview(review)
				&& comment.getAuthor().getUsername().equals(CrucibleUiUtil.getCurrentUsername(review));
	}

	public static boolean canMarkAsReadOrUnread(Review review, Comment comment) {
		return CrucibleUtil.canAddCommentToReview(review)
				&& !comment.getAuthor().getUsername().equals(CrucibleUiUtil.getCurrentUsername(review));
	}

	public static Set<String> getUsernamesFromUsers(Collection<? extends User> users) {
		final Set<String> userNames = new HashSet<String>();
		for (User user : users) {
			userNames.add(user.getUsername());
		}
		return userNames;
	}

	public static boolean hasCachedData(TaskRepository repository) {
		CrucibleClientData clientData = CrucibleCorePlugin.getRepositoryConnector()
				.getClientManager()
				.getCrucibleClientData(repository);
		return clientData.hasData();
	}

	@NotNull
	public static Set<Reviewer> getAllCachedUsersAsReviewers(@NotNull TaskRepository taskRepository) {
		return toReviewers(CrucibleUiUtil.getCachedUsers(taskRepository));
	}

	@NotNull
	public static Set<Reviewer> toReviewers(@NotNull Collection<User> users) {
		Set<Reviewer> allReviewers = new HashSet<Reviewer>();
		for (User user : users) {
			allReviewers.add(new Reviewer(user.getUsername(), user.getDisplayName(), false));
		}
		return allReviewers;
	}

	@NotNull
	public static Set<User> toUsers(@NotNull Collection<Reviewer> users) {
		Set<User> res = new HashSet<User>();
		for (Reviewer user : users) {
			res.add(new User(user.getUsername(), user.getDisplayName(), user.getAvatarUrl()));
		}
		return res;
	}

	public static void focusOnComment(IEditorPart editor, CrucibleFile crucibleFile, VersionedComment versionedComment) {
		if (editor instanceof ITextEditor) {
			ITextEditor textEditor = ((ITextEditor) editor);
			if (versionedComment != null) {
				EditorUtil.selectAndReveal(textEditor, versionedComment, crucibleFile.getSelectedFile());
			}
		}
	}

	public static void updateTaskRepositoryCache(@NotNull final TaskRepository taskRepository,
			@NotNull IWizardContainer container, @NotNull WizardPage currentPage) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
				CrucibleClient client = connector.getClientManager().getClient(taskRepository);
				if (client != null) {
					try {
						client.updateRepositoryData(monitor, taskRepository);
					} catch (CoreException e) {
						StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
								"Failed to update repository data", e));
					}
				}
			}
		};
		try {
			container.run(true, true, runnable);
		} catch (Exception ex) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, "Failed to update repository data",
					ex));
		}
		if (!CrucibleUiUtil.hasCachedData(taskRepository)) {
			currentPage.setErrorMessage("Could not retrieve available projects and users from server.");
		}
	}

	public static boolean updateProjectDetailsCache(@NotNull final TaskRepository taskRepository,
			@NotNull final String projectKey, @NotNull IRunnableContext container) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
				CrucibleClient client = connector.getClientManager().getClient(taskRepository);
				if (client != null) {
					try {
						client.updateProjectDetails(monitor, taskRepository, projectKey);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			}
		};
		try {
			container.run(true, true, runnable);
			return true;
		} catch (Exception ex) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, "Failed to update repository data",
					ex));
			return false;
		}
	}

	/**
	 * Gets file from review (both pre- or post-commit)
	 * 
	 * @param resource
	 * @param review
	 * @return
	 */
	public static CrucibleFile getCrucibleFileFromResource(IResource resource, Review review, IProgressMonitor monitor) {
		CrucibleFile cruFile = getCruciblePostCommitFile(resource, review);

		if (cruFile != null) {
			return cruFile;
		}

		try {
			return getCruciblePreCommitFile(resource, review, monitor);
		} catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.WARNING, CrucibleUiPlugin.PRODUCT_NAME,
					"Cannot find pre-commit file for selected resource.", e));
		}

		return null;
	}

	/**
	 * Gets post-commit file form review
	 * 
	 * @param resource
	 * @param review
	 * @return
	 */
	public static CrucibleFile getCruciblePostCommitFile(IResource resource, Review review) {
		if (review == null || !(resource instanceof IFile)) {
			return null;
		}

		IFile file = (IFile) resource;

		TeamUiResourceManager teamResourceManager = AtlassianTeamUiPlugin.getDefault().getTeamResourceManager();

		for (ITeamUiResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled() && connector.canHandleFile(file)) {
				CrucibleFile fileInfo;
				try {
					fileInfo = connector.getCrucibleFileFromReview(review, file);
				} catch (UnsupportedTeamProviderException e) {
					return null;
				}
				if (fileInfo != null) {
					return fileInfo;
				}
			}
		}

		try {
			CrucibleFile crucibleFile = TeamUiUtils.getDefaultConnector().getCrucibleFileFromReview(review, file);
			if (crucibleFile != null) {
				return crucibleFile;
			}
		} catch (UnsupportedTeamProviderException e) {
			// ignore
		}

		return null;
	}

	/**
	 * Gets pre-commit file form review
	 * 
	 * @param resource
	 * @param review
	 * @return
	 * @throws CoreException
	 */
	public static CrucibleFile getCruciblePreCommitFile(final IResource resource, Review review,
			IProgressMonitor monitor) throws CoreException {

		if (review == null || !(resource instanceof IFile)) {
			return null;
		}

		IFile file = (IFile) resource;

		String localFileUrl = StringUtil.removeLeadingAndTrailingSlashes(file.getFullPath().toString());

		List<CrucibleFile> matchingFiles = new ArrayList<CrucibleFile>();

		for (CrucibleFileInfo cruFile : review.getFiles()) {
			String newFileUrl = StringUtil.removeLeadingAndTrailingSlashes(cruFile.getFileDescriptor().getUrl());
			String oldFileUrl = StringUtil.removeLeadingAndTrailingSlashes(cruFile.getOldFileDescriptor().getUrl());

			if (newFileUrl != null && newFileUrl.equals(localFileUrl)) {
				matchingFiles.add(new CrucibleFile(cruFile, false));
			} else if (oldFileUrl != null && oldFileUrl.equals(localFileUrl)) {
				matchingFiles.add(new CrucibleFile(cruFile, true));
			}
		}

		if (matchingFiles.size() > 0) {
			CrucibleClient client = CrucibleUiUtil.getClient(review);
			TaskRepository repository = CrucibleUiUtil.getCrucibleTaskRepository(review);

			for (final CrucibleFile cruFile : matchingFiles) {
				final String url = cruFile.getSelectedFile().getContentUrl();
				if (url == null || url.length() == 0) {
					StatusHandler.log(new Status(IStatus.WARNING, CrucibleUiPlugin.PRODUCT_NAME,
							"Cannot find pre-commit file for selected resource. Matching review item content url is empty"));
					continue;
				}
				Boolean ret = client.execute(new RemoteOperation<Boolean, CrucibleServerFacade2>(monitor, repository) {

					@Override
					public Boolean run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
							throws RemoteApiException, ServerPasswordNotProvidedException {

						final byte[] content = OpenVirtualFileJob.getContent(url, server.getSession(serverCfg),
								serverCfg.getUrl());

						if (content == null) {
							return false;
						}

						File localFile;
						try {
							localFile = OpenVirtualFileJob.createTempFile(cruFile.getSelectedFile().getName(), content);

							if (FileUtils.contentEquals(localFile, resource.getRawLocation().toFile())) {
								return true;
							}
						} catch (IOException e) {
							StatusHandler.log(new Status(
									IStatus.ERROR,
									CrucibleUiPlugin.PRODUCT_NAME,
									"Cannot create local temporary file. Cannot compare selected resource with review item.",
									e));
						}
						return false;
					}
				}, true);

				if (ret) {
					return cruFile;
				}
			}
		}

		return null;
	}

	public static String getDisplayNameOrUsername(User user) {
		return user.getDisplayName() == null || "".equals(user.getDisplayName()) ? user.getUsername()
				: user.getDisplayName();
	}

}
