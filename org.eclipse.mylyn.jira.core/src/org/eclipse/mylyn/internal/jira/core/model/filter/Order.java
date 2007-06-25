/*******************************************************************************
 * Copyright (c) 2007 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.model.filter;

import java.io.Serializable;

/**
 * @author	Brock Janiczak
 */
public class Order implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Field field;

	private final boolean ascending;

	public Order(Field field, boolean ascending) {
		this.field = field;
		this.ascending = ascending;
	}

	public boolean isAscending() {
		return this.ascending;
	}

	public Field getField() {
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

	public static final class Field {
		private final String fieldName;

		public static final Field ISSUE_TYPE = new Field("type"); //$NON-NLS-1$

		public static final Field ISSUE_KEY = new Field("key"); //$NON-NLS-1$

		public static final Field SUMMARY = new Field("summary"); //$NON-NLS-1$

		public static final Field ASSIGNEE = new Field("assignee"); //$NON-NLS-1$

		public static final Field REPORTER = new Field("reporter"); //$NON-NLS-1$

		public static final Field PRIORITY = new Field("priority"); //$NON-NLS-1$

		public static final Field STATUS = new Field("status"); //$NON-NLS-1$

		public static final Field RESOLUTION = new Field("resolution"); //$NON-NLS-1$

		public static final Field CREATED = new Field("created"); //$NON-NLS-1$

		public static final Field UPDATED = new Field("updated"); //$NON-NLS-1$

		public static final Field DUE_DATE = new Field("due"); //$NON-NLS-1$

		// TODO how do we support custom fields?

		private Field(String fieldName) {
			this.fieldName = fieldName;
		}

		public String getFieldName() {
			return this.fieldName;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return this.fieldName;
		}
	}
}
