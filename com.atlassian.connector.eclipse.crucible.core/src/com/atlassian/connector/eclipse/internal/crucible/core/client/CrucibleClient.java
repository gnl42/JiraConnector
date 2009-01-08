/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.core.client;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleTaskMapper;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
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
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;

import java.util.Date;
import java.util.List;

/**
 * Bridge between Mylyn and the ACC API's
 * 
 * @author Shawn Minto
 */
public class CrucibleClient {

	private final CrucibleClientData clientData;

	private final AbstractWebLocation location;

	private final CrucibleServerCfg serverCfg;

	private final CrucibleServerFacade crucibleServer;

	public CrucibleClient(AbstractWebLocation location, CrucibleServerCfg serverCfg,
			CrucibleServerFacade crucibleServer, CrucibleClientData data) {
		this.location = location;
		this.clientData = data;
		this.serverCfg = serverCfg;
		this.crucibleServer = crucibleServer;
	}

	public void validate(IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			updateCredentials();
			crucibleServer.testServerConnection(serverCfg);
		} catch (CrucibleLoginException e) {
			throw new CoreException(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID,
					RepositoryStatus.ERROR_REPOSITORY_LOGIN, e.getMessage(), e));
		} catch (RemoteApiException e) {
			throw new CoreException(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID, e.getMessage(), e));
		}
	}

	public TaskData getTaskData(TaskRepository taskRepository, String taskId, IProgressMonitor monitor)
			throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			updateCredentials();
			String permId = CrucibleUtil.getPermIdFromTaskId(taskId);
			Review review = crucibleServer.getReview(serverCfg, new PermIdBean(permId));
			return getTaskDataForReview(taskRepository, review);
		} catch (CrucibleLoginException e) {
			throw new CoreException(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID,
					RepositoryStatus.ERROR_REPOSITORY_LOGIN, e.getMessage(), e));
		} catch (RemoteApiException e) {
			throw new CoreException(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID, e.getMessage(), e));
		} catch (ServerPasswordNotProvidedException e) {
			throw new CoreException(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID,
					RepositoryStatus.ERROR_REPOSITORY_LOGIN, e.getMessage(), e));
		}
	}

	public void performQuery(TaskRepository taskRepository, IRepositoryQuery query, TaskDataCollector resultCollector,
			IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			updateCredentials();
			if (!CrucibleUtil.isFilterDefinition(query)) {
				String filterId = query.getAttribute(CrucibleUtil.KEY_FILTER_ID);
				PredefinedFilter filter = CrucibleUtil.getPredefinedFilter(filterId);
				if (filter != null) {
					List<Review> reviewsForFilter = crucibleServer.getReviewsForFilter(serverCfg, filter);
					for (Review review : reviewsForFilter) {
						TaskData taskData = getTaskDataForReview(taskRepository, review);
						resultCollector.accept(taskData);
					}
				} else {
					throw new CoreException(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID,
							"No predefined filter exists for string: " + filterId));
				}
			} else {
				String allComplete = query.getAttribute(CustomFilter.ALLCOMPLETE);
				String author = query.getAttribute(CustomFilter.AUTHOR);
				String complete = query.getAttribute(CustomFilter.COMPLETE);
				String creator = query.getAttribute(CustomFilter.CREATOR);
				String moderator = query.getAttribute(CustomFilter.MODERATOR);
				String orRoles = query.getAttribute(CustomFilter.ORROLES);
				String project = query.getAttribute(CustomFilter.PROJECT);
				String reviewer = query.getAttribute(CustomFilter.REVIEWER);
				String states = query.getAttribute(CustomFilter.STATES);

				CustomFilterBean customFilter = new CustomFilterBean();

				customFilter.setAllReviewersComplete(Boolean.parseBoolean(allComplete));
				customFilter.setAuthor(author);
				customFilter.setComplete(Boolean.parseBoolean(complete));
				customFilter.setCreator(creator);
				customFilter.setModerator(moderator);
				customFilter.setOrRoles(Boolean.parseBoolean(orRoles));
				customFilter.setProjectKey(project);
				customFilter.setReviewer(reviewer);
				customFilter.setState(CrucibleUtil.getStatesFromString(states));

				List<Review> reviewsForFilter = crucibleServer.getReviewsForCustomFilter(serverCfg, customFilter);
				for (Review review : reviewsForFilter) {
					TaskData taskData = getTaskDataForReview(taskRepository, review);
					resultCollector.accept(taskData);
				}
			}
		} catch (CrucibleLoginException e) {
			throw new CoreException(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID,
					RepositoryStatus.ERROR_REPOSITORY_LOGIN, e.getMessage(), e));
		} catch (RemoteApiException e) {
			throw new CoreException(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID, e.getMessage(), e));
		} catch (ServerPasswordNotProvidedException e) {
			throw new CoreException(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID,
					RepositoryStatus.ERROR_REPOSITORY_LOGIN, e.getMessage(), e));
		}

	}

	private void updateCredentials() {
		AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
		if (credentials != null) {
			String newUserName = credentials.getUserName();
			String newPassword = credentials.getPassword();
			serverCfg.setUsername(newUserName);
			serverCfg.setPassword(newPassword);
		}
	}

	private TaskData getTaskDataForReview(TaskRepository taskRepository, Review review) {
		String summary = review.getName();
		String key = review.getPermId().getId();
		String id = CrucibleUtil.getTaskIdFromPermId(key);
		String owner = review.getAuthor().getUserName();
		Date creationDate = review.getCreateDate();
		Date closeDate = review.getCloseDate();

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
		return taskData;
	}

	public boolean hasRepositoryData() {
		return clientData != null && clientData.hasData();
	}

	public CrucibleClientData getClientData() {
		return clientData;
	}

	public void updateRepositoryData(IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		updateCredentials();
		try {
			monitor.subTask("Retrieving Crucible projects");
			List<CrucibleProject> projects = crucibleServer.getProjects(serverCfg);
			clientData.setProjects(projects);

			monitor.subTask("Retrieving Crucible users");
			List<User> users = crucibleServer.getUsers(serverCfg);
			clientData.setUsers(users);
		} catch (CrucibleLoginException e) {
			throw new CoreException(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID,
					RepositoryStatus.ERROR_REPOSITORY_LOGIN, e.getMessage(), e));
		} catch (RemoteApiException e) {
			throw new CoreException(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID, e.getMessage(), e));
		} catch (ServerPasswordNotProvidedException e) {
			throw new CoreException(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID,
					RepositoryStatus.ERROR_REPOSITORY_LOGIN, e.getMessage(), e));
		}

	}

}
