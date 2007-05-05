/*******************************************************************************
 * Copyright (c) 2007 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author	Brock Janiczak
 */
public class Project implements Serializable {

	private static final long serialVersionUID = 1L;

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
		List<Version> releasedVersions = new ArrayList<Version>();

		for (int i = 0; i < this.versions.length; i++) {
			Version version = this.versions[i];
			if (version.isReleased()) {
				releasedVersions.add(version);
			}
		}

		return releasedVersions.toArray(new Version[releasedVersions.size()]);
	}

	public Version[] getUnreleasedVersions() {
		List<Version> unreleasedVersions = new ArrayList<Version>();

		for (int i = 0; i < this.versions.length; i++) {
			Version version = this.versions[i];
			if (!version.isReleased()) {
				unreleasedVersions.add(version);
			}
		}

		return unreleasedVersions.toArray(new Version[unreleasedVersions.size()]);
	}

	public Version[] getArchivedVersions() {
		List<Version> archivedVersions = new ArrayList<Version>();

		for (int i = 0; i < this.versions.length; i++) {
			Version version = this.versions[i];
			if (version.isArchived()) {
				archivedVersions.add(version);
			}
		}

		return archivedVersions.toArray(new Version[archivedVersions.size()]);
	}

	public Version[] getVersions() {
		return this.versions;
	}

	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (!(obj instanceof Project))
			return false;

		Project that = (Project) obj;

		return this.name.equals(that.name);
	}

	public int hashCode() {
		return name.hashCode();
	}

	public String toString() {
		return this.name;
	}
	
}
