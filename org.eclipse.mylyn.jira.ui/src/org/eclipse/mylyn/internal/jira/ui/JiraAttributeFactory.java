/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui;

import java.util.Collection;
import java.util.Date;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.internal.tasks.core.AbstractAttributeMapper;
import org.eclipse.mylyn.monitor.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.AbstractAttributeFactory;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;

/**
 * @author Mik Kersten
 * @author Steffen Pingel
 */
@SuppressWarnings("restriction")
public class JiraAttributeFactory extends AbstractAttributeFactory {

	private static final long serialVersionUID = 8000933300692372211L;

	public static final String ATTRIBUTE_TYPE = "attribute.jira.type";

	public static final String ATTRIBUTE_ISSUE_PARENT_KEY = "attribute.jira.issue_parent_key";

	public static final String ATTRIBUTE_ISSUE_PARENT_ID = "attribute.jira.issue_parent_id";

	public static final String ATTRIBUTE_ENVIRONMENT = "attribute.jira.environment";

	public static final String ATTRIBUTE_COMPONENTS = "attribute.jira.components";

	public static final String ATTRIBUTE_FIXVERSIONS = "attribute.jira.fixversions";

	public static final String ATTRIBUTE_AFFECTSVERSIONS = "attribute.jira.affectsversions";

	public static final String ATTRIBUTE_ESTIMATE = "attribute.jira.estimate";

	public static final String ATTRIBUTE_INITIAL_ESTIMATE = "attribute.jira.initialestimate";

	public static final String ATTRIBUTE_ACTUAL = "attribute.jira.actual";

	public static final String ATTRIBUTE_DUE_DATE = "attribute.jira.due";

	public static final String ATTRIBUTE_SUBTASK_IDS = "attribute.jira.subtask_ids";

	public static final String ATTRIBUTE_SUBTASK_KEYS = "attribute.jira.subtask_keys";

	public static final String ATTRIBUTE_CUSTOM_PREFIX = "attribute.jira.custom::";

	public static final String ATTRIBUTE_LINK_PREFIX = "attribute.jira.link::";

	public static final String ATTRIBUTE_SECURITY_LEVEL = "attribute.jira.security";

	public static final String ATTRIBUTE_READ_ONLY = "attribute.jira.read-only";

	public static final String JIRA_DATE_FORMAT = "dd MMM yyyy HH:mm:ss z";

	public static final String TYPE_KEY = "type";

	public static final String LINKED_IDS = "attribute.jira.link_ids";

	private final JiraAttributeMapper attributeMapper;

	public JiraAttributeFactory() {
		this.attributeMapper = new JiraAttributeMapper(this);
	}

	@Override
	public RepositoryTaskAttribute createAttribute(String key) {
		RepositoryTaskAttribute attribute = super.createAttribute(key);
		attribute.putMetaDataValue(TYPE_KEY, JiraAttribute.valueById(attribute.getId()).getKey());
		return attribute;
	}

	@Override
	public boolean isHidden(String key) {
		return JiraAttribute.valueById(key).isHidden();
	}

	@Override
	public String getName(String key) {
		return JiraAttribute.valueById(key).getName();
	}

	@Override
	public boolean isReadOnly(String key) {
		return JiraAttribute.valueById(key).isReadOnly();
	}

	@Override
	public String mapCommonAttributeKey(String key) {
		if ("summary".equals(key)) {
			return RepositoryTaskAttribute.SUMMARY;
		} else if ("description".equals(key)) {
			return RepositoryTaskAttribute.DESCRIPTION;
		} else if ("priority".equals(key)) {
			return RepositoryTaskAttribute.PRIORITY;
		} else if ("resolution".equals(key)) {
			return RepositoryTaskAttribute.RESOLUTION;
		} else if ("assignee".equals(key)) {
			return RepositoryTaskAttribute.USER_ASSIGNED;
		} else if ("environment".equals(key)) {
			return ATTRIBUTE_ENVIRONMENT;
		} else if ("issuetype".equals(key)) {
			return ATTRIBUTE_TYPE;
		} else if ("components".equals(key)) {
			return ATTRIBUTE_COMPONENTS;
		} else if ("versions".equals(key)) {
			return ATTRIBUTE_AFFECTSVERSIONS;
		} else if ("fixVersions".equals(key)) {
			return ATTRIBUTE_FIXVERSIONS;
		} else if ("timetracking".equals(key)) {
			return ATTRIBUTE_ESTIMATE;
		} else if ("duedate".equals(key)) {
			return ATTRIBUTE_DUE_DATE;
		}

		if (RepositoryTaskAttribute.COMPONENT.equals(key)) {
			return JiraAttributeFactory.ATTRIBUTE_COMPONENTS;
		}

		if (key.startsWith("issueLink")) {
			return ATTRIBUTE_LINK_PREFIX + key;
		}
		if (key.startsWith("customfield")) {
			return ATTRIBUTE_CUSTOM_PREFIX + key;
		}

		return key;
	}

//	public String mapAttributeToKey(String key) {
//		JiraAttribute attribute = JiraAttribute.valueById(key);
//		if (!attribute.equals(JiraAttribute.UNKNOWN)) {
//			return attribute.getParamName();
//		}
//		if (key.startsWith(ATTRIBUTE_CUSTOM_PREFIX)) {
//			return key.substring(ATTRIBUTE_CUSTOM_PREFIX.length());
//		}
//		return key;
//	}

	@Override
	public Date getDateForAttributeType(String attributeKey, String dateString) {
		if (dateString == null || dateString.equals("")) {
			return null;
		}
		try {
			// RepositoryTaskAttribute.DATE_MODIFIED
			// RepositoryTaskAttribute.DATE_CREATION
			return JiraUtils.stringToDate(dateString);
		} catch (Exception e) {
			StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraUiPlugin.PLUGIN_ID,
					"Error parsing date for attribute \"" + attributeKey + "\": \"" + dateString + "\"", e));
			return null;
		}
	}

	@Override
	public AbstractAttributeMapper getAttributeMapper() {
		return attributeMapper;
	}

	private class JiraAttributeMapper extends AbstractAttributeMapper {

		public JiraAttributeMapper(AbstractAttributeFactory attributeFactory) {
			super(attributeFactory);
		}

		@Override
		public String getType(RepositoryTaskAttribute taskAttribute) {
			if (RepositoryTaskAttribute.DATE_CREATION.equals(taskAttribute.getId())
					|| RepositoryTaskAttribute.DATE_MODIFIED.equals(taskAttribute.getId())) {
				return RepositoryTaskAttribute.TYPE_DATE;
			}

			JiraFieldType type = JiraFieldType.valueByKey(taskAttribute.getMetaDataValue(JiraAttributeFactory.TYPE_KEY));
			Collection<String> options = taskAttribute.getOptions();
			if (type.equals(JiraFieldType.SELECT)
					&& (options == null || options.isEmpty() || taskAttribute.isReadOnly())) {
				type = JiraFieldType.TEXTFIELD;
			} else if (type.equals(JiraFieldType.MULTISELECT) && (options == null || options.isEmpty())) {
				type = JiraFieldType.TEXTFIELD;
			}

			switch (type) {
			case DATEPICKER:
				return RepositoryTaskAttribute.TYPE_DATE;
			case ISSUELINK:
				return RepositoryTaskAttribute.TYPE_TASK_DEPENDENCY;
			case ISSUELINKS:
				return RepositoryTaskAttribute.TYPE_TASK_DEPENDENCY;
			case MULTISELECT:
				return RepositoryTaskAttribute.TYPE_MULTI_SELECT;
			case SELECT:
				return RepositoryTaskAttribute.TYPE_SINGLE_SELECT;
			case TEXTAREA:
				return RepositoryTaskAttribute.TYPE_LONG_TEXT;
			default:
				return RepositoryTaskAttribute.TYPE_SHORT_TEXT;
			}
		}

	}

}