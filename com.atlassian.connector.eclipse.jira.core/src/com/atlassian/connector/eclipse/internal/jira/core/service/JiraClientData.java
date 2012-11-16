/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.connector.eclipse.internal.jira.core.model.Group;
import com.atlassian.connector.eclipse.internal.jira.core.model.IssueType;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus;
import com.atlassian.connector.eclipse.internal.jira.core.model.Priority;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.ProjectRole;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.model.ServerInfo;
import com.atlassian.connector.eclipse.internal.jira.core.model.User;

/**
 * Caches repository configuration data.
 * 
 * @author Steffen Pingel
 */
public class JiraClientData implements Serializable {

	private static final long serialVersionUID = 1L;

	Group[] groups = new Group[0];

	IssueType[] issueTypes = new IssueType[0];

	Map<String, IssueType> issueTypesById = new HashMap<String, IssueType>();

	Priority[] priorities = new Priority[0];

	Map<String, Priority> prioritiesById = new HashMap<String, Priority>();

	Map<String, Priority> prioritiesByName = new HashMap<String, Priority>();

	Project[] projects = new Project[0];

	Map<String, Project> projectsById = new HashMap<String, Project>();

	Map<String, Project> projectsByKey = new HashMap<String, Project>();

	Resolution[] resolutions = new Resolution[0];

	Map<String, Resolution> resolutionsById = new HashMap<String, Resolution>();

	Map<String, Resolution> resolutionsByName = new HashMap<String, Resolution>();

	volatile ServerInfo serverInfo;

	JiraStatus[] statuses = new JiraStatus[0];

	ProjectRole[] projectRoles = new ProjectRole[0];

	Map<String, JiraStatus> statusesById = new HashMap<String, JiraStatus>();

	Map<String, JiraStatus> statusesByName = new HashMap<String, JiraStatus>();

	// not used
	User[] users = new User[0];

	Map<String, User> usersByName = new HashMap<String, User>();

	long lastUpdate;

//	JiraConfiguration configuration;

}
