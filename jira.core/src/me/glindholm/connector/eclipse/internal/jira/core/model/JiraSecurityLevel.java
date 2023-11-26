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
import java.util.ArrayList;
import java.util.List;

import me.glindholm.jira.rest.client.api.domain.SecurityLevel;

/**
 * @author Steffen Pingel
 */
public class JiraSecurityLevel implements Serializable {
    private static final long serialVersionUID = -6036656360935034900L;

    public static final String ID_NONE = "-1";

    public static final JiraSecurityLevel NONE = new JiraSecurityLevel(ID_NONE, JiraMessages.SecurityLevel_None, "None"); //$NON-NLS-1$

    private final String id;

    private final String name;

    private final String description;

    private JiraSecurityLevel() {
        this("", "", "");
    }

    public JiraSecurityLevel(final Long id, final String name, final String description) {
        this(id + "", name, description);
    }

    public JiraSecurityLevel(final String id) {
        this(id, null, null);
    }

    public JiraSecurityLevel(final String id, final String name, final String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static List<JiraSecurityLevel> convert(final List<SecurityLevel> securityLevels) {
        final List<JiraSecurityLevel> levels = new ArrayList<>(securityLevels.size());
        for (final SecurityLevel level : securityLevels) {
            levels.add(convert(level));
        }
        return levels;
    }

    public static JiraSecurityLevel convert(final SecurityLevel level) {
        return new JiraSecurityLevel(level.getId(), level.getName(), level.getDescription());
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("JiraSecurityLevel [id=").append(id).append(", name=").append(name).append(", description=").append(description).append("]");
        return builder.toString();
    }

}
