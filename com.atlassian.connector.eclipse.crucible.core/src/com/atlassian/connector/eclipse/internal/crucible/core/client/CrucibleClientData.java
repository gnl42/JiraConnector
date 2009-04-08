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

import com.atlassian.connector.eclipse.internal.crucible.core.client.model.CrucibleCachedProject;
import com.atlassian.connector.eclipse.internal.crucible.core.client.model.CrucibleCachedRepository;
import com.atlassian.connector.eclipse.internal.crucible.core.client.model.CrucibleCachedUser;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.User;

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

	private Set<CrucibleCachedUser> cachedUsers;

	private Set<CrucibleCachedProject> cachedProjects;

	private Set<CrucibleCachedRepository> cachedRepositories;

	public CrucibleClientData() {

	}

	public boolean hasData() {
		return cachedUsers != null && cachedProjects != null;
	}

	public void setRepositories(List<Repository> repositories) {
		cachedRepositories = new HashSet<CrucibleCachedRepository>();
		for (Repository repository : repositories) {
			cachedRepositories.add(new CrucibleCachedRepository(repository));
		}
	}

	public void setUsers(List<User> users) {
		cachedUsers = new HashSet<CrucibleCachedUser>();
		for (User user : users) {
			cachedUsers.add(new CrucibleCachedUser(user));
		}

	}

	public void setProjects(List<CrucibleProject> projects) {
		cachedProjects = new HashSet<CrucibleCachedProject>();
		for (CrucibleProject project : projects) {
			cachedProjects.add(new CrucibleCachedProject(project));
		}
	}

	public Set<CrucibleCachedProject> getCachedProjects() {
		if (cachedProjects != null) {
			return Collections.unmodifiableSet(cachedProjects);
		} else {
			return Collections.unmodifiableSet(new HashSet<CrucibleCachedProject>());
		}
	}

	public Set<CrucibleCachedUser> getCachedUsers() {
		if (cachedUsers != null) {
			return Collections.unmodifiableSet(cachedUsers);
		} else {
			return Collections.unmodifiableSet(new HashSet<CrucibleCachedUser>());
		}
	}

	public Set<CrucibleCachedRepository> getCachedRepositories() {
		if (cachedRepositories != null) {
			return Collections.unmodifiableSet(cachedRepositories);
		} else {
			return Collections.unmodifiableSet(new HashSet<CrucibleCachedRepository>());
		}
	}

}
