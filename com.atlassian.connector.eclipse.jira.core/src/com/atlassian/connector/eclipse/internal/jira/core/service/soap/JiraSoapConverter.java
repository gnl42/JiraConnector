/*******************************************************************************
 * Copyright (c) 2004, 2009 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core.service.soap;

import java.util.Calendar;
import java.util.Date;

import com.atlassian.connector.eclipse.internal.jira.core.model.Comment;
import com.atlassian.connector.eclipse.internal.jira.core.model.Component;
import com.atlassian.connector.eclipse.internal.jira.core.model.Group;
import com.atlassian.connector.eclipse.internal.jira.core.model.IssueType;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraConfiguration;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import com.atlassian.connector.eclipse.internal.jira.core.model.NamedFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.Priority;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.ProjectRole;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.model.SecurityLevel;
import com.atlassian.connector.eclipse.internal.jira.core.model.ServerInfo;
import com.atlassian.connector.eclipse.internal.jira.core.model.User;
import com.atlassian.connector.eclipse.internal.jira.core.model.Version;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraTimeFormat;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemoteComment;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemoteComponent;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemoteConfiguration;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemoteCustomFieldValue;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemoteFieldValue;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemoteFilter;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemoteGroup;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemoteIssueType;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemotePriority;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemoteProject;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemoteProjectRole;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemoteResolution;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemoteSecurityLevel;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemoteServerInfo;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemoteStatus;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemoteUser;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemoteVersion;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemoteWorklog;

/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 */
public class JiraSoapConverter {

	protected static Priority[] convert(RemotePriority[] remotePriorities) {
		Priority[] priorities = new Priority[remotePriorities.length];
		for (int i = 0; i < remotePriorities.length; i++) {
			priorities[i] = convert(remotePriorities[i]);
		}
		return priorities;
	}

	protected static Priority convert(RemotePriority remotePriority) {
		Priority priority = new Priority(remotePriority.getId());
		priority.setColour(remotePriority.getColor());
		priority.setDescription(remotePriority.getDescription());
		priority.setIcon(remotePriority.getIcon());
		priority.setName(remotePriority.getName());

		return priority;
	}

	protected static JiraWorkLog[] convert(RemoteWorklog[] remoteWorklogs) {
		JiraWorkLog[] worklogs = new JiraWorkLog[remoteWorklogs.length];
		for (int i = 0; i < remoteWorklogs.length; i++) {
			worklogs[i] = convert(remoteWorklogs[i]);
		}
		return worklogs;
	}

	protected static JiraWorkLog convert(RemoteWorklog remoteWorklog) {
		JiraWorkLog worklog = new JiraWorkLog();
		worklog.setAuthor(remoteWorklog.getAuthor());
		worklog.setComment(remoteWorklog.getComment());
		worklog.setCreated(convert(remoteWorklog.getCreated()));
		worklog.setId(remoteWorklog.getId());
		worklog.setRoleLevelId(remoteWorklog.getRoleLevelId());
		worklog.setStartDate(convert(remoteWorklog.getStartDate()));
		worklog.setTimeSpent(remoteWorklog.getTimeSpentInSeconds());
		worklog.setUpdateAuthor(remoteWorklog.getUpdateAuthor());
		worklog.setAuthor(worklog.getAuthor());
		return worklog;
	}

	protected static RemoteWorklog convert(JiraWorkLog worklog, JiraTimeFormat formatter) {
		RemoteWorklog remoteWorklog = new RemoteWorklog();
		remoteWorklog.setAuthor(worklog.getAuthor());
		remoteWorklog.setComment(worklog.getComment());
		remoteWorklog.setCreated(convert(worklog.getCreated()));
		remoteWorklog.setId(worklog.getId());
		remoteWorklog.setRoleLevelId(worklog.getRoleLevelId());
		remoteWorklog.setStartDate(convert(worklog.getStartDate()));
		// looks like JIRA ignores seconds and takes string 'timespent' 
		// it can cause some problems if local time tracking setting is different from the server one 
		remoteWorklog.setTimeSpentInSeconds(worklog.getTimeSpent());
		remoteWorklog.setTimeSpent(formatter.format(worklog.getTimeSpent()));
		remoteWorklog.setUpdateAuthor(worklog.getUpdateAuthor());
		remoteWorklog.setAuthor(worklog.getAuthor());
		return remoteWorklog;
	}

