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

package org.eclipse.mylar.internal.jira.core.model;

import java.io.Serializable;

/**
 * @author	Brock Janiczak
 */
public class Priority implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String BLOCKER_ID = "1"; //$NON-NLS-1$

	public static final String CRITICAL_ID = "2"; //$NON-NLS-1$

	public static final String MAJOR_ID = "3"; //$NON-NLS-1$

	public static final String MINOR_ID = "4"; //$NON-NLS-1$

	public static final String TRIVIAL_ID = "5"; //$NON-NLS-1$

	private String id;

	private String name;

	private String description;

	private String icon;

	private String colour;

	public String getColour() {
		return this.colour;
	}

	public void setColour(String colour) {
		this.colour = colour;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIcon() {
		return this.icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (!(obj instanceof Priority))
			return false;

		Priority that = (Priority) obj;

		return this.id.equals(that.id);
	}

	public int hashCode() {
		return id.hashCode();
	}

	public String toString() {
		return this.name;
	}
}
