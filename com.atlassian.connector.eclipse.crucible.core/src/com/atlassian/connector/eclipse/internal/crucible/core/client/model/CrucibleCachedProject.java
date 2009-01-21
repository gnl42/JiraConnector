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

package com.atlassian.connector.eclipse.internal.crucible.core.client.model;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;

import java.io.Serializable;

/**
 * Cached Project information
 * 
 * @author Shawn Minto
 */
public class CrucibleCachedProject implements Serializable {

	private static final long serialVersionUID = 5778501312205967958L;

	private final String id;

	private final String name;

	private final String key;

	public CrucibleCachedProject(String id, String name, String key) {
		this.id = id;
		this.name = name;
		this.key = key;
	}

	public CrucibleCachedProject(CrucibleProject project) {
		this(project.getId(), project.getName(), project.getKey());
	}

	public String getId() {
		return id;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}
}
