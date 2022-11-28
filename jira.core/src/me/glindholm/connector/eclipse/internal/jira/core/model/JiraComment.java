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
public final class JiraComment implements Serializable {

    private static final long serialVersionUID = 1L;

    private String roleLevel;

    private String comment;

    private String author;

    private Instant created;

    private boolean markupDetected;

    private String authorDisplayName;

    public JiraComment(final String comment, final String author, final String roleLvel, final Instant created) {
        this.comment = comment;
        this.author = author;
        roleLevel = roleLvel;
        this.created = created;
    }

    public JiraComment() {
    }

    public String getAuthor() {
        return author;
    }

    public String getComment() {
        return comment;
    }

    public Instant getCreated() {
        return created;
    }

    public String getRoleLevel() {
        return roleLevel;
    }

    public boolean isMarkupDetected() {
        return markupDetected;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public void setCreated(final Instant created) {
        this.created = created;
    }

    public void setRoleLevel(final String level) {
        roleLevel = level;
    }

    public void setMarkupDetected(final boolean markupDetected) {
        this.markupDetected = markupDetected;
    }

    @Override
    public String toString() {
        return author + ": " + comment; //$NON-NLS-1$
    }

    public void setAuthorDisplayName(final String displayName) {
        authorDisplayName = displayName;
    }

    public String getAuthorDisplayName() {
        return authorDisplayName;
    }
}