	protected static Date convert(Calendar calendar) {
		return (calendar != null) ? calendar.getTime() : null;
	}

	protected static Calendar convert(Date date) {
		if (date != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			return calendar;
		}
		return null;
	}

	protected static JiraStatus[] convert(RemoteStatus[] remoteStatuses) {
		JiraStatus[] statuses = new JiraStatus[remoteStatuses.length];
		for (int i = 0; i < remoteStatuses.length; i++) {
			statuses[i] = convert(remoteStatuses[i]);
		}
		return statuses;
	}

	protected static JiraStatus convert(RemoteStatus remoteStatus) {
		return new JiraStatus(remoteStatus.getId(), remoteStatus.getName(), remoteStatus.getDescription(),
				remoteStatus.getIcon());
	}

	protected static Version[] convert(RemoteVersion[] remoteVersions) {
		Version[] versions = new Version[remoteVersions.length];
		for (int i = 0; i < remoteVersions.length; i++) {
			versions[i] = convert(remoteVersions[i]);
		}
		return versions;
	}

	protected static Version convert(RemoteVersion remoteVersion) {
		Version version = new Version(remoteVersion.getId());
		version.setArchived(remoteVersion.isArchived());
		version.setName(remoteVersion.getName());
		version.setReleased(remoteVersion.isReleased());
		version.setReleaseDate(remoteVersion.getReleaseDate() != null ? remoteVersion.getReleaseDate().getTime() : null);
		version.setSequence(remoteVersion.getSequence().longValue());

		return version;
	}

	protected static Resolution[] convert(RemoteResolution[] remoteResolutions) {
		Resolution[] resolutions = new Resolution[remoteResolutions.length];
		for (int i = 0; i < remoteResolutions.length; i++) {
			resolutions[i] = convert(remoteResolutions[i]);
		}
		return resolutions;
	}

	protected static Resolution convert(RemoteResolution remoteResolution) {
		return new Resolution(remoteResolution.getId(), remoteResolution.getName(), remoteResolution.getDescription(),
				remoteResolution.getIcon());
	}

	protected static IssueType[] convert(RemoteIssueType[] remoteIssueTypes) {
		IssueType[] issueTypes = new IssueType[remoteIssueTypes.length];
		for (int i = 0; i < remoteIssueTypes.length; i++) {
			issueTypes[i] = convert(remoteIssueTypes[i]);
		}
		return issueTypes;
	}

	protected static IssueType convert(RemoteIssueType remoteIssueType) {
		return new IssueType(remoteIssueType.getId(), remoteIssueType.getName(), remoteIssueType.getDescription(),
				remoteIssueType.getIcon());
	}

	protected static Project[] convert(RemoteProject[] remoteProjects) {
		Project[] projects = new Project[remoteProjects.length];
		for (int i = 0; i < remoteProjects.length; i++) {
			projects[i] = convert(remoteProjects[i]);
		}
		return projects;
	}

	protected static Project convert(RemoteProject remoteProject) {
		Project project = new Project();
		project.setDescription(remoteProject.getDescription());
		project.setId(remoteProject.getId());
		project.setKey(remoteProject.getKey());
		project.setLead(remoteProject.getLead());
		project.setName(remoteProject.getName());
		project.setProjectUrl(remoteProject.getProjectUrl());
		project.setUrl(remoteProject.getUrl());

		return project;
	}

