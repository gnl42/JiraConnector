/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.model;

import java.io.Serializable;

import org.eclipse.core.runtime.Assert;

/**
 * @author Steffen Pingel
 */
public class SecurityLevel implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	private String name;

	public SecurityLevel(String id) {
		Assert.isNotNull(id);
		this.id = id;
	}

	public SecurityLevel() {
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
