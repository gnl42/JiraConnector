/*******************************************************************************
 * Copyright (c) 2004, 2008 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core.model.filter;

import java.io.Serializable;

/**
 * @author Brock Janiczak
 */
public class Order implements Serializable {
	private static final long serialVersionUID = 1L;

	private final JiraFields field;

	private final boolean ascending;

	public Order(JiraFields field, boolean ascending) {
		this.field = field;
		this.ascending = ascending;
	}

	public boolean isAscending() {
		return this.ascending;
	}

	public JiraFields getField() {
		return field;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "order by " + field.toString() + " " + (this.ascending ? "asc" : "desc"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
