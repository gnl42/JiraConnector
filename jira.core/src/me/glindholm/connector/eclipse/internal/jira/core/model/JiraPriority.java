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
import java.net.URI;

import org.eclipse.core.runtime.Assert;

/**
 * @author Brock Janiczak
 */
public class JiraPriority implements Serializable {

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

    private URI self;

    public JiraPriority(final String id) {
        Assert.isNotNull(id);
        this.id = id;
    }

    public JiraPriority(final String id, final String name, final String description, final String icon, final String colour) {
        this(id);
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.colour = colour;
    }

    public String getColour() {
        return colour;
    }

    public URI getSelf() {
        return self;
    }

    public void setColour(final String colour) {
        this.colour = colour;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(final String icon) {
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public boolean equals(final Object obj) {
        if ((obj == null) || !(obj instanceof JiraPriority)) {
            return false;
        }

        final JiraPriority that = (JiraPriority) obj;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    public void setSelf(final URI self) {
        this.self = self;
    }
}
