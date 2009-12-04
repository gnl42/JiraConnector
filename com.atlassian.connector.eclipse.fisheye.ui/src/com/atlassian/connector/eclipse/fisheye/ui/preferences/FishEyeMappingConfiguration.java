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

package com.atlassian.connector.eclipse.fisheye.ui.preferences;

import org.eclipse.mylyn.tasks.core.TaskRepository;

public class FishEyeMappingConfiguration {
	private final TaskRepository taskRepository;

	private final String scmPath;

	private final String fishEyeRepo;

	public FishEyeMappingConfiguration(TaskRepository taskRepository, String scmPath, String fishEyeRepo) {
		this.scmPath = scmPath;
		this.fishEyeRepo = fishEyeRepo;
		this.taskRepository = taskRepository;
	}

	public String getScmPath() {
		return scmPath;
	}

	public TaskRepository getTaskRepository() {
		return taskRepository;
	}

	public String getFishEyeRepo() {
		return fishEyeRepo;
	}

	public FishEyeMappingConfiguration getClone() {
		return new FishEyeMappingConfiguration(taskRepository, scmPath, fishEyeRepo);
	}

}
