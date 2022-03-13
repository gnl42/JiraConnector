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

package com.atlassian.connector.commons.crucible.api.model;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import org.jetbrains.annotations.Nullable;

public final class ReviewModelUtil {

	private ReviewModelUtil() {

	}

	// VersionedComment -> Coment -> Comment -> ***
	// GeneralComment -> Coment -> Comment -> ***
	@Nullable
	public static VersionedComment getParentVersionedComment(Comment comment) {
		while (comment != null && !(comment instanceof VersionedComment)) {
			comment = comment.getParentComment();
		}
		return (VersionedComment) comment;
	}

}
