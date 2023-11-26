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

package me.glindholm.connector.eclipse.internal.jira.core;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

/**
 * @author Steffen Pingel
 */
public class AttributeValueConverter<T> {

    private final Class<T> clazz;

    private final String type;

    public AttributeValueConverter(final Class<T> clazz, final String type) {
        this.clazz = clazz;
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public T getValue(final TaskAttribute attribute) {
        return (T) attribute.getTaskData().getAttributeMapper().getValue(attribute);
    }

    public void setValue(final TaskAttribute attribute, final T value) {
        attribute.getTaskData().getAttributeMapper().setValue(attribute, value.toString());
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public String getType() {
        return type;
    }

}
