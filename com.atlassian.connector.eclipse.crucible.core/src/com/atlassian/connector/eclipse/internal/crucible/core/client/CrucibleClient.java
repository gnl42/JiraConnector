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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleConstants;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleTaskMapper;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.model.ReviewCache;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.PermIdBean;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
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
 */
public class CrucibleClient {

	private final CrucibleClientData clientData;

	private final AbstractWebLocation location;

	private final CrucibleServerCfg crucibleServerCfg;

	private final CrucibleServerFacade crucibleServer;

	private final ReviewCache cachedReviewManager;

	public abstract static class RemoteOperation<T> {

		private final IProgressMonitor fMonitor;

		public RemoteOperation(IProgressMonitor monitor) {
			this.fMonitor = Policy.monitorFor(monitor);
		}

		public IProgressMonitor getMonitor() {
			return fMonitor;
		}

		public abstract T run(CrucibleServerFacade server, CrucibleServerCfg serverCfg, IProgressMonitor monitor)
				throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException;

	}

	public CrucibleClient(AbstractWebLocation location, CrucibleServerCfg serverCfg,
			CrucibleServerFacade crucibleServer, CrucibleClientData data, ReviewCache cachedReviewManager) {
		this.location = location;
		this.clientData = data;
		this.crucibleServerCfg = serverCfg;
		this.crucibleServer = crucibleServer;
		this.cachedReviewManager = cachedReviewManager;
	}

	public <T> T execute(RemoteOperation<T> op) throws CoreException {
		IProgressMonitor monitor = op.getMonitor();
		try {
			monitor.beginTask("Connecting to Crucible", IProgressMonitor.UNKNOWN);
			updateServer();
			return op.run(crucibleServer, crucibleServerCfg, op.getMonitor());
		} catch (CrucibleLoginException e) {
			throw new CoreException(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID,
					RepositoryStatus.ERROR_REPOSITORY_LOGIN, e.getMessage(), e));
		} catch (RemoteApiException e) {
			throw new CoreException(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID, e.getMessage(), e));
		} catch (ServerPasswordNotProvidedException e) {
			throw new CoreException(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID,
					RepositoryStatus.ERROR_REPOSITORY_LOGIN, e.getMessage(), e));
		} finally {
			monitor.done();
		}
	}

	public String getUserName() {
		AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
		if (credentials != null) {
			return credentials.getUserName();
		} else {
			return null;
		}
	}

	private void updateServer() {
		AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
		if (credentials != null) {
			String newUserName = credentials.getUserName();
			String newPassword = credentials.getPassword();
			crucibleServerCfg.setUsername(newUserName);
			crucibleServerCfg.setPassword(newPassword);
		}
	}

	public void validate(IProgressMonitor monitor) throws CoreException {
		execute(new RemoteOperation<Object>(monitor) {
			@Override
			public Object run(CrucibleServerFacade server, CrucibleServerCfg serverCfg, IProgressMonitor monitor)
					throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
				server.testServerConnection(serverCfg);
				return null;
			}
		});
	}

	public TaskData getTaskData(final TaskRepository taskRepository, final String taskId, IProgressMonitor monitor)
			throws CoreException {

		return execute(new RemoteOperation<TaskData>(monitor) {
			@Override
			public TaskData run(CrucibleServerFacade server, CrucibleServerCfg serverCfg, IProgressMonitor monitor)
					throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
				String permId = CrucibleUtil.getPermIdFromTaskId(taskId);
				Review review = server.getReview(serverCfg, new PermIdBean(permId));

				int metricsVersion = review.getMetricsVersion();
				if (cachedReviewManager != null && !cachedReviewManager.hasMetrics(metricsVersion)) {
					cachedReviewManager.setMetrics(metricsVersion, server.getMetrics(serverCfg, metricsVersion));
				}

				boolean hasChanged = cacheReview(taskId, review);

				return getTaskDataForReview(taskRepository, review, hasChanged);
			}
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

	public void performQuery(final TaskRepository taskRepository, final IRepositoryQuery query,
			final TaskDataCollector resultCollector, IProgressMonitor monitor) throws CoreException {
		execute(new RemoteOperation<Object>(monitor) {
			@Override
			public Object run(CrucibleServerFacade server, CrucibleServerCfg serverCfg, IProgressMonitor monitor)
					throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
				if (!CrucibleUtil.isFilterDefinition(query)) {
					String filterId = query.getAttribute(CrucibleConstants.KEY_FILTER_ID);
					PredefinedFilter filter = CrucibleUtil.getPredefinedFilter(filterId);
					if (filter != null) {
						List<Review> reviewsForFilter = server.getReviewsForFilter(serverCfg, filter);
						for (Review review : reviewsForFilter) {

							collectTaskDataForReview(taskRepository, resultCollector, review);
						}
					} else {
						throw new RemoteApiException("No predefined filter exists for string: " + filterId);
					}
				} else {
					CustomFilterBean customFilter = CrucibleUtil.createCustomFilterFromQuery(query);

					List<Review> reviewsForFilter = server.getReviewsForCustomFilter(serverCfg, customFilter);
					for (Review review : reviewsForFilter) {

						collectTaskDataForReview(taskRepository, resultCollector, review);
					}
				}
				return null;
			}

		});
	}

	private void collectTaskDataForReview(final TaskRepository taskRepository, final TaskDataCollector resultCollector,
			Review review) throws RemoteApiException, ServerPasswordNotProvidedException {
		String taskId = CrucibleUtil.getTaskIdFromReview(review);
		if (CrucibleUtil.isPartialReview(review)) {
			review = crucibleServer.getReview(crucibleServerCfg, review.getPermId());
		}
		boolean hasChanged = cacheReview(taskId, review);
		TaskData taskData = getTaskDataForReview(taskRepository, review, hasChanged);
		resultCollector.accept(taskData);
	}

	private TaskData getTaskDataForReview(TaskRepository taskRepository, Review review, boolean hasChanged) {
		String summary = review.getName();
		String key = review.getPermId().getId();
		String id = CrucibleUtil.getTaskIdFromReview(review);
		String owner = review.getAuthor().getUserName();
		Date creationDate = review.getCreateDate();
		Date closeDate = review.getCloseDate();

		if (CrucibleUtil.isCompleted(review) || CrucibleUtil.isUserCompleted(getUserName(), review)) {
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

	public void updateRepositoryData(IProgressMonitor monitor) throws CoreException {
		execute(new RemoteOperation<Object>(monitor) {
			@Override
			public Object run(CrucibleServerFacade server, CrucibleServerCfg serverCfg, IProgressMonitor monitor)
					throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
				monitor.subTask("Retrieving Crucible projects");
				List<CrucibleProject> projects = server.getProjects(serverCfg);
				clientData.setProjects(projects);

				monitor.subTask("Retrieving Crucible users");
				List<User> users = server.getUsers(serverCfg);
				clientData.setUsers(users);
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
