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
import java.util.List;

public final class Revision {

	private final List<Link> links;

	private final String revision;

	private final String path;

	public List<Link> getLink() {
		return links;
	}

	public String getRevision() {
		return revision;
	}

	public String getPath() {
		return path;
	}

	public Revision(String revision, String path, Collection<Link> links) {
		this.revision = revision;
		this.path = path;
		if (links == null) {
			this.links = MiscUtil.buildArrayList();
		} else {
			this.links = MiscUtil.buildArrayList(links);
		}
	}

}
