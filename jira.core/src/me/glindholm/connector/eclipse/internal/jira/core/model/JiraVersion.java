/*******************************************************************************
 * Copyright (c) 2004, 2008 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core.model;

import java.io.Serializable;
import java.time.Instant;

/**
 * @author Brock Janiczak
 */
public class JiraVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private boolean archived;

    private Instant releaseDate;

    private boolean released;

    private long sequence;

    public JiraVersion(final String id, final String name) {
        this.id = id;
        this.name = name;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(final boolean archived) {
        this.archived = archived;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(final boolean released) {
        this.released = released;
    }

    public Instant getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(final Instant releaseDate) {
        this.releaseDate = releaseDate;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(final long sequence) {
        this.sequence = sequence;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !(obj instanceof final JiraVersion that)) {
            return false;
        }

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

}
