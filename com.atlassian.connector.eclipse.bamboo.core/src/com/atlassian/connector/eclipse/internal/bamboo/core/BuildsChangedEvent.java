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

import java.util.List;

public class BuildsChangedEvent {
	private final List<BambooBuild> addedBuilds;

	private final List<BambooBuild> removedBuilds;

	private final List<BambooBuild> changedBuilds;

	private final List<BambooBuild> allBuilds;

	public BuildsChangedEvent(List<BambooBuild> addedBuilds, List<BambooBuild> removedBuilds,
			List<BambooBuild> changedBuilds, List<BambooBuild> allBuilds) {
		super();
		this.addedBuilds = addedBuilds;
		this.removedBuilds = removedBuilds;
		this.changedBuilds = changedBuilds;
		this.allBuilds = allBuilds;
	}

	public List<BambooBuild> getAddedBuilds() {
		return addedBuilds;
	}

	public List<BambooBuild> getRemovedBuilds() {
		return removedBuilds;
	}

	public List<BambooBuild> getChangedBuilds() {
		return changedBuilds;
	}

	public List<BambooBuild> getAllBuilds() {
		return allBuilds;
	}

}