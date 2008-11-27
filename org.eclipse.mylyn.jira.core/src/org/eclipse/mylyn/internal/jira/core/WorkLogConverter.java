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
 */
public class WorkLogConverter extends AbstractComplexAttributeConverter<JiraWorkLog> {

	private static List<JiraField<?>> _taskFields = new ArrayList<JiraField<?>>();

	public final static JiraField<IRepositoryPerson> AUTOR = create(IRepositoryPerson.class, "author", "Author",
			TaskAttribute.TYPE_PERSON);

	public final static JiraField<String> COMMENT = create(String.class, "comment", "Comment",
			TaskAttribute.TYPE_LONG_TEXT);

	public final static JiraField<Date> CREATION_DATE = create(Date.class, "created", "Created",
			TaskAttribute.TYPE_DATETIME);

	public final static JiraField<String> GROUP_LEVEL = create(String.class, "groupLevel", "Group Level",
			TaskAttribute.TYPE_DATETIME);

	public final static JiraField<String> ID = create(String.class, "id", "Created", TaskAttribute.TYPE_SHORT_TEXT);

	public final static JiraField<Date> MODIFICATION_DATE = create(Date.class, "updated", "Created",
			TaskAttribute.TYPE_DATETIME);

	public static final String PREFIX_WORKLOG = "attribute.jira.worklog-";

	public final static JiraField<String> ROLE_LEVEL_ID = create(String.class, "roleLevelId", "Created",
			TaskAttribute.TYPE_SHORT_TEXT);

	public final static JiraField<Date> START_DATE = create(Date.class, "startDate", "Created",
			TaskAttribute.TYPE_DATETIME);

	public final static JiraField<Long> TIME_SPENT = create(Long.class, "timeSpent", "Time", TaskAttribute.TYPE_LONG);

	public static final String TYPE_WORKLOG = "jira.worklog";

	public final static JiraField<IRepositoryPerson> UPDATE_AUTHOR = create(IRepositoryPerson.class, "updateAuthor",
			"Created", TaskAttribute.TYPE_PERSON);

	private static <T> JiraField<T> create(Class<T> clazz, String key, String label, String type) {
		JiraField<T> field = new JiraField<T>(clazz, "attribute.jira.worklog." + key, key, label, type);
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
