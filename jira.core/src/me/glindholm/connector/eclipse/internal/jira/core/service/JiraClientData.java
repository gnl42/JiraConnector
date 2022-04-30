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

package me.glindholm.connector.eclipse.internal.jira.core.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraGroup;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueType;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraPriority;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProjectRole;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraResolution;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraServerInfo;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraStatus;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraUser;
import me.glindholm.jira.rest.client.api.domain.Field;

/**
 * Caches repository configuration data.
 *
 * @author Steffen Pingel
 */
public class JiraClientData implements Serializable {

    private static final long serialVersionUID = 1L;

    JiraGroup[] groups = new JiraGroup[0];

    JiraIssueType[] issueTypes = new JiraIssueType[0];

    Map<String, JiraIssueType> issueTypesById = new HashMap<>();

    JiraPriority[] priorities = new JiraPriority[0];

    Map<String, JiraPriority> prioritiesById = new HashMap<>();

    Map<String, JiraPriority> prioritiesByName = new HashMap<>();

    JiraProject[] projects = new JiraProject[0];

    Map<String, JiraProject> projectsById = new HashMap<>();

    Map<String, JiraProject> projectsByKey = new HashMap<>();

    JiraResolution[] resolutions = new JiraResolution[0];

    Map<String, JiraResolution> resolutionsById = new HashMap<>();

    Map<String, JiraResolution> resolutionsByName = new HashMap<>();

    volatile JiraServerInfo serverInfo;

    JiraStatus[] statuses = new JiraStatus[0];

    JiraProjectRole[] projectRoles = new JiraProjectRole[0];

    Map<String, JiraStatus> statusesById = new HashMap<>();

    Map<String, JiraStatus> statusesByName = new HashMap<>();

    // not used
    JiraUser[] users = new JiraUser[0];

    Map<String, JiraUser> usersByName = new HashMap<>();

    long lastUpdate;

    public Map<String, Field> metadata = new HashMap<>();

    //	JiraConfiguration configuration;

}
