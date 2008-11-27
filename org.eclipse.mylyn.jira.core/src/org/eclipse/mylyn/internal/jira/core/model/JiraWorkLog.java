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

package org.eclipse.mylyn.internal.jira.core.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Steffen Pingel
 */
public class JiraWorkLog implements Serializable {

	private static final long serialVersionUID = 1L;

	private String author;

	private String comment;

	private Date created;

	private String groupLevel;

	private String id;

	private String roleLevelId;

	private Date startDate;

	private long timeSpent;

	private String updateAuthor;

	private Date updated;

	public JiraWorkLog() {
	}

	public String getAuthor() {
		return author;
	}

	public String getComment() {
		return comment;
	}

	public Date getCreated() {
		return created;
	}

	public String getGroupLevel() {
		return groupLevel;
	}

	public String getId() {
		return id;
	}

	public String getRoleLevelId() {
		return roleLevelId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public long getTimeSpent() {
		return timeSpent;
	}

	public String getUpdateAuthor() {
		return updateAuthor;
	}

	public Date getUpdated() {
		return updated;
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

	public void setGroupLevel(String groupLevel) {
		this.groupLevel = groupLevel;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setRoleLevelId(String roleLevelId) {
		this.roleLevelId = roleLevelId;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setTimeSpent(long timeSpent) {
		this.timeSpent = timeSpent;
	}

	public void setUpdateAuthor(String updateAuthor) {
		this.updateAuthor = updateAuthor;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

}
