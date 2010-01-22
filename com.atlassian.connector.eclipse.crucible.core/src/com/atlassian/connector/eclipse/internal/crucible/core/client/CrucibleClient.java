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

package com.atlassian.connector.eclipse.internal.crucible.core.client;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.connector.eclipse.internal.core.client.AbstractConnectorClient;
import com.atlassian.connector.eclipse.internal.core.client.HttpSessionCallbackImpl;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleConstants;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleTaskMapper;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.model.ReviewCache;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleVersionInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;

import java.util.Date;
import java.util.List;

/**
 * Bridge between Mylyn and the ACC API's
 * 
 * @author Shawn Minto
 * @author Thomas Ehrnhoefer
 * @author Wojciech Seliga
 */
public class CrucibleClient extends AbstractConnectorClient<CrucibleServerFacade2> {

	private final CrucibleClientData clientData;

	private final ReviewCache cachedReviewManager;

	public CrucibleClient(AbstractWebLocation location, ConnectionCfg serverCfg, CrucibleServerFacade2 crucibleServer,
			CrucibleClientData data, ReviewCache cachedReviewManager, HttpSessionCallbackImpl callback) {
		super(location, serverCfg, crucibleServer, callback);
		this.clientData = data;
		this.cachedReviewManager = cachedReviewManager;
	}

