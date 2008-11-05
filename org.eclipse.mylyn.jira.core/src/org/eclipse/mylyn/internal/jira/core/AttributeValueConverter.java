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

package org.eclipse.mylyn.internal.jira.core;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

public class AttributeValueConverter<T> {

	private final Class<T> clazz;

	private final String type;

	public AttributeValueConverter(Class<T> clazz, String type) {
		this.clazz = clazz;
		this.type = type;
	}

	public T getValue(TaskAttribute attribute) {
		return (T) attribute.getTaskData().getAttributeMapper().getValue(attribute);
	}

	public void setValue(TaskAttribute attribute, T value) {
		attribute.getTaskData().getAttributeMapper().setValue(attribute, value.toString());
	}

}
