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

package me.glindholm.connector.eclipse.internal.jira.core.model;

import java.io.Serializable;

// TODO need a service to populate this information
// Could discover it while creating issues, but this seems dodgey at best

/**
 * @author Brock Janiczak
 */
public class JiraLinkType implements Serializable {

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
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getInwardsDescription() {
        return inwardsDescription;
    }

    public void setInwardsDescription(final String inwardsDescription) {
        this.inwardsDescription = inwardsDescription;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getOutwardsDescription() {
        return outwardsDescription;
    }

    public void setOutwardsDescription(final String outwardsDescription) {
        this.outwardsDescription = outwardsDescription;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(final String style) {
        this.style = style;
    }

}
