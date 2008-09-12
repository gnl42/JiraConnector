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
public class Attachment implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	private String name;

	private long size;

	private String author;

	private Date created;

	public Attachment(String id, String name, long size, String author, Date created) {
		this.id = id;
		this.name = name;
		this.size = size;
		this.author = author;
		this.created = created;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	@Override
	public String toString() {
		return this.name;
	}

}
