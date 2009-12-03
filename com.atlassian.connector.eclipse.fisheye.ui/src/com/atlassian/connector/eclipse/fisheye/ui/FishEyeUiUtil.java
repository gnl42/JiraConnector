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

package com.atlassian.connector.eclipse.fisheye.ui;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.fisheye.core.FishEyeCorePlugin;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public final class FishEyeUiUtil {
	private FishEyeUiUtil() {
	}

	@NotNull
	public static Set<TaskRepository> getFishEyeServers() {
		final Set<TaskRepository> fishEyeLikeRepos = MiscUtil.buildHashSet();
		final List<TaskRepository> allRepositories = TasksUi.getRepositoryManager().getAllRepositories();
		for (TaskRepository taskRepository : allRepositories) {
			if (taskRepository.getConnectorKind().equals(FishEyeCorePlugin.CONNECTOR_KIND)
					|| CrucibleRepositoryConnector.isFishEye(taskRepository)) {
				fishEyeLikeRepos.add(taskRepository);
			}
		}
		return fishEyeLikeRepos;
	}

}
