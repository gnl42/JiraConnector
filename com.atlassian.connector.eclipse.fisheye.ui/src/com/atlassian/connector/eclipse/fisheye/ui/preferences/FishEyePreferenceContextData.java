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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FishEyePreferenceContextData {
	private final String scmPath;
	private final TaskRepository taskRepository;

	public FishEyePreferenceContextData(@NotNull String scmPath) {
		this(scmPath, null);
	}

	public FishEyePreferenceContextData(@NotNull String scmPath, @Nullable TaskRepository repository) {
		this.scmPath = scmPath;
		this.taskRepository = repository;
	}

	@NotNull
	public String getScmPath() {
		return scmPath;
	}

	@Nullable
	public TaskRepository getTaskRepository() {
		return taskRepository;
	}
}
