/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.core.client.model;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProjectBean;

import java.io.Serializable;

/**
 * Cached Project information
 * 
 * @author Shawn Minto
 */
public class CrucibleCachedProject implements Serializable {

	private static final long serialVersionUID = 5778501312205967958L;

	private final String id;

	private final String name;

	private final String key;

	public CrucibleCachedProject(String id, String name, String key) {
		this.id = id;
		this.name = name;
		this.key = key;
	}

	public CrucibleCachedProject(CrucibleProject project) {
		this(project.getId(), project.getName(), project.getKey());
	}

	public String getId() {
		return id;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	public CrucibleProjectBean createProjectBeanFromCachedProject() {
		CrucibleProjectBean project = new CrucibleProjectBean();
		project.setId(id);
		project.setName(name);
		project.setKey(key);
		return project;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CrucibleCachedProject other = (CrucibleCachedProject) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		} else if (!key.equals(other.key)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

}
