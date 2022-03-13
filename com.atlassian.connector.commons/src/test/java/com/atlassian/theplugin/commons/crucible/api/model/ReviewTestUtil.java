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

package com.atlassian.theplugin.commons.crucible.api.model;

import org.jetbrains.annotations.Nullable;

public class ReviewTestUtil {
	public static Review createReview(String url, @Nullable String prjKey, @Nullable String authorUsername,
			@Nullable String moderatorUsername) {
		if (prjKey == null) {
			prjKey = "myprojectkey";
		}
		if (authorUsername == null) {
			authorUsername = "myauthor";
		}
		return new Review(url, prjKey, new User(authorUsername),
				moderatorUsername != null ? new User(moderatorUsername) : null);
	}

	public static Review createReview(String url) {
		return createReview(url, null, null, null);
	}

}
