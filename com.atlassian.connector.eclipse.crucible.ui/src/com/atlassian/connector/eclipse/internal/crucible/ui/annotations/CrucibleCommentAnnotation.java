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

	public CrucibleCommentAnnotation(int offset, int length, VersionedComment comment) {
		super(COMMENT_ANNOTATION_ID, false, null);
		position = new Position(offset, length);
		this.comment = comment;
	}

	public Position getPosition() {
		return position;
	}

	@Override
	public String getText() {
		String message = comment.getAuthor().getDisplayName() + "--" + comment.getMessage();
//		for (VersionedComment reply : comment.getReplies()) {
//			message += "\n\t" + reply.getAuthor().getDisplayName() + "\n\t" + reply.getMessage();
//		}

		return message;
	}
}
