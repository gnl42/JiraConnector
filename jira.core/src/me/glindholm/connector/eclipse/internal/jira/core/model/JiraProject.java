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

package me.glindholm.connector.eclipse.internal.jira.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import me.glindholm.jira.rest.client.api.domain.BasicUser;
import me.glindholm.jira.rest.client.api.domain.User;

/**
 * TODO need mapping statuses -> actions -> fields
 *
 * TODO need mapping statuses -> fields
 *
 * @author Brock Janiczak
 * @author Thomas Ehrnhoefer
 */
public class JiraProject implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private String key;

    private String description;

    private String lead;

    private String projectUrl;

    private String url;

    private JiraComponent[] components;

    private JiraVersion[] versions;

    private JiraIssueType[] issueTypes;

    private JiraSecurityLevel[] securityLevels;

    private Map<String, JiraIssueType> issueTypesById;

    private boolean details;

    private Map<String, BasicUser> assignables = new ConcurrentHashMap<>();

    public JiraProject(final String id) {
        this.id = id;
    }

    public JiraProject() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getLead() {
        return lead;
    }

    public void setLead(final String lead) {
        this.lead = lead;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(final String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public JiraComponent getComponent(final String name) {
        for (final JiraComponent component : components) {
            if (component.getName().equals(name)) {
                return component;
            }
        }
        return null;
    }

    public JiraComponent[] getComponents() {
        return components;
    }

    public void setComponents(final JiraComponent[] components) {
        this.components = components;
    }

    public JiraVersion getVersion(final String name) {
        for (final JiraVersion version : versions) {
            if (version.getName().equals(name)) {
                return version;
            }
        }
        return null;
    }

    public void setVersions(final JiraVersion[] versions) {
        this.versions = versions;
    }

    public JiraVersion[] getReleasedVersions(final boolean includeArchived) {
        final List<JiraVersion> releasedVersions = new ArrayList<>();

        for (final JiraVersion version : versions) {
            if (version.isReleased()) {
                if (!version.isArchived() || includeArchived) {
                    releasedVersions.add(version);
                }
            }
        }

        return releasedVersions.toArray(new JiraVersion[releasedVersions.size()]);
    }

    public JiraVersion[] getUnreleasedVersions(final boolean includeArchived) {
        final List<JiraVersion> unreleasedVersions = new ArrayList<>();

        for (final JiraVersion version : versions) {
            if (!version.isReleased()) {
                if (!version.isArchived() || includeArchived) {
                    unreleasedVersions.add(version);
                }
            }
        }

        return unreleasedVersions.toArray(new JiraVersion[unreleasedVersions.size()]);
    }

    public JiraVersion[] getArchivedVersions() {
        final List<JiraVersion> archivedVersions = new ArrayList<>();

        for (final JiraVersion version : versions) {
            if (version.isArchived()) {
                archivedVersions.add(version);
            }
        }

        return archivedVersions.toArray(new JiraVersion[archivedVersions.size()]);
    }

    public JiraVersion[] getVersions() {
        return versions;
    }

    @Override
    public boolean equals(final Object obj) {
        if ((obj == null) || !(obj instanceof JiraProject)) {
            return false;
        }

        final JiraProject that = (JiraProject) obj;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    public JiraIssueType[] getIssueTypes() {
        return issueTypes;
    }

    public void setIssueTypes(final JiraIssueType[] issueTypes) {
        this.issueTypes = issueTypes;
        issueTypesById = new HashMap<>();
        if (issueTypes != null) {
            for (final JiraIssueType type : issueTypes) {
                issueTypesById.put(type.getId(), type);
            }
        }
    }

    public JiraSecurityLevel[] getSecurityLevels() {
        return securityLevels;
    }

    public void setSecurityLevels(final JiraSecurityLevel[] securityLevels) {
        this.securityLevels = securityLevels;
    }

    public void setDetails(final boolean details) {
        this.details = details;
    }

    public boolean hasDetails() {
        return details;
    }

    public JiraIssueType getIssueTypeById(final String typeId) {
        if (issueTypesById != null) {
            return issueTypesById.get(typeId);
        }
        return null;
    }

    public Map<String, BasicUser> getAssignables() {
        return assignables;
    }

    public void addAssignables(final List<? extends BasicUser> assignables) {
        for (final BasicUser assignable : assignables) {
            this.assignables.putIfAbsent(assignable.getId(), assignable);
        }
    }

    public void setAssignables(final List<User> assignables) {
        this.assignables = assignables.stream().collect(Collectors.toConcurrentMap(BasicUser::getId, Function.identity()));
    }

}
