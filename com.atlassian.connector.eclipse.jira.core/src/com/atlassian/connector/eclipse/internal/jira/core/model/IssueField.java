/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
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

/**
 * @author Steffen Pingel
 * @author Jacek Jaroczynski
 */
public class IssueField implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String id;

	private final String name;

	private String type;

	private boolean required;

	public IssueField(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[id=" + id + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isRequired() {
		return this.required;
	}

}
