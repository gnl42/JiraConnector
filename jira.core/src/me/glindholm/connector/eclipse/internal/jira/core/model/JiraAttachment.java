/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core.model;

import java.io.Serializable;
import java.net.URI;
import java.time.Instant;

/**
 * @author Steffen Pingel
 */
public class JiraAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private long size;

    private String author;

    private Instant created;

    private URI content;

    private String authorDisplayName;

    public JiraAttachment() {
    }

    public JiraAttachment(final String id, final String name, final long size, final String author, final Instant created) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.author = author;
        this.created = created;
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

    public long getSize() {
        return size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(final Instant created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return name;
    }

    public URI getContent() {
        return content;
    }

    public void setContent(final URI content) {
        this.content = content;
    }

    public void setAuthorDisplayName(final String authorDisplayName) {
        this.authorDisplayName = authorDisplayName;
    }

    public String getAuthorDisplayName() {
        return authorDisplayName;
    }

}
