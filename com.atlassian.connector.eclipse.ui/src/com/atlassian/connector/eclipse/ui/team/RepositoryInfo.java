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

package com.atlassian.connector.eclipse.ui.team;

public class RepositoryInfo {
	private final String scmPath;

	private final String name;

	public RepositoryInfo(String scmPath, String name) {
		this.scmPath = scmPath;
		this.name = name;
	}

	public String getScmPath() {
		return scmPath;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return (name != null ? name + ": " : "") + scmPath;
	}
}