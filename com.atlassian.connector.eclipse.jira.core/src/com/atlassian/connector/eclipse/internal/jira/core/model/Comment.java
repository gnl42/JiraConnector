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

package com.atlassian.connector.eclipse.internal.jira.core.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Brock Janiczak
 */
public final class Comment implements Serializable {

	private static final long serialVersionUID = 1L;

	private String roleLevel;

	private String comment;

	private String author;

	private Date created;

	private boolean markupDetected;

	private String authorDisplayName;

	public Comment(String comment, String author, String roleLvel, Date created) {
		this.comment = comment;
		this.author = author;
		this.roleLevel = roleLvel;
		this.created = created;
	}

	public Comment() {
	}

	public String getAuthor() {
		return this.author;
	}

	public String getComment() {
		return this.comment;
	}

	public Date getCreated() {
		return this.created;
	}

	public String getRoleLevel() {
		return this.roleLevel;
	}

	public boolean isMarkupDetected() {
		return markupDetected;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public void setRoleLevel(String level) {
		this.roleLevel = level;
	}

	public void setMarkupDetected(boolean markupDetected) {
		this.markupDetected = markupDetected;
	}

	@Override
	public String toString() {
		return this.author + ": " + this.comment; //$NON-NLS-1$
	}

	public void setAuthorDisplayName(String displayName) {
		this.authorDisplayName = displayName;
	}

	public String getAuthorDisplayName() {
		return authorDisplayName;
	}
}
