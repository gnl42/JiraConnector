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

package com.atlassian.theplugin.commons.crucible.api.model.changes;

import com.atlassian.theplugin.commons.util.MiscUtil;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public final class Change {

	private final String author;
	private final Date date;
	private final String csid;
	private final String comment;
	private final Link link;
	private final List<Revision> revisions;

	public String getAuthor() {
		return author;
	}

	public Date getDate() {
		return date;
	}
	public String getCsid() {
		return csid;
	}
	public String getComment() {
		return comment;
	}
	public Link getLink() {
		return link;
	}
	public List<Revision> getRevisions() {
		return revisions;
	}

	public Change(String author, Date date, String csid, Link link, String comment, Collection<Revision> revisions) {
		this.author = author;
		this.date = date;
		this.csid = csid;
		this.link = link;
		this.comment = comment;
		if (revisions == null) {
			this.revisions = MiscUtil.buildArrayList();
		} else {
			this.revisions = MiscUtil.buildArrayList(revisions);
		}
	}
}
