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

package me.glindholm.connector.eclipse.internal.jira.core;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;

import me.glindholm.connector.eclipse.internal.jira.core.TaskSchema.TaskField;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraWorkLog.AdjustEstimateMethod;
import me.glindholm.jira.rest.client.api.domain.BasicUser;

/**
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 */
public class WorkLogConverter {

    private static List<JiraField<?>> _taskFields = new ArrayList<>();

    public final static JiraField<IRepositoryPerson> AUTOR = create(IRepositoryPerson.class, "author", Messages.WorkLogConverter_Author, //$NON-NLS-1$
            TaskAttribute.TYPE_PERSON);

    public final static JiraField<String> COMMENT = create(String.class, "comment", Messages.WorkLogConverter_Comment, //$NON-NLS-1$
            TaskAttribute.TYPE_LONG_TEXT);

    public final static JiraField<Instant> CREATION_DATE = create(Instant.class, "created", Messages.WorkLogConverter_Created, //$NON-NLS-1$
            TaskAttribute.TYPE_DATETIME);

    public final static JiraField<String> GROUP_LEVEL = create(String.class, "groupLevel", Messages.WorkLogConverter_Group_Level, //$NON-NLS-1$
            TaskAttribute.TYPE_DATETIME);

    public final static JiraField<String> ID = create(String.class, "id", Messages.WorkLogConverter_Id, TaskAttribute.TYPE_SHORT_TEXT); //$NON-NLS-1$

    public final static JiraField<Instant> MODIFICATION_DATE = create(Instant.class, "updated", Messages.WorkLogConverter_Updated, //$NON-NLS-1$
            TaskAttribute.TYPE_DATETIME);

    public static final String PREFIX_WORKLOG = "attribute.jira.worklog-"; //$NON-NLS-1$

    public static final String ATTRIBUTE_WORKLOG_NEW = "attribute.jira.worklog.new"; //$NON-NLS-1$

    public static final String ATTRIBUTE_WORKLOG_NEW_SUBMIT_FLAG = "attribute.jira.worklog.new.submit.flag"; //$NON-NLS-1$

    public final static JiraField<String> ROLE_LEVEL_ID = create(String.class, "roleLevelId", Messages.WorkLogConverter_Role_Level, //$NON-NLS-1$
            TaskAttribute.TYPE_SHORT_TEXT);

    public final static JiraField<Instant> START_DATE = create(Instant.class, "startDate", Messages.WorkLogConverter_Start_Date, //$NON-NLS-1$
            TaskAttribute.TYPE_DATETIME);

    public final static JiraField<Long> TIME_SPENT = create(Long.class, "timeSpent", Messages.WorkLogConverter_Time, TaskAttribute.TYPE_LONG); //$NON-NLS-1$

    public static final String TYPE_WORKLOG = "jira.worklog"; //$NON-NLS-1$

    public final static JiraField<IRepositoryPerson> UPDATE_AUTHOR = create(IRepositoryPerson.class, "updateAuthor", //$NON-NLS-1$
            Messages.WorkLogConverter_Author, TaskAttribute.TYPE_PERSON);

    private static final String ADJUST_ESTIMATE_KEY = "attribute.jira.worklog.adjustEstimate"; //$NON-NLS-1$

    private static <T> JiraField<T> create(final Class<T> clazz, final String key, final String label, final String type) {
        final JiraField<T> field = new JiraField<>(clazz, "attribute.jira.worklog." + key, key, label, type); //$NON-NLS-1$
        _taskFields.add(field);
        return field;
    }

    public static List<JiraField<?>> taskFields() {
        return Collections.unmodifiableList(_taskFields);
    }

    public WorkLogConverter() {
        fields = taskFields();
    }

    protected JiraWorkLog newInstance() {
        return new JiraWorkLog();
    }

    private final List<? extends TaskField<?>> fields;

    public JiraWorkLog createFrom(final TaskAttribute taskAttribute) {
        Assert.isNotNull(taskAttribute);
        final JiraWorkLog instance = newInstance();
        for (final TaskField<?> field : fields) {
            final TaskAttribute child = taskAttribute.getAttribute(field.key());
            if (child != null) {
                setJavaField(instance, field, child);
            }
        }

        final TaskAttribute child = taskAttribute.getAttribute(ADJUST_ESTIMATE_KEY);
        if (child != null) {
            instance.setAdjustEstimate(AdjustEstimateMethod.fromValue(child.getValue()));
        } else {
            instance.setAdjustEstimate(AdjustEstimateMethod.AUTO);
        }

        return instance;
    }

