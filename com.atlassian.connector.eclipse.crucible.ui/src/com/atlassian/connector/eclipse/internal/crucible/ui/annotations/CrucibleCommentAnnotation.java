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

package com.atlassian.connector.eclipse.internal.crucible.ui.annotations;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;

/**
 * Class to represent a comment in a Crucible review
 * 
 * @author Shawn Minto
 */
public class CrucibleCommentAnnotation extends Annotation {
	private static final String COMMENT_ANNOTATION_ID = "com.atlassian.connector.eclipse.cruicible.ui.comment.annotation";

	private final Position position;

	private final VersionedComment comment;

	private final CrucibleFileInfo crucibleFileInfo;

	private final Review review;

	public CrucibleCommentAnnotation(int offset, int length, VersionedComment comment,
			CrucibleFileInfo crucibleFileInfo, Review review) {
		super(COMMENT_ANNOTATION_ID, false, null);
		position = new Position(offset, length);
		this.comment = comment;
		this.review = review;
		this.crucibleFileInfo = crucibleFileInfo;
	}

	public Position getPosition() {
		return position;
	}

	@Override
	public String getText() {
		return comment.getAuthor().getDisplayName() + " - " + comment.getMessage();
	}

	public VersionedComment getVersionedComment() {
		return comment;
	}

	public CrucibleFileInfo getCrucibleFileInfo() {
		return crucibleFileInfo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
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
		if (!(obj instanceof CrucibleCommentAnnotation)) {
			return false;
		}
		final CrucibleCommentAnnotation other = (CrucibleCommentAnnotation) obj;
		if (comment == null) {
			if (other.comment != null) {
				return false;
			}
		} else if (!comment.equals(other.comment)) {
			return false;
		}
		if (position == null) {
			if (other.position != null) {
				return false;
			}
		} else if (!position.equals(other.position)) {
			return false;
		}
		return true;
	}

	public Review getReview() {
		return review;
	}
}
