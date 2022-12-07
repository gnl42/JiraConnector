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
import java.util.List;
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

/**
 * Caches repository configuration data.
 *
 * @author Steffen Pingel
 */
public class JiraClientData implements Serializable {

    private static final long serialVersionUID = -2259866721234344593L;

    JiraGroup[] groups = {};

    JiraIssueType[] issueTypes = {};

    Map<String, JiraIssueType> issueTypesById = new HashMap<>();

    JiraPriority[] priorities = {};

    Map<String, JiraPriority> prioritiesById = new HashMap<>();

    Map<String, JiraPriority> prioritiesByName = new HashMap<>();

    JiraProject[] projects = {};

    Map<String, JiraProject> projectsById = new HashMap<>();

    Map<String, JiraProject> projectsByKey = new HashMap<>();

    JiraResolution[] resolutions = {};

    Map<String, JiraResolution> resolutionsById = new HashMap<>();

    Map<String, JiraResolution> resolutionsByName = new HashMap<>();

    volatile JiraServerInfo serverInfo;

    JiraStatus[] statuses = {};

    JiraProjectRole[] projectRoles = {};

    Map<String, JiraStatus> statusesById = new HashMap<>();

    Map<String, JiraStatus> statusesByName = new HashMap<>();

    Map<String, JiraUser> usersByName = new HashMap<>();

    List<Integer> spaceHolder;
    long lastUpdate;

    // JiraConfiguration configuration;

}
