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

/**
 * TODO need mapping statuses -> actions -> fields TODO need mapping statuses -> fields
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

	public JiraProject(String id) {
		this.id = id;
	}

	public JiraProject() {
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLead() {
		return this.lead;
	}

	public void setLead(String lead) {
		this.lead = lead;
	}

	public String getProjectUrl() {
		return this.projectUrl;
	}

	public void setProjectUrl(String projectUrl) {
		this.projectUrl = projectUrl;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public JiraComponent getComponent(String name) {
		for (JiraComponent component : this.components) {
			if (component.getName().equals(name)) {
				return component;
			}
		}
		return null;
	}

	public JiraComponent[] getComponents() {
		return this.components;
	}

	public void setComponents(JiraComponent[] components) {
		this.components = components;
	}

	public JiraVersion getVersion(String name) {
		for (JiraVersion version : this.versions) {
			if (version.getName().equals(name)) {
				return version;
			}
		}
		return null;
	}

	public void setVersions(JiraVersion[] versions) {
		this.versions = versions;
	}

	public JiraVersion[] getReleasedVersions(boolean includeArchived) {
		List<JiraVersion> releasedVersions = new ArrayList<JiraVersion>();

		for (JiraVersion version : this.versions) {
			if (version.isReleased()) {
				if (!version.isArchived() || includeArchived) {
					releasedVersions.add(version);
				}
			}
		}

		return releasedVersions.toArray(new JiraVersion[releasedVersions.size()]);
	}

	public JiraVersion[] getUnreleasedVersions(boolean includeArchived) {
		List<JiraVersion> unreleasedVersions = new ArrayList<JiraVersion>();

		for (JiraVersion version : this.versions) {
			if (!version.isReleased()) {
				if (!version.isArchived() || includeArchived) {
					unreleasedVersions.add(version);
				}
			}
		}

		return unreleasedVersions.toArray(new JiraVersion[unreleasedVersions.size()]);
	}

	public JiraVersion[] getArchivedVersions() {
		List<JiraVersion> archivedVersions = new ArrayList<JiraVersion>();

		for (JiraVersion version : this.versions) {
			if (version.isArchived()) {
				archivedVersions.add(version);
			}
		}

		return archivedVersions.toArray(new JiraVersion[archivedVersions.size()]);
	}

	public JiraVersion[] getVersions() {
		return this.versions;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (!(obj instanceof JiraProject)) {
			return false;
		}

		JiraProject that = (JiraProject) obj;

		return this.name.equals(that.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return this.name;
	}

	public JiraIssueType[] getIssueTypes() {
		return issueTypes;
	}

	public void setIssueTypes(JiraIssueType[] issueTypes) {
		this.issueTypes = issueTypes;
		this.issueTypesById = new HashMap<String, JiraIssueType>();
		if (issueTypes != null) {
			for (JiraIssueType type : issueTypes) {
				issueTypesById.put(type.getId(), type);
			}
		}
	}

	public JiraSecurityLevel[] getSecurityLevels() {
		return securityLevels;
	}

	public void setSecurityLevels(JiraSecurityLevel[] securityLevels) {
		this.securityLevels = securityLevels;
	}

	public void setDetails(boolean details) {
		this.details = details;
	}

	public boolean hasDetails() {
		return details;
	}

	public JiraIssueType getIssueTypeById(String typeId) {
		if (issueTypesById != null) {
			return issueTypesById.get(typeId);
		}
		return null;
	}
}
