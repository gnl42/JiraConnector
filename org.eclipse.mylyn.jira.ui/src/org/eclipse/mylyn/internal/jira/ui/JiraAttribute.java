/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui;

import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;

/**
 * @author Eugene Kuleshov
 */
public enum JiraAttribute {
	ISSUE_KEY(RepositoryTaskAttribute.TASK_KEY, JiraFieldType.TEXTFIELD, "Issue ID:"),

	SUMMARY(RepositoryTaskAttribute.SUMMARY, JiraFieldType.TEXTFIELD, "Summary:", true, false, "summary"),
	DESCRIPTION(RepositoryTaskAttribute.DESCRIPTION, JiraFieldType.TEXTFIELD, "Description:", true, false, "description"),
	STATUS(RepositoryTaskAttribute.STATUS, JiraFieldType.SELECT, "Status:"),
	RESOLUTION(RepositoryTaskAttribute.RESOLUTION, JiraFieldType.SELECT, "Resolution:", true, false, "resolution"),

	DATE_CREATION(RepositoryTaskAttribute.DATE_CREATION, JiraFieldType.TEXTFIELD, "Created:"),
	DATE_MODIFIED(RepositoryTaskAttribute.DATE_MODIFIED, JiraFieldType.TEXTFIELD, "Modified:"),

	USER_ASSIGNED(RepositoryTaskAttribute.USER_ASSIGNED, JiraFieldType.USERPICKER, "Assigned to:", true, true, "assignee"),
	USER_REPORTER(RepositoryTaskAttribute.USER_REPORTER, JiraFieldType.USERPICKER, "Reported by:"),

	PRODUCT(RepositoryTaskAttribute.PRODUCT, JiraFieldType.PROJECT, "Project:", false, true),
	PRIORITY(RepositoryTaskAttribute.PRIORITY, JiraFieldType.SELECT, "Priority:", false, false, "priority"),

	TYPE(JiraAttributeFactory.ATTRIBUTE_TYPE, JiraFieldType.SELECT, "Type:", false, false, "issuetype"),
	PARENT_KEY(JiraAttributeFactory.ATTRIBUTE_ISSUE_PARENT_KEY, JiraFieldType.ISSUELINK, "Parent:", false, true),

	COMPONENTS(JiraAttributeFactory.ATTRIBUTE_COMPONENTS, JiraFieldType.MULTISELECT, "Components:", false, false, "components"),
	AFFECTSVERSIONS(JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS, JiraFieldType.MULTISELECT, "Affects Versions:", false, false, "versions"),
	FIXVERSIONS(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS, JiraFieldType.MULTISELECT, "Fix Versions:", false, false, "fixVersions"),

	ESTIMATE(JiraAttributeFactory.ATTRIBUTE_ESTIMATE, JiraFieldType.TEXTFIELD, "Estimate:", false, false, "timetracking"),
	ENVIRONMENT(JiraAttributeFactory.ATTRIBUTE_ENVIRONMENT, JiraFieldType.TEXTAREA, "Environment:", false, false, "environment"),

	COMMENT_NEW(RepositoryTaskAttribute.COMMENT_NEW, JiraFieldType.TEXTAREA, "New Comment:", true, false, "comment"),

	COMMENT_AUTHOR(RepositoryTaskAttribute.COMMENT_AUTHOR, JiraFieldType.TEXTAREA, "Author:"),
	COMMENT_TEXT(RepositoryTaskAttribute.COMMENT_TEXT, JiraFieldType.TEXTAREA, "Comment:"),
	COMMENT_DATE(RepositoryTaskAttribute.COMMENT_DATE, JiraFieldType.TEXTAREA, "Date:"),

	SUBTASK_IDS(JiraAttributeFactory.ATTRIBUTE_SUBTASK_IDS, JiraFieldType.TEXTFIELD, "Subtask ids:", true, true),
	SUBTASK_KEYS(JiraAttributeFactory.ATTRIBUTE_SUBTASK_KEYS, JiraFieldType.ISSUELINKS, "Sub-Tasks:", false, true),

	UNKNOWN(null, JiraFieldType.UNKNOWN, "unknown:", true, true); 
	
	private final String id;
	private final String name;
	private final JiraFieldType type;
	private final boolean isHidden;
	private final boolean isReadOnly;
	private final String paramName;

	private JiraAttribute(String id, JiraFieldType type, String name) {
		this.id = id;
		this.type = type;
		this.name = name;
		this.isHidden = true;
		this.isReadOnly = true;
		this.paramName = null;
	}

	private JiraAttribute(String id, JiraFieldType type, String name, boolean isHidden, boolean isReadOnly) {
		this.id = id;
		this.type = type;
		this.name = name;
		this.isHidden = isHidden;
		this.isReadOnly = isReadOnly;
		this.paramName = null;
	}

	private JiraAttribute(String id, JiraFieldType type, String name, boolean isHidden, boolean isReadOnly, String paramName) {
		this.id = id;
		this.type = type;
		this.name = name;
		this.isHidden = isHidden;
		this.isReadOnly = isReadOnly;
		this.paramName = paramName;
	}

	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public JiraFieldType getType() {
		return type;
	}
	
	public String getKey() {
		return type.getKey();
	}

	public boolean isHidden() {
		return isHidden;
	}
	
	public boolean isReadOnly() {
		return isReadOnly;
	}
	
	public static JiraAttribute valueById(String id) {
		for (JiraAttribute attribute : values()) {
			if(id.equals(attribute.getId())) {
				return attribute;
			}
		}
		return UNKNOWN;
	}

	public String getParamName() {
		return paramName;
	}

}
