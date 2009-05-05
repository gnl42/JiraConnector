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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.mylyn.internal.jira.core.model.JiraWorkLog;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

/**
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 */
public class WorkLogConverter extends AbstractComplexAttributeConverter<JiraWorkLog> {

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

	public static final JiraField<Boolean> ADJUST_ESTIMATE = create(Boolean.class,
			"autoAdjustEstimate", Messages.WorkLogConverter_Auto_Adjust_Estimate, TaskAttribute.TYPE_BOOLEAN); //$NON-NLS-1$

	private static <T> JiraField<T> create(Class<T> clazz, String key, String label, String type) {
		JiraField<T> field = new JiraField<T>(clazz, "attribute.jira.worklog." + key, key, label, type); //$NON-NLS-1$
		_taskFields.add(field);
		return field;
	}

	public static List<JiraField<?>> taskFields() {
		return Collections.unmodifiableList(_taskFields);
	}

	public WorkLogConverter() {
		super(taskFields());
	}

	@Override
	protected JiraWorkLog newInstance() {
		return new JiraWorkLog();
	}

}
