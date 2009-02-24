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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildsChangedEvent {
	private final Map<TaskRepository, Collection<BambooBuild>> changedBuilds;

	private final Map<TaskRepository, Collection<BambooBuild>> allBuilds;

	private final Map<TaskRepository, Collection<BambooBuild>> oldBuilds;

	private final List<String> errorLog;

	private final boolean forcedRefresh;

	private final boolean failed;

	public BuildsChangedEvent(Map<TaskRepository, Collection<BambooBuild>> changedBuilds,
			Map<TaskRepository, Collection<BambooBuild>> allBuilds,
			Map<TaskRepository, Collection<BambooBuild>> oldBuilds, List<String> errorLog, boolean forcedRefresh,
			boolean failed) {
		super();
		this.changedBuilds = changedBuilds;
		this.allBuilds = allBuilds;
		this.oldBuilds = oldBuilds;
		this.errorLog = errorLog;
		this.forcedRefresh = forcedRefresh;
		this.failed = failed;
	}

	public Map<TaskRepository, Collection<BambooBuild>> getChangedBuilds() {
		return changedBuilds == null ? new HashMap<TaskRepository, Collection<BambooBuild>>() : changedBuilds;
	}

	public Map<TaskRepository, Collection<BambooBuild>> getAllBuilds() {
		return allBuilds == null ? new HashMap<TaskRepository, Collection<BambooBuild>>() : allBuilds;
	}

	public Map<TaskRepository, Collection<BambooBuild>> getOldBuilds() {
		return oldBuilds == null ? new HashMap<TaskRepository, Collection<BambooBuild>>() : oldBuilds;
	}

	public List<String> getErrorLog() {
		return errorLog == null ? new ArrayList<String>() : errorLog;
	}

	public boolean isForcedRefresh() {
		return forcedRefresh;
	}

	public boolean isFailed() {
		return failed;
	}
}