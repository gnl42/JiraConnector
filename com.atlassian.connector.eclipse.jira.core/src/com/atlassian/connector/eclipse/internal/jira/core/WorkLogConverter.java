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

package com.atlassian.connector.eclipse.internal.jira.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;

import com.atlassian.connector.eclipse.internal.jira.core.TaskSchema.TaskField;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog.AdjustEstimateMethod;

/**
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 */
public class WorkLogConverter {

	private static List<JiraField<?>> _taskFields = new ArrayList<JiraField<?>>();

	public final static JiraField<IRepositoryPerson> AUTOR = create(IRepositoryPerson.class,
			"author", Messages.WorkLogConverter_Author, //$NON-NLS-1$
			TaskAttribute.TYPE_PERSON);

	public final static JiraField<String> COMMENT = create(String.class, "comment", Messages.WorkLogConverter_Comment, //$NON-NLS-1$
			TaskAttribute.TYPE_LONG_TEXT);

	public final static JiraField<Date> CREATION_DATE = create(Date.class,
			"created", Messages.WorkLogConverter_Created, //$NON-NLS-1$
			TaskAttribute.TYPE_DATETIME);

	public final static JiraField<String> GROUP_LEVEL = create(String.class,
			"groupLevel", Messages.WorkLogConverter_Group_Level, //$NON-NLS-1$
			TaskAttribute.TYPE_DATETIME);

	public final static JiraField<String> ID = create(String.class,
			"id", Messages.WorkLogConverter_Id, TaskAttribute.TYPE_SHORT_TEXT); //$NON-NLS-1$

	public final static JiraField<Date> MODIFICATION_DATE = create(Date.class,
			"updated", Messages.WorkLogConverter_Updated, //$NON-NLS-1$
			TaskAttribute.TYPE_DATETIME);

	public static final String PREFIX_WORKLOG = "attribute.jira.worklog-"; //$NON-NLS-1$

	public static final String ATTRIBUTE_WORKLOG_NEW = "attribute.jira.worklog.new"; //$NON-NLS-1$

	public static final String ATTRIBUTE_WORKLOG_NEW_SUBMIT_FLAG = "attribute.jira.worklog.new.submit.flag"; //$NON-NLS-1$

	public final static JiraField<String> ROLE_LEVEL_ID = create(String.class,
			"roleLevelId", Messages.WorkLogConverter_Role_Level, //$NON-NLS-1$
			TaskAttribute.TYPE_SHORT_TEXT);

	public final static JiraField<Date> START_DATE = create(Date.class,
			"startDate", Messages.WorkLogConverter_Start_Date, //$NON-NLS-1$
			TaskAttribute.TYPE_DATETIME);

	public final static JiraField<Long> TIME_SPENT = create(Long.class,
			"timeSpent", Messages.WorkLogConverter_Time, TaskAttribute.TYPE_LONG); //$NON-NLS-1$

	public static final String TYPE_WORKLOG = "jira.worklog"; //$NON-NLS-1$

	public final static JiraField<IRepositoryPerson> UPDATE_AUTHOR = create(IRepositoryPerson.class, "updateAuthor", //$NON-NLS-1$
			Messages.WorkLogConverter_Author, TaskAttribute.TYPE_PERSON);

	private static final String ADJUST_ESTIMATE_KEY = "attribute.jira.worklog.adjustEstimate"; //$NON-NLS-1$

	private static <T> JiraField<T> create(Class<T> clazz, String key, String label, String type) {
		JiraField<T> field = new JiraField<T>(clazz, "attribute.jira.worklog." + key, key, label, type); //$NON-NLS-1$
		_taskFields.add(field);
		return field;
	}

	public static List<JiraField<?>> taskFields() {
		return Collections.unmodifiableList(_taskFields);
	}

	public WorkLogConverter() {
		this.fields = taskFields();
	}

	protected JiraWorkLog newInstance() {
		return new JiraWorkLog();
	}

	private final List<? extends TaskField<?>> fields;

	public JiraWorkLog createFrom(TaskAttribute taskAttribute) {
		Assert.isNotNull(taskAttribute);
		JiraWorkLog instance = newInstance();
		for (TaskField<?> field : fields) {
			TaskAttribute child = taskAttribute.getAttribute(field.key());
			if (child != null) {
				setJavaField(instance, field, child);
			}
		}

		TaskAttribute child = taskAttribute.getAttribute(ADJUST_ESTIMATE_KEY);
		if (child != null) {
			instance.setAdjustEstimate(AdjustEstimateMethod.fromValue(child.getValue()));
		} else {
			instance.setAdjustEstimate(AdjustEstimateMethod.AUTO);
		}

		return instance;
	}

	public void applyTo(JiraWorkLog instance, TaskAttribute taskAttribute) {
		Assert.isNotNull(taskAttribute);
		for (TaskField<?> field : fields) {
			TaskAttribute child = addAttribute(taskAttribute, field);
			setAttributeValue(instance, field, child);
		}

		if (instance.getAdjustEstimate() != null) {
			TaskAttribute child = addAttribute(taskAttribute, new JiraField<String>(String.class, ADJUST_ESTIMATE_KEY,
					"adjustEstimate", "Adjust Estimate", TaskAttribute.TYPE_SHORT_TEXT)); //$NON-NLS-1$ //$NON-NLS-2$
			child.setValue(instance.getAdjustEstimate().value());
		}
	}

	private boolean setJavaField(JiraWorkLog instance, TaskField<?> taskField, TaskAttribute attribute) {
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
				} else if (TaskAttribute.TYPE_BOOLEAN.equals(taskField.getType())) {
					value = mapper.getBooleanValue(attribute);
					if (value == null) {
						value = false;
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

	private boolean setAttributeValue(JiraWorkLog instance, TaskField<?> taskField, TaskAttribute attribute) {
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
