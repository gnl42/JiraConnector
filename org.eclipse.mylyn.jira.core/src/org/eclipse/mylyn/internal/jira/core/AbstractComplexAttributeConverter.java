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

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.internal.jira.core.TaskSchema.TaskField;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;

public abstract class AbstractComplexAttributeConverter<T> {

	private final List<? extends TaskField<?>> fields;

	public AbstractComplexAttributeConverter(List<? extends TaskField<?>> fields) {
		this.fields = fields;
	}

	protected abstract T newInstance();

	public T createFrom(TaskAttribute taskAttribute) {
		Assert.isNotNull(taskAttribute);
		T instance = newInstance();
		for (TaskField<?> field : fields) {
			TaskAttribute child = taskAttribute.getAttribute(field.key());
			if (child != null) {
				setJavaField(instance, field, child);
			}
		}
		return instance;
	}

	public void applyTo(T instance, TaskAttribute taskAttribute) {
		Assert.isNotNull(taskAttribute);
		for (TaskField<?> field : fields) {
			TaskAttribute child = addAttribute(taskAttribute, field);
			setAttributeValue(instance, field, child);
		}
	}

	private boolean setJavaField(T instance, TaskField<?> taskField, TaskAttribute attribute) {
		if (taskField.javaKey() != null) {
			Field field;
			try {
				field = instance.getClass().getDeclaredField(taskField.javaKey());
				field.setAccessible(true);
				Object value;
				TaskAttributeMapper mapper = attribute.getTaskData().getAttributeMapper();
				if (TaskAttribute.TYPE_DATE.equals(taskField.getType())) {
					value = mapper.getDateValue(attribute);
				} else if (TaskAttribute.TYPE_DATETIME.equals(taskField.getType())) {
					value = mapper.getDateValue(attribute);
				} else if (TaskAttribute.TYPE_INTEGER.equals(taskField.getType())) {
					value = mapper.getIntegerValue(attribute);
					if (value == null) {
						value = 0;
					}
				} else if (TaskAttribute.TYPE_LONG.equals(taskField.getType())) {
					value = mapper.getLongValue(attribute);
					if (value == null) {
						value = 0;
					}
				} else {
					value = attribute.getValue();
				}
				field.set(instance, value);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	private boolean setAttributeValue(T instance, TaskField<?> taskField, TaskAttribute attribute) {
		Field field;
		try {
			field = instance.getClass().getDeclaredField(taskField.javaKey());
			field.setAccessible(true);
			Object value = field.get(instance);
			TaskAttributeMapper mapper = attribute.getTaskData().getAttributeMapper();
			if (value == null) {
				attribute.clearValues();
			} else {
				if (TaskAttribute.TYPE_DATE.equals(taskField.getType())) {
					mapper.setDateValue(attribute, (Date) value);
				} else if (TaskAttribute.TYPE_DATETIME.equals(taskField.getType())) {
					mapper.setDateValue(attribute, (Date) value);
				} else if (TaskAttribute.TYPE_INTEGER.equals(taskField.getType())) {
					mapper.setIntegerValue(attribute, (Integer) value);
				} else if (TaskAttribute.TYPE_LONG.equals(taskField.getType())) {
					mapper.setLongValue(attribute, (Long) value);
				} else {
					attribute.setValue(value.toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public TaskAttribute addAttribute(TaskAttribute parent, TaskField<?> field) {
		TaskAttribute attribute = parent.createAttribute(field.key());
		// meta data
		TaskAttributeMetaData metaData = attribute.getMetaData();
		metaData.setLabel(field.getLabel());
		metaData.setType(field.getType());
		metaData.setReadOnly(field.isReadOnly());
		metaData.setKind(field.getKind());
		// options
		Map<String, String> options = ((ITaskAttributeMapper2) parent.getTaskData().getAttributeMapper()).getRepositoryOptions(attribute);
		if (options != null) {
			for (Entry<String, String> option : options.entrySet()) {
				attribute.putOption(option.getKey(), option.getValue());
			}
		}
		return attribute;
	}
}
