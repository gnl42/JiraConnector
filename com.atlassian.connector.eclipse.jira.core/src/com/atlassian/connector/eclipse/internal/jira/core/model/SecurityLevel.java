/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core.model;

import java.io.Serializable;

import org.eclipse.core.runtime.Assert;

/**
 * @author Steffen Pingel
 */
public class SecurityLevel implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final SecurityLevel NONE = new SecurityLevel("-1", Messages.SecurityLevel_None); //$NON-NLS-1$

	private String id;

	private String name;

	public SecurityLevel(String id) {
		Assert.isNotNull(id);
		this.id = id;
	}

	public SecurityLevel() {
	}

	public SecurityLevel(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

}
