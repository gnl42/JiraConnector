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

package me.glindholm.connector.eclipse.internal.bamboo.ui;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import javax.annotation.Nonnull;

import me.glindholm.theplugin.commons.bamboo.BambooBuild;

/**
 * Links build info received from Bamboo server with the original {@link TaskRepository} used for querying.
 * 
 * @author Wojciech Seliga
 */
public class EclipseBambooBuild {

	@Nonnull
	private final BambooBuild build;

	@Nonnull
	private final TaskRepository taskRepository;

	public EclipseBambooBuild(@Nonnull BambooBuild build, @Nonnull TaskRepository taskRepository) {
		this.build = build;
		this.taskRepository = taskRepository;
	}

	@Nonnull
	public BambooBuild getBuild() {
		return build;
	}

	@Nonnull
	public TaskRepository getTaskRepository() {
		return taskRepository;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((build == null) ? 0 : build.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EclipseBambooBuild other = (EclipseBambooBuild) obj;
		if (build == null) {
			if (other.build != null) {
				return false;
			}
		} else if (!build.equals(other.build)) {
			return false;
		}
		return true;
	}

}