	public TaskData getTaskData(TaskRepository taskRepository, final String taskId, IProgressMonitor monitor)
			throws CoreException {

		return execute(new CrucibleRemoteOperation<TaskData>(monitor, taskRepository) {
			@Override
			public TaskData run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
					throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
				String permId = CrucibleUtil.getPermIdFromTaskId(taskId);
				Review review = server.getReview(serverCfg, new PermId(permId));

//				 this stuff could be invoked when we have avatarUrl served by Crucible  
//				fillMissingAvatarUrl(server, serverCfg, review);
				int metricsVersion = review.getMetricsVersion();
				if (cachedReviewManager != null && !cachedReviewManager.hasMetrics(metricsVersion)) {
					cachedReviewManager.setMetrics(metricsVersion, server.getMetrics(serverCfg, metricsVersion));
				}

				boolean hasChanged = cacheReview(taskId, review);

				return getTaskDataForReview(getTaskRepository(), review, hasChanged);
			}

//			private void fillMissingAvatarUrl(CrucibleServerFacade2 server, ConnectionCfg serverCfg, Review review) {
//				final List<User> users = server.getUsers(serverCfg);
//				Map<String, User> myDict = MiscUtil.buildHashMap();
//				for (User user : users) {
//					myDict.put(user.getUsername(), user);
//				}
//
//				try {
//					for (CrucibleFileInfo cfi : review.getFiles()) {
//						for (VersionedComment versionedComment : cfi.getVersionedComments()) {
//							fixMissingAvatar(myDict, versionedComment);
//						}
//					}
//				} catch (ValueNotYetInitialized e) {
//					//
//				}
//
//			}

//			private void fixMissingAvatar(Map<String, User> myDict, Comment versionedComment) {
//				final User userWithAvatarInfo = myDict.get(versionedComment.getAuthor().getUsername());
//				if (userWithAvatarInfo != null) {
//					versionedComment.getAuthor().setAvatarUrl(userWithAvatarInfo.getAvatarUrl());
//				}
//				for (Comment comment : versionedComment.getReplies()) {
//					fixMissingAvatar(myDict, comment);
//				}
//			}
		});

	}

	public Review getReview(TaskRepository repository, String taskId, boolean getWorkingCopy, IProgressMonitor monitor)
			throws CoreException {
		getTaskData(repository, taskId, monitor);
		if (getWorkingCopy) {
			return cachedReviewManager.getWorkingCopyReview(repository.getRepositoryUrl(), taskId);
		} else {
			return cachedReviewManager.getServerReview(repository.getRepositoryUrl(), taskId);
		}
	}

	public void performQuery(TaskRepository taskRepository, final IRepositoryQuery query,
			final TaskDataCollector resultCollector, IProgressMonitor monitor) throws CoreException {
		execute(new CrucibleRemoteOperation<Void>(monitor, taskRepository) {
			@Override
			public Void run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
					throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
				TaskRepository taskRepository = getTaskRepository();
				if (!CrucibleUtil.isFilterDefinition(query)) {
					String filterId = query.getAttribute(CrucibleConstants.KEY_FILTER_ID);
					PredefinedFilter filter = CrucibleUtil.getPredefinedFilter(filterId);
					if (filter != null) {
						List<Review> reviewsForFilter = server.getReviewsForFilter(serverCfg, filter);
						for (Review review : reviewsForFilter) {

							collectTaskDataForReview(server, serverCfg, taskRepository, resultCollector, review);
						}
					} else {
						throw new RemoteApiException("No predefined filter exists for string: " + filterId);
					}
				} else {
					CustomFilterBean customFilter = CrucibleUtil.createCustomFilterFromQuery(query);

					List<Review> reviewsForFilter = server.getReviewsForCustomFilter(serverCfg, customFilter);
					for (Review review : reviewsForFilter) {

						collectTaskDataForReview(server, serverCfg, taskRepository, resultCollector, review);
					}
				}
				return null;
			}

		});
	}

	private void collectTaskDataForReview(CrucibleServerFacade2 crucibleFacade, ConnectionCfg conCfg,
			final TaskRepository taskRepository, final TaskDataCollector resultCollector, Review review)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		String taskId = CrucibleUtil.getTaskIdFromReview(review);
		if (CrucibleUtil.isPartialReview(review)) {
			review = crucibleFacade.getReview(conCfg, review.getPermId());
		}

		int metricsVersion = review.getMetricsVersion();
		if (cachedReviewManager != null && !cachedReviewManager.hasMetrics(metricsVersion)) {
			cachedReviewManager.setMetrics(metricsVersion, facade.getMetrics(conCfg, metricsVersion));
		}

		boolean hasChanged = cacheReview(taskId, review);
		TaskData taskData = getTaskDataForReview(taskRepository, review, hasChanged);
		resultCollector.accept(taskData);
	}

	private TaskData getTaskDataForReview(TaskRepository taskRepository, Review review, boolean hasChanged) {
		String summary = review.getName();
		String key = review.getPermId().getId();
		String id = CrucibleUtil.getTaskIdFromReview(review);
		String owner = review.getAuthor().getUsername();
		Date creationDate = review.getCreateDate();
		Date closeDate = review.getCloseDate();

		if (CrucibleUtil.isCompleted(review) || CrucibleUtil.isUserCompleted(getUsername(), review)) {
			if (closeDate == null) {
				closeDate = new Date();
			}
		} else {
			closeDate = null;
		}

		int hash = CrucibleUtil.createHash(review);

		Date dateModified = creationDate;

		TaskData taskData = new TaskData(new TaskAttributeMapper(taskRepository), CrucibleCorePlugin.CONNECTOR_KIND,
				location.getUrl(), id);
		taskData.setPartial(true);

		CrucibleTaskMapper mapper = new CrucibleTaskMapper(taskData, true);
		mapper.setTaskKey(key);
		mapper.setCreationDate(creationDate);
		mapper.setModificationDate(dateModified);
		mapper.setSummary(summary);
		mapper.setOwner(owner);
		mapper.setCompletionDate(closeDate);
		mapper.setTaskUrl(CrucibleUtil.getReviewUrl(taskRepository.getUrl(), id));

		TaskAttribute hasChangedAttribute = taskData.getRoot().createAttribute(
				CrucibleConstants.HAS_CHANGED_TASKDATA_KEY);
		hasChangedAttribute.setValue(Boolean.toString(hasChanged));
		hasChangedAttribute.getMetaData().defaults().setReadOnly(true).setKind(TaskAttribute.KIND_DEFAULT).setLabel(
				"Has Changed").setType(TaskAttribute.TYPE_BOOLEAN);

		if (hash != -1) {
			TaskAttribute hashAttribute = taskData.getRoot().createAttribute(CrucibleConstants.CHANGED_HASH_CODE_KEY);
			hashAttribute.setValue(Integer.toString(hash));
			hashAttribute.getMetaData().defaults().setReadOnly(true).setKind(TaskAttribute.KIND_DEFAULT).setLabel(
					"Hash").setType(TaskAttribute.TYPE_INTEGER);

		}
		return taskData;
	}

	public boolean hasRepositoryData() {
		return clientData != null && clientData.hasData();
	}

	public CrucibleClientData getClientData() {
		return clientData;
	}

	public void updateVersionInfo(IProgressMonitor monitor, TaskRepository taskRepository) throws CoreException {
		execute(new CrucibleRemoteOperation<Void>(monitor, taskRepository) {
			@Override
			public Void run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
					throws RemoteApiException, ServerPasswordNotProvidedException {
				SubMonitor submonitor = SubMonitor.convert(monitor, "Updating server version info", 1);
				CrucibleVersionInfo versionInfo = server.getServerVersion(serverCfg);
				clientData.setVersionInfo(versionInfo);
				submonitor.worked(1);
				return null;
			}
		});
	}

	public void updateRepositoryData(IProgressMonitor monitor, TaskRepository taskRepository) throws CoreException {
		execute(new CrucibleRemoteOperation<Void>(monitor, taskRepository) {
			@Override
			public Void run(CrucibleServerFacade2 server, ConnectionCfg serverCfg, IProgressMonitor monitor)
					throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
				SubMonitor submonitor = SubMonitor.convert(monitor, "Updating repository data", 4);
				CrucibleVersionInfo versionInfo = server.getServerVersion(serverCfg);
				clientData.setVersionInfo(versionInfo);
				submonitor.worked(1);

				List<CrucibleProject> projects = server.getProjects(serverCfg);
				clientData.setProjects(projects);
				submonitor.worked(1);

				List<User> users = server.getUsers(serverCfg);
				clientData.setUsers(users);
				submonitor.worked(1);

				List<Repository> repositories = server.getRepositories(serverCfg);
				clientData.setRepositories(repositories);
				submonitor.worked(1);
				return null;
			}
		});
	}

	/**
	 * 
	 * @return true if there was a change with the latest version of the review added to the cache
	 */
	private boolean cacheReview(String taskId, Review review) {
		if (cachedReviewManager != null) {
			return cachedReviewManager.updateCachedReview(location.getUrl(), taskId, review);
		}
		return false;
	}

}
