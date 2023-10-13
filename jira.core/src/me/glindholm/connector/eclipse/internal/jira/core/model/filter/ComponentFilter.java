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

package me.glindholm.connector.eclipse.internal.jira.core.model.filter;

import java.io.Serializable;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraComponent;

/**
 * Restricts to issues that have one of the specified components. This filter can only be used in conjunction with a
 * {@link ProjectFilter}. If no components are specified it is assumed the user is looking for issues wih no assigned
 * components. If you are looking for issues with any component, don't add a component filter.
 *
 * @see com.gbst.jira.core.model.filter.ProjectFilter
 * @author Brock Janiczak
 */
public class ComponentFilter implements Filter, Serializable {
    private static final long serialVersionUID = 1L;

    private final JiraComponent[] components;

    private final boolean containsNone;

    public ComponentFilter(final JiraComponent[] components, final boolean containsNone) {
        assert components != null;
        this.containsNone = containsNone;
        this.components = components;
    }

    public JiraComponent[] getComponents() {
        return components;
    }

    public boolean hasNoComponent() {
        return containsNone;
    }

    public ComponentFilter copy() {
        final JiraComponent[] copy = new JiraComponent[components.length];
        System.arraycopy(components, 0, copy, 0, components.length);
        return new ComponentFilter(copy, containsNone);
    }
}
