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

import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.RepositoryBean;

import java.io.Serializable;

/**
 * Cached repository information
 * 
 * @author Thomas Ehrnhoefer
 */
public class CrucibleCachedRepository implements Serializable {

	private static final long serialVersionUID = -2217544468152826345L;

	private final String name;

	private final String type;

	private final boolean enabled;

	public CrucibleCachedRepository(Repository repository) {
		this(repository.getName(), repository.getType(), repository.isEnabled());
	}

	public CrucibleCachedRepository(String name, String type, boolean enabled) {
		super();
		this.name = name;
		this.type = type;
		this.enabled = enabled;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Repository createRepositoryFromCached() {
		RepositoryBean repo = new RepositoryBean();
		repo.setEnabled(enabled);
		repo.setName(name);
		repo.setType(type);
		return repo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		CrucibleCachedRepository other = (CrucibleCachedRepository) obj;
		if (enabled != other.enabled) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

}
