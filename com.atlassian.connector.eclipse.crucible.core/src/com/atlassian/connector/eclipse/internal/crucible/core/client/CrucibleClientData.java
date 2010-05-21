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

import com.atlassian.theplugin.commons.crucible.api.model.BasicProject;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleVersionInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cached offline data used by Mylyn
 * 
 * @author Shawn Minto
 */
public class CrucibleClientData implements Serializable {

	private static final long serialVersionUID = 5078330984585994532L;

	private Set<User> cachedUsers;

	private Set<BasicProject> cachedProjects;

	private Set<Repository> cachedRepositories;

	private CrucibleVersionInfo versionInfo;

	private transient Map<String, byte[]> avatars;

	public CrucibleClientData() {
	}

	public boolean hasData() {
		return cachedUsers != null && cachedProjects != null;
	}

	public void setRepositories(List<Repository> repositories) {
		cachedRepositories = MiscUtil.buildHashSet();
		cachedRepositories.addAll(repositories);
	}

	public void setUsers(List<User> users) {
		cachedUsers = MiscUtil.buildHashSet();
		cachedUsers.addAll(users);
	}

	public void setProjects(Collection<BasicProject> projects) {
		cachedProjects = MiscUtil.buildHashSet();
		cachedProjects.addAll(projects);
	}

	public Set<BasicProject> getCachedProjects() {
		if (cachedProjects != null) {
			return Collections.unmodifiableSet(cachedProjects);
		} else {
			return Collections.unmodifiableSet(new HashSet<BasicProject>());
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

	public void setVersionInfo(@Nullable CrucibleVersionInfo versionInfo) {
		this.versionInfo = versionInfo;
	}

	@Nullable
	public CrucibleVersionInfo getVersionInfo() {
		return versionInfo;
	}

	public void addAvatar(@NotNull User user, @NotNull byte[] avatar) {
		if (avatars == null) {
			avatars = MiscUtil.buildHashMap();
		}
		avatars.put(user.getAvatarUrl(), avatar);
	}

	@Nullable
	public byte[] getAvatar(@NotNull User user) {
		return avatars != null ? avatars.get(user.getAvatarUrl()) : null;
	}

}