	public static RemoteProject convert(Project project) {
		RemoteProject remoteProject = new RemoteProject();

		remoteProject.setDescription(project.getDescription());
		remoteProject.setId(project.getId());
		remoteProject.setKey(project.getKey());
		remoteProject.setLead(project.getLead());
		remoteProject.setName(project.getName());
		remoteProject.setProjectUrl(project.getProjectUrl());
		remoteProject.setUrl(project.getUrl());

		return remoteProject;
	}

	public static ProjectRole[] convert(RemoteProjectRole[] remoteProjectRoles) {
		ProjectRole[] projectRoles = new ProjectRole[remoteProjectRoles.length];
		for (int i = 0; i < remoteProjectRoles.length; ++i) {
			projectRoles[i] = convert(remoteProjectRoles[i]);
		}
		return projectRoles;
	}

	private static ProjectRole convert(RemoteProjectRole remoteProjectRole) {
		ProjectRole projectRole = new ProjectRole();

		projectRole.setDescription(remoteProjectRole.getDescription());
		projectRole.setId(remoteProjectRole.getId());
		projectRole.setName(remoteProjectRole.getName());

		return projectRole;
	}

	public static RemoteProjectRole convert(ProjectRole projectRole) {
		RemoteProjectRole remoteProjectRole = new RemoteProjectRole();

		remoteProjectRole.setDescription(projectRole.getDescription());
		remoteProjectRole.setId(projectRole.getId());
		remoteProjectRole.setName(projectRole.getName());

		return remoteProjectRole;
	}

	protected static Component[] convert(RemoteComponent[] remoteComponents) {
		Component[] components = new Component[remoteComponents.length];
		for (int i = 0; i < remoteComponents.length; i++) {
			components[i] = convert(remoteComponents[i]);
		}
		return components;
	}

	protected static Component convert(RemoteComponent remoteComponent) {
		Component component = new Component();
		component.setId(remoteComponent.getId());
		component.setName(remoteComponent.getName());

		return component;
	}

	protected static User[] convert(RemoteUser[] remoteUsers) {
		User[] users = new User[remoteUsers.length];
		for (int i = 0; i < remoteUsers.length; i++) {
			users[i] = convert(remoteUsers[i]);
		}
		return users;
	}

	protected static User convert(RemoteUser remoteUser) {
		User user = new User();
		user.setEmail(remoteUser.getEmail());
		user.setFullName(remoteUser.getFullname());
		user.setName(remoteUser.getName());

		return user;
	}

	protected static Group[] convert(RemoteGroup[] remoteGroups) {
		Group[] groups = new Group[remoteGroups.length];
		for (int i = 0; i < remoteGroups.length; i++) {
			groups[i] = convert(remoteGroups[i]);
		}

		return groups;
	}

	protected static Group convert(RemoteGroup remoteGroup) {
		Group group = new Group();
		group.setName(remoteGroup.getName());
		group.setUsers(convert(remoteGroup.getUsers()));

		return group;
	}

	protected static ServerInfo convert(RemoteServerInfo remoteServerInfo, ServerInfo serverInfo) {
		serverInfo.setBaseUrl(remoteServerInfo.getBaseUrl());
		serverInfo.setBuildDate(remoteServerInfo.getBuildDate().getTime());
		serverInfo.setBuildNumber(remoteServerInfo.getBuildNumber());
		serverInfo.setEdition(remoteServerInfo.getEdition());
		serverInfo.setVersion(remoteServerInfo.getVersion());
		return serverInfo;
	}

	public static NamedFilter[] convert(RemoteFilter[] savedFilters) {
		NamedFilter[] namedFilters = new NamedFilter[savedFilters.length];
		for (int i = 0; i < savedFilters.length; i++) {
			RemoteFilter savedFilter = savedFilters[i];
			NamedFilter filter = new NamedFilter();
			filter.setId(savedFilter.getId());
			filter.setName(savedFilter.getName());
			filter.setAuthor(savedFilter.getAuthor());
			filter.setDescription(savedFilter.getDescription());
//			filter.setProject(savedFilter.getProject());

			namedFilters[i] = filter;
		}
		return namedFilters;
	}

