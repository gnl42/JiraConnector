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

import com.atlassian.theplugin.commons.util.MiscUtil;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public final class RevisionData {

	private final Set<String> revisions;

	private final String source;

	private final String path;

	public RevisionData(@NotNull String source, @NotNull String path, @NotNull Collection<String> revisions) {
		this.source = source;
		this.path = path;
		this.revisions = MiscUtil.buildHashSet();
		this.revisions.addAll(revisions);
	}

	public String getSource() {
		return source;
	}

	public String getPath() {
		return path;
	}

	public Set<String> getRevisions() {
		return Collections.unmodifiableSet(revisions);
	}

}
