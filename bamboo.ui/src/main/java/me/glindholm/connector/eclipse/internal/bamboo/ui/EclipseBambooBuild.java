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

import java.util.Objects;

import javax.annotation.Nonnull;

import org.eclipse.mylyn.tasks.core.TaskRepository;

import me.glindholm.theplugin.commons.bamboo.BambooBuild;

/**
 * Links build info received from Bamboo server with the original
 * {@link TaskRepository} used for querying.
 *
 * @author Wojciech Seliga
 */
public class EclipseBambooBuild {

    @Nonnull
    private final BambooBuild build;

    @Nonnull
    private final TaskRepository taskRepository;

    public EclipseBambooBuild(@Nonnull final BambooBuild build, @Nonnull final TaskRepository taskRepository) {
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
        return Objects.hash(build);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EclipseBambooBuild other = (EclipseBambooBuild) obj;
        if (!Objects.equals(build, other.build)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("EclipseBambooBuild [build=").append(build).append(", taskRepository=").append(taskRepository).append("]");
        return builder.toString();
    }

}
