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

package me.glindholm.connector.eclipse.internal.jira.core.model;

import java.io.Serializable;

import org.eclipse.core.runtime.Assert;

/**
 * @author Steffen Pingel
 */
public class JiraSecurityLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final JiraSecurityLevel NONE = new JiraSecurityLevel("-1", JiraMessages.SecurityLevel_None); //$NON-NLS-1$

    private String id;

    private String name;

    public JiraSecurityLevel(final String id) {
        Assert.isNotNull(id);
        this.id = id;
    }

    public JiraSecurityLevel() {
    }

    public JiraSecurityLevel(final String id, final String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

}
