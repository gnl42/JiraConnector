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

package com.atlassian.connector.eclipse.internal.bamboo.ui;

import com.atlassian.connector.eclipse.internal.bamboo.core.BuildPlanManager;
import com.atlassian.connector.eclipse.internal.bamboo.core.BuildsChangedEvent;
import com.atlassian.connector.eclipse.internal.bamboo.core.BuildsChangedListener;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;

import java.util.Collection;
import java.util.Map;

/**
 * Provides the data (a.k.a. Bamboo builds) for the view
 * 
 * @author Thomas Ehrnhoefer
 */
public class BambooViewDataProvider implements BuildsChangedListener {

	private static BambooViewDataProvider uniqueInstance;

	private Map<TaskRepository, Collection<BambooBuild>> builds;

	private BambooView bambooView;

	private BambooViewDataProvider() {
	}

	public static BambooViewDataProvider getInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new BambooViewDataProvider();
		}
		return uniqueInstance;
	}

	public void init() {
		BuildPlanManager buildPlanMgr = BuildPlanManager.getInstance();
		buildPlanMgr.initializeScheduler(TasksUi.getRepositoryManager());
		BuildPlanManager.getInstance().addBuildsChangedListener(this);
		if (bambooView != null) {
			bambooView.buildsChanged();
		}
	}

	public Collection<BambooBuild> getBuilds(TaskRepository repository) {
		return builds.get(repository);
	}

	public Map<TaskRepository, Collection<BambooBuild>> getBuilds() {
		return builds;
	}

	public void dispose() {
		BuildPlanManager.getInstance().removeBuildsChangedListener(this);
	}

	public void buildsAdded(BuildsChangedEvent event) {
		updateBuilds(event);
	}

	private void updateBuilds(BuildsChangedEvent event) {
		builds = event.getAllBuilds();
		if (bambooView != null) {
			bambooView.buildsChanged();
		}
	}

	public void buildsChanged(BuildsChangedEvent event) {
		updateBuilds(event);
	}

	public void buildsRemoved(BuildsChangedEvent event) {
		updateBuilds(event);
	}

	public void setView(BambooView bambooView) {
		this.bambooView = bambooView;
	}
}
