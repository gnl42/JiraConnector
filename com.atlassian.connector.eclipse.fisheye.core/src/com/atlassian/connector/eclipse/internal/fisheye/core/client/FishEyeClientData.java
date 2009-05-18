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

package com.atlassian.connector.eclipse.internal.fisheye.core.client;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Cached offline FishEye data used by Mylyn
 * 
 * @author Wojciech Seliga
 */
public class FishEyeClientData implements Serializable {

	private static final long serialVersionUID = -2403572323195301936L;

	private Set<String> cachedRepositories;

	public FishEyeClientData() {
	}

	public boolean hasData() {
		return cachedRepositories != null;
	}

	public void setRepositories(Collection<String> repositories) {
		cachedRepositories = new HashSet<String>(repositories);
	}

	public Set<String> getCachedRepositories() {
		if (cachedRepositories != null) {
			return Collections.unmodifiableSet(cachedRepositories);
		} else {
			return Collections.emptySet();
		}
	}
}
