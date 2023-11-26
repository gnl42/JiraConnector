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

package me.glindholm.connector.eclipse.internal.jira.core.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Steffen Pingel
 * @author Jacek Jaroczynski
 */
public class JiraIssueField implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;

    private final String name;

    private String type;

    private boolean required;

    private List<JiraAllowedValue> allowedValues = Collections.emptyList();

    public JiraIssueField(final String id, final String name) {
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
        return "JiraIssueField [id=" + id + ", name=" + name + ", type=" + type + "]";
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setRequired(final boolean required) {
        this.required = required;
    }

    public boolean isRequired() {
        return required;
    }

    public void setAllowedValues(final List<JiraAllowedValue> allowedValues) {
        this.allowedValues = allowedValues;
    }

    public List<JiraAllowedValue> getAlloweValues() {
        return allowedValues;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 17;
        result = prime * result + (id == null ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof final JiraIssueField other) || !Objects.equals(id, other.id)) {
            return false;
        }

        return true;
    }

}