	public static Comment[] convert(RemoteComment[] remoteComments) {
		Comment[] comments = new Comment[remoteComments.length];
		for (int i = 0; i < remoteComments.length; i++) {
			comments[i] = convert(remoteComments[i]);
		}
		return comments;
	}

	private static Comment convert(RemoteComment remoteComment) {
		Comment comment = new Comment();
		comment.setAuthor(remoteComment.getAuthor());
		comment.setComment(remoteComment.getBody());
		comment.setRoleLevel(remoteComment.getRoleLevel());
		return comment;
	}

	public static RemoteComment convert(Comment comment) {
		RemoteComment rComment = new RemoteComment();
		rComment.setAuthor(comment.getAuthor());
		rComment.setBody(comment.getComment());
		rComment.setRoleLevel(comment.getRoleLevel());

		return rComment;
	}

	protected static SecurityLevel[] convert(RemoteSecurityLevel[] remoteSecurityLevels) {
		SecurityLevel[] securityLevels = new SecurityLevel[remoteSecurityLevels.length];
		for (int i = 0; i < remoteSecurityLevels.length; i++) {
			SecurityLevel securityLevel = new SecurityLevel();
			securityLevel.setId(remoteSecurityLevels[i].getId());
			securityLevel.setName(remoteSecurityLevels[i].getName());
			securityLevels[i] = securityLevel;
		}
		return securityLevels;
	}

	public static RemoteComponent[] convert(Component[] components) {
		RemoteComponent[] remoteComponents = new RemoteComponent[components.length];
		for (int i = 0; i < remoteComponents.length; ++i) {
			remoteComponents[i] = convert(components[i]);
		}
		return remoteComponents;
	}

	private static RemoteComponent convert(Component component) {
		return new RemoteComponent(component.getId(), component.getName());
	}

	public static RemoteVersion[] convert(Version[] reportedVersions) {
		RemoteVersion[] versions = new RemoteVersion[reportedVersions.length];
		for (int i = 0; i < versions.length; ++i) {
			versions[i] = convert(reportedVersions[i]);
		}
		return versions;
	}

	private static RemoteVersion convert(Version version) {
		Calendar releaseDate = null;
		if (version.getReleaseDate() != null) {
			releaseDate = Calendar.getInstance();
			releaseDate.setTime(version.getReleaseDate());
		}
		return new RemoteVersion(version.getId(), version.getName(), false, releaseDate, false, version.getSequence());
	}

	public static RemoteCustomFieldValue[] convert(RemoteFieldValue[] array) {
		RemoteCustomFieldValue[] fields = new RemoteCustomFieldValue[array.length];
		for (int i = 0; i < array.length; ++i) {
			fields[i] = convert(array[i]);
		}
		return fields;
	}

	private static RemoteCustomFieldValue convert(RemoteFieldValue remoteFieldValue) {
		return new RemoteCustomFieldValue(remoteFieldValue.getId(), null, remoteFieldValue.getValues());
	}

	public static JiraConfiguration convert(RemoteConfiguration remoteConf) {
		JiraConfiguration conf = new JiraConfiguration();

		conf.setTimeTrackingHoursPerDay(remoteConf.getTimeTrackingHoursPerDay());
		conf.setTimeTrackingDaysPerWeek(remoteConf.getTimeTrackingDaysPerWeek());
		conf.setAllowAttachments(remoteConf.isAllowAttachments());
		conf.setAllowExternalUserManagment(remoteConf.isAllowExternalUserManagment());
		conf.setAllowIssueLinking(remoteConf.isAllowIssueLinking());
		conf.setAllowSubTasks(remoteConf.isAllowSubTasks());
		conf.setAllowTimeTracking(remoteConf.isAllowTimeTracking());
		conf.setAllowUnassignedIssues(remoteConf.isAllowUnassignedIssues());
		conf.setAllowVoting(remoteConf.isAllowVoting());
		conf.setAllowWatching(remoteConf.isAllowWatching());

		return conf;
	}

}
