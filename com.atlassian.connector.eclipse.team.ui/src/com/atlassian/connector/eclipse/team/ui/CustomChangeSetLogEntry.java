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

package com.atlassian.connector.eclipse.team.ui;

import java.util.Arrays;
import java.util.Date;

/**
 * Model for a changeset logentry. Team API does not provide a common interface for that, so we need a custom model for
 * being able to have a generic connector.
 * 
 * @author Thomas Ehrnhoefer
 */
public class CustomChangeSetLogEntry implements ICustomChangesetLogEntry {

	private final String comment;

	private final String author;

	private final String revision;

	private final String[] changedFiles;

	private final ScmRepository repository;

	private final Date date;

	public CustomChangeSetLogEntry(String comment, String author, String revision, Date date, String[] changedFiles,
			ScmRepository repository) {
		super();
		this.comment = comment;
		this.author = author;
		this.revision = revision;
		this.date = date;
		this.changedFiles = changedFiles;
		this.repository = repository;
	}

	public String getComment() {
		return comment;
	}

	public String getAuthor() {
		return author;
	}

	public String getRevision() {
		return revision;
	}

	public String[] getChangedFiles() {
		return changedFiles;
	}

	public ScmRepository getRepository() {
		return repository;
	}

	public Date getDate() {
		return date;
	}

	public int compareTo(ICustomChangesetLogEntry other) {
		return this.date.compareTo(other.getDate()) * -1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + Arrays.hashCode(changedFiles);
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((repository == null) ? 0 : repository.hashCode());
		result = prime * result + ((revision == null) ? 0 : revision.hashCode());
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
		CustomChangeSetLogEntry other = (CustomChangeSetLogEntry) obj;
		if (author == null) {
			if (other.author != null) {
				return false;
			}
		} else if (!author.equals(other.author)) {
			return false;
		}
		if (!Arrays.equals(changedFiles, other.changedFiles)) {
			return false;
		}
		if (comment == null) {
			if (other.comment != null) {
				return false;
			}
		} else if (!comment.equals(other.comment)) {
			return false;
		}
		if (date == null) {
			if (other.date != null) {
				return false;
			}
		} else if (!date.equals(other.date)) {
			return false;
		}
		if (repository == null) {
			if (other.repository != null) {
				return false;
			}
		} else if (!repository.equals(other.repository)) {
			return false;
		}
		if (revision == null) {
			if (other.revision != null) {
				return false;
			}
		} else if (!revision.equals(other.revision)) {
			return false;
		}
		return true;
	}

}
