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

package com.atlassian.connector.eclipse.internal.bamboo.core;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.mylyn.tasks.core.TaskRepository;

import java.util.Collection;
import java.util.Map;

public class BuildsChangedEvent {
	private final Map<TaskRepository, Collection<BambooBuild>> addedBuilds;

	private final Map<TaskRepository, Collection<BambooBuild>> removedBuilds;

	private final Map<TaskRepository, Collection<BambooBuild>> changedBuilds;

	private final Map<TaskRepository, Collection<BambooBuild>> allBuilds;

	private final Map<TaskRepository, Collection<BambooBuild>> oldBuilds;

	public BuildsChangedEvent(Map<TaskRepository, Collection<BambooBuild>> addedBuilds,
			Map<TaskRepository, Collection<BambooBuild>> removedBuilds,
			Map<TaskRepository, Collection<BambooBuild>> changedBuilds,
			Map<TaskRepository, Collection<BambooBuild>> allBuilds,
			Map<TaskRepository, Collection<BambooBuild>> oldBuilds) {
		super();
		this.addedBuilds = addedBuilds;
		this.removedBuilds = removedBuilds;
		this.changedBuilds = changedBuilds;
		this.allBuilds = allBuilds;
		this.oldBuilds = oldBuilds;
	}

	public Map<TaskRepository, Collection<BambooBuild>> getAddedBuilds() {
		return addedBuilds;
	}

	public Map<TaskRepository, Collection<BambooBuild>> getRemovedBuilds() {
		return removedBuilds;
	}

	public Map<TaskRepository, Collection<BambooBuild>> getChangedBuilds() {
		return changedBuilds;
	}

	public Map<TaskRepository, Collection<BambooBuild>> getAllBuilds() {
		return allBuilds;
	}

	public Map<TaskRepository, Collection<BambooBuild>> getOldBuilds() {
		return oldBuilds;
	}

}