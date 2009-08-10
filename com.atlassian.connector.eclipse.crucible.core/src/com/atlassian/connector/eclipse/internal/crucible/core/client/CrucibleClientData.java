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

package com.atlassian.connector.eclipse.internal.crucible.core.client;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.util.MiscUtil;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cached offline data used by Mylyn
 * 
 * @author Shawn Minto
 */
public class CrucibleClientData implements Serializable {

	private static final long serialVersionUID = 5078330984585994532L;

	private Set<User> cachedUsers;

	private Set<CrucibleProject> cachedProjects;

	private Set<Repository> cachedRepositories;

	public CrucibleClientData() {

	}

	public boolean hasData() {
		return cachedUsers != null && cachedProjects != null;
	}

	public void setRepositories(List<Repository> repositories) {
		cachedRepositories = new HashSet<Repository>();
		cachedRepositories.addAll(repositories);
	}

	public void setUsers(List<User> users) {
		cachedUsers = MiscUtil.buildHashSet();
		cachedUsers.addAll(users);
	}

	public void setProjects(List<CrucibleProject> projects) {
		cachedProjects = new HashSet<CrucibleProject>();
		cachedProjects.addAll(projects);
	}

	public Set<CrucibleProject> getCachedProjects() {
		if (cachedProjects != null) {
			return Collections.unmodifiableSet(cachedProjects);
		} else {
			return Collections.unmodifiableSet(new HashSet<CrucibleProject>());
		}
	}

	public Set<User> getCachedUsers() {
		if (cachedUsers != null) {
			return Collections.unmodifiableSet(cachedUsers);
		} else {
			return Collections.unmodifiableSet(new HashSet<User>());
		}
	}

	public Set<Repository> getCachedRepositories() {
		if (cachedRepositories != null) {
			return Collections.unmodifiableSet(cachedRepositories);
		} else {
			return Collections.unmodifiableSet(new HashSet<Repository>());
		}
	}

}
