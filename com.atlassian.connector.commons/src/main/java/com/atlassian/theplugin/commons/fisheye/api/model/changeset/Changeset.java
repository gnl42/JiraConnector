/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.theplugin.commons.fisheye.api.model.changeset;

import com.atlassian.theplugin.commons.util.MiscUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class Changeset {

	private final String csid;
	private final String branch;
	private final String author;
	private final Date date;
	private final String comment;
	private final ArrayList<FileRevisionKey> revisionKeys;

	public Changeset(Date date, String csid, String branch, String author, String comment, List<FileRevisionKey> keys) {
		this.date = date;
		this.csid = csid;
		this.branch = branch;
		this.author = author;
		this.comment = comment;
		if (keys == null) {
			this.revisionKeys = MiscUtil.buildArrayList();
		} else {
			this.revisionKeys = MiscUtil.buildArrayList(keys);
		}
	}

	public String getCsid() {
		return csid;
	}
	public String getBranch() {
		return branch;
	}
	public String getAuthor() {
		return author;
	}
	public Date getDate() {
		return date;
	}

	public String getComment() {
		return comment;
	}

	public List<FileRevisionKey> getRevisionKeys() {
		return revisionKeys;
	}

}
