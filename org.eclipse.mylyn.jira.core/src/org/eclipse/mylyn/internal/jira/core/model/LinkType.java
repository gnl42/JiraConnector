/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.model;

import java.io.Serializable;

// TODO need a service to populate this information
// Could discover it while creating issues, but this seems dodgey at best

/**
 * @author Brock Janiczak
 */
public class LinkType implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	private String name;

	private String style;

	private String inwardsDescription;

	private String outwardsDescription;

//	private boolean isSubTaskLinkType;
//
//	private boolean isSystemLinkType;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getInwardsDescription() {
		return this.inwardsDescription;
	}

	public void setInwardsDescription(String inwardsDescription) {
		this.inwardsDescription = inwardsDescription;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOutwardsDescription() {
		return this.outwardsDescription;
	}

	public void setOutwardsDescription(String outwardsDescription) {
		this.outwardsDescription = outwardsDescription;
	}

	public String getStyle() {
		return this.style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

}
