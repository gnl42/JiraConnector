/*******************************************************************************
 * Copyright (c) 2005 Jira Dashboard project.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylar.jira.core.internal.service;

import java.io.File;

import org.eclipse.mylar.jira.core.internal.model.Comment;
import org.eclipse.mylar.jira.core.internal.model.Component;
import org.eclipse.mylar.jira.core.internal.model.Group;
import org.eclipse.mylar.jira.core.internal.model.Issue;
import org.eclipse.mylar.jira.core.internal.model.IssueType;
import org.eclipse.mylar.jira.core.internal.model.NamedFilter;
import org.eclipse.mylar.jira.core.internal.model.Priority;
import org.eclipse.mylar.jira.core.internal.model.Project;
import org.eclipse.mylar.jira.core.internal.model.Resolution;
import org.eclipse.mylar.jira.core.internal.model.ServerInfo;
import org.eclipse.mylar.jira.core.internal.model.Status;
import org.eclipse.mylar.jira.core.internal.model.User;
import org.eclipse.mylar.jira.core.internal.model.Version;
import org.eclipse.mylar.jira.core.internal.model.filter.FilterDefinition;
import org.eclipse.mylar.jira.core.internal.model.filter.IssueCollector;
import org.eclipse.mylar.jira.core.internal.service.exceptions.AuthenticationException;
import org.eclipse.mylar.jira.core.internal.service.exceptions.InsufficientPermissionException;
import org.eclipse.mylar.jira.core.internal.service.exceptions.ServiceUnavailableException;

/**
 * This interface exposes the full set of services available from a Jira
 * installation. It provides a unified inferface for the SOAP and Web/RSS
 * services available.
 * 
 * @author Brock Janiczak
 */
public interface JiraService {
	public User getUser(String username) throws AuthenticationException, InsufficientPermissionException,
			ServiceUnavailableException;

	// public User createUser(String in1, String in2, String in3, String in4);
	public Component[] getComponents(String projectKey) throws InsufficientPermissionException,
			AuthenticationException, ServiceUnavailableException;

	// public Group createGroup(String in1, User in2);
	public String login(String username, String password) throws AuthenticationException, ServiceUnavailableException;

	public Group getGroup(String name) throws InsufficientPermissionException, AuthenticationException,
			ServiceUnavailableException;

	public ServerInfo getServerInfo() throws ServiceUnavailableException;

	// public Group updateGroup(Group group);
	public Issue getIssue(String issueKey);

	public Issue createIssue(Issue issue);

	public Project[] getProjects() throws InsufficientPermissionException, AuthenticationException,
			ServiceUnavailableException;

	public Project[] getProjectsNoSchemes() throws InsufficientPermissionException, AuthenticationException,
			ServiceUnavailableException;

	// public Project updateProject(Project project);
	public Status[] getStatuses() throws InsufficientPermissionException, AuthenticationException,
			ServiceUnavailableException;

	public IssueType[] getIssueTypes() throws InsufficientPermissionException, AuthenticationException,
			ServiceUnavailableException;

	public IssueType[] getSubTaskIssueTypes() throws InsufficientPermissionException, AuthenticationException,
			ServiceUnavailableException;

	public Priority[] getPriorities() throws InsufficientPermissionException, AuthenticationException,
			ServiceUnavailableException;

	public Resolution[] getResolutions() throws InsufficientPermissionException, AuthenticationException,
			ServiceUnavailableException;

	public Comment[] getComments(String issueKey);

	public Version[] getVersions(String componentKey) throws InsufficientPermissionException, AuthenticationException,
			ServiceUnavailableException;

	// public Project createProject(String in1, String in2, String in3, String
	// in4, String in5, PermissionScheme in6, Scheme in7, Scheme in8);
	public boolean logout() throws ServiceUnavailableException;

	// public void deleteProject(String projectKey);
	// public void deleteIssue(String issueKey);
	// public Scheme[] getNotificationSchemes();
	// public PermissionScheme[] getPermissionSchemes();
	// public Permission[] getAllPermissions();
	// public PermissionScheme createPermissionScheme(String in1, String in2);
	// public void deletePermissionScheme(String in1) ;
	// public PermissionScheme addPermissionTo(PermissionScheme in1, Permission
	// in2, Entity in3);
	// public PermissionScheme deletePermissionFrom(PermissionScheme in1,
	// Permission in2, Entity in3);
	// public boolean addAttachmentToIssue(String[] in1, Issue in2);
	// public void deleteUser(String username);
	// public void deleteGroup(String in1, String in2);
	public NamedFilter[] getSavedFilters();

	// public Scheme[] getSecuritySchemes();

	void findIssues(FilterDefinition filterDefinition, IssueCollector collector);

	void executeNamedFilter(NamedFilter filter, IssueCollector collector);

	void quickSearch(String searchString, IssueCollector collector);

	public abstract void addCommentToIssue(final Issue issue, final String comment);

	public abstract void updateIssue(final Issue issue, final String comment);

	public abstract void assignIssueTo(final Issue issue, final int assigneeType, final String user,
			final String comment);

	public abstract void advanceIssueWorkflow(final Issue issue, final String action, final Resolution resolution,
			final Version[] fixVersions, final String comment, final int assigneeType, final String user);

	public abstract void advanceIssueWorkflow(final Issue issue, final String action);

	public abstract void startIssue(Issue issue, String comment, String user);

	public abstract void stopIssue(Issue issue, String comment, String user);

	public abstract void resolveIssue(Issue issue, Resolution resolution, Version[] fixVersions, String comment,
			int assigneeType, String user);

	public abstract void reopenIssue(Issue issue, String comment, int assigneeType, String user);

	public abstract void closeIssue(Issue issue, Resolution resolution, Version[] fixVersions, String comment,
			int assigneeType, String user);

	public abstract void attachFile(final Issue issue, final String comment, final String filename,
			final byte[] contents, final String contentType);

	public abstract void attachFile(final Issue issue, final String comment, final File file, final String contentType);

	public abstract void watchIssue(final Issue issue);

	public abstract void unwatchIssue(final Issue issue);

	public abstract void voteIssue(final Issue issue);

	public abstract void unvoteIssue(final Issue issue);
}
