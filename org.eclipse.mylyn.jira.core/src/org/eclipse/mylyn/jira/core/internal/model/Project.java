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
package org.eclipse.mylar.jira.core.internal.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Project implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final Project MISSING_PROJECT = createMissingProject();

	private String id;

	private String name;

	private String key;

	private String description;

	private String lead;

	private String projectUrl;

	private String url;

	private Component[] components;

	private Version[] versions;

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

	public Component getComponent(String name) {
		for (int i = 0; i < this.components.length; i++) {
			if (components[i].getName().equals(name)) {
				return components[i];
			}
		}
		return Component.createMissingComponent(name);
	}

	public Component[] getComponents() {
		return this.components;
	}

	public void setComponents(Component[] components) {
		this.components = components;
	}

	public Version getVersion(String name) {
		for (int i = 0; i < this.versions.length; i++) {
			if (versions[i].getName().equals(name)) {
				return versions[i];
			}
		}

		// Return a dodgy placeholder version
		return Version.createMissingVersion(name);
	}

	public void setVersions(Version[] versions) {
		this.versions = versions;
	}

	public Version[] getReleasedVersions() {
		List releasedVersions = new ArrayList();

		for (int i = 0; i < this.versions.length; i++) {
			Version version = this.versions[i];
			if (version.isReleased()) {
				releasedVersions.add(version);
			}
		}

		return (Version[]) releasedVersions.toArray(new Version[releasedVersions.size()]);
	}

	public Version[] getUnreleasedVersions() {
		List unreleasedVersions = new ArrayList();

		for (int i = 0; i < this.versions.length; i++) {
			Version version = this.versions[i];
			if (!version.isReleased()) {
				unreleasedVersions.add(version);
			}
		}

		return (Version[]) unreleasedVersions.toArray(new Version[unreleasedVersions.size()]);
	}

	public Version[] getArchivedVersions() {
		List archivedVersions = new ArrayList();

		for (int i = 0; i < this.versions.length; i++) {
			Version version = this.versions[i];
			if (version.isArchived()) {
				archivedVersions.add(version);
			}
		}

		return (Version[]) archivedVersions.toArray(new Version[archivedVersions.size()]);
	}

	public Version[] getVersions() {
		return this.versions;
	}

	public static Project createMissingProject() {
		Project project = new Project();
		project.setDescription("Unknown");
		project.setId("Unknown");
		project.setKey("UNKNOWN");
		project.setId("-1");
		project.setVersions(new Version[0]);
		project.setComponents(new Component[0]);

		return project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (!(obj instanceof Project))
			return false;

		Project that = (Project) obj;

		return this.name.equals(that.name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return name.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.name;
	}
}