    public void applyTo(final JiraWorkLog instance, final TaskAttribute taskAttribute) {
        Assert.isNotNull(taskAttribute);
        for (final TaskField<?> field : fields) {
            final TaskAttribute child = addAttribute(taskAttribute, field);
            setAttributeValue(instance, field, child);
        }

        if (instance.getAdjustEstimate() != null) {
            final TaskAttribute child = addAttribute(taskAttribute,
                    new JiraField<>(String.class, ADJUST_ESTIMATE_KEY, "adjustEstimate", "Adjust Estimate", TaskAttribute.TYPE_SHORT_TEXT)); //$NON-NLS-1$ //$NON-NLS-2$
            child.setValue(instance.getAdjustEstimate().value());
        }
    }

    private boolean setJavaField(final JiraWorkLog instance, final TaskField<?> taskField, final TaskAttribute attribute) {
        if (taskField.javaKey() != null) {
            Field field;
            try {
                field = instance.getClass().getDeclaredField(taskField.javaKey());
                field.setAccessible(true);
                Object value = null;
                final TaskAttributeMapper mapper = attribute.getTaskData().getAttributeMapper();
                if (TaskAttribute.TYPE_DATE.equals(taskField.getType())) {
                    @Nullable
                    final Date date = mapper.getDateValue(attribute);
                    if (date != null) {
                        value = date.toInstant();
                    }
                } else if (TaskAttribute.TYPE_DATETIME.equals(taskField.getType())) {
                    @Nullable
                    final Date dateTime = mapper.getDateValue(attribute);
                    if (dateTime != null) {
                        value = dateTime.toInstant();
                    }
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
                } else if (TaskAttribute.TYPE_PERSON.equals(taskField.getType())) {
                    value = ((ITaskAttributeMapper2) attribute.getTaskData().getAttributeMapper()).getRepositoryUser(attribute);
                } else {
                    value = attribute.getValue();
                }
                field.set(instance, value);
            } catch (final Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private boolean setAttributeValue(final JiraWorkLog instance, final TaskField<?> taskField, final TaskAttribute attribute) {
        Field field;
        try {
            field = instance.getClass().getDeclaredField(taskField.javaKey());
            field.setAccessible(true);
            final Object value = field.get(instance);
            final TaskAttributeMapper mapper = attribute.getTaskData().getAttributeMapper();
            if (value == null) {
                attribute.clearValues();
            } else {
                if (TaskAttribute.TYPE_DATE.equals(taskField.getType())) {
                    mapper.setDateValue(attribute, Date.from((Instant) value));
                } else if (TaskAttribute.TYPE_DATETIME.equals(taskField.getType())) {
                    mapper.setDateValue(attribute, Date.from((Instant) value));
                } else if (TaskAttribute.TYPE_INTEGER.equals(taskField.getType())) {
                    mapper.setIntegerValue(attribute, (Integer) value);
                } else if (TaskAttribute.TYPE_LONG.equals(taskField.getType())) {
                    mapper.setLongValue(attribute, (Long) value);
                } else if (TaskAttribute.TYPE_PERSON.equals(taskField.getType())) {
                    final BasicUser user = (BasicUser) value;
                    final ITaskAttributeMapper2 jiraMapper = (ITaskAttributeMapper2) attribute.getTaskData().getAttributeMapper();
                    jiraMapper.setRepositoryUser(attribute, user);
                } else {
                    attribute.setValue(value.toString());
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public TaskAttribute addAttribute(final TaskAttribute parent, final TaskField<?> field) {
        final TaskAttribute attribute = parent.createAttribute(field.key());
        // meta data
        final TaskAttributeMetaData metaData = attribute.getMetaData();
        metaData.setLabel(field.getLabel());
        metaData.setType(field.getType());
        metaData.setReadOnly(field.isReadOnly());
        metaData.setKind(field.getKind());
        // options
        final ITaskAttributeMapper2 mapper2 = (ITaskAttributeMapper2) parent.getTaskData().getAttributeMapper();
        final Map<String, String> options = mapper2.getRepositoryOptions(attribute);
        if (options != null) {
            for (final Entry<String, String> option : options.entrySet()) {
                attribute.putOption(option.getKey(), option.getValue());
            }
        }
        return attribute;
    }
}
