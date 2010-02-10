/*******************************************************************************
 * Copyright (c) 2004, 2009 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

/**
 * @author Eugene Kuleshov
 */
public enum JiraFieldType {

	CASCADINGSELECT("com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect"), //$NON-NLS-1$

	DATE("com.atlassian.jira.plugin.system.customfieldtypes:datepicker", TaskAttribute.TYPE_DATE), //$NON-NLS-1$

	DATETIME("com.atlassian.jira.plugin.system.customfieldtypes:datetime", TaskAttribute.TYPE_DATE), //$NON-NLS-1$

	FLOATFIELD("com.atlassian.jira.plugin.system.customfieldtypes:float"), //$NON-NLS-1$

	GROUPPICKER("com.atlassian.jira.plugin.system.customfieldtypes:grouppicker"), //$NON-NLS-1$

	// field that has link to a single issue
	ISSUELINK("org.mylar.jira.issuelink", TaskAttribute.TYPE_TASK_DEPENDENCY), //$NON-NLS-1$

	// field that has list of links to issues
	ISSUELINKS("org.mylar.jira.issuelinks", TaskAttribute.TYPE_TASK_DEPENDENCY), //$NON-NLS-1$

	LABELS("com.atlassian.jira.plugin.labels:labels", TaskAttribute.TYPE_SHORT_TEXT), //$NON-NLS-1$

	MULTICHECKBOXES("com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes"), //$NON-NLS-1$

	MULTIGROUPPICKER("com.atlassian.jira.plugin.system.customfieldtypes:multigrouppicker"), //$NON-NLS-1$

	MULTISELECT("com.atlassian.jira.plugin.system.customfieldtypes:multiselect", TaskAttribute.TYPE_MULTI_SELECT), //$NON-NLS-1$

	MULTIUSERPICKER("com.atlassian.jira.plugin.system.customfieldtypes:multiuserpicker", TaskAttribute.TYPE_PERSON), //$NON-NLS-1$

	MULTIVERSION("com.atlassian.jira.plugin.system.customfieldtypes:multiversion"), //$NON-NLS-1$

	PROJECT("com.atlassian.jira.plugin.system.customfieldtypes:project", TaskAttribute.TYPE_SINGLE_SELECT), //$NON-NLS-1$

	RADIOBUTTONS("com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons"), //$NON-NLS-1$

	READONLYFIELD("com.atlassian.jira.plugin.system.customfieldtypes:readonlyfield"), //$NON-NLS-1$

	SELECT("com.atlassian.jira.plugin.system.customfieldtypes:select", TaskAttribute.TYPE_SINGLE_SELECT), //$NON-NLS-1$

	TEXTAREA("com.atlassian.jira.plugin.system.customfieldtypes:textarea", TaskAttribute.TYPE_LONG_TEXT), //$NON-NLS-1$

	TEXTFIELD("com.atlassian.jira.plugin.system.customfieldtypes:textfield", TaskAttribute.TYPE_SHORT_TEXT), //$NON-NLS-1$

	// read only fields from JIRA Toolkit plug-in

	TOOLKIT_ASSIGNEEDOMAIN("com.atlassian.jira.toolkit:assigneedomain"), //$NON-NLS-1$

	TOOLKIT_ATTACHMENTS("com.atlassian.jira.toolkit:attachments "), //$NON-NLS-1$

	TOOLKIT_COMMENTS("com.atlassian.jira.toolkit:comments"), //$NON-NLS-1$

	TOOLKIT_DAYSLASTCOMMENTED("com.atlassian.jira.toolkit:dayslastcommented"), //$NON-NLS-1$

	TOOLKIT_LASTUSERCOMMENTED("com.atlassian.jira.toolkit:lastusercommented"), //$NON-NLS-1$

	TOOLKIT_MESSAGE("com.atlassian.jira.toolkit:message"), //$NON-NLS-1$

	TOOLKIT_MULTIKEYFIELD("com.atlassian.jira.toolkit:multikeyfield"), //$NON-NLS-1$

	TOOLKIT_MULTIPROJECT("com.atlassian.jira.toolkit:multiproject"), //$NON-NLS-1$

	TOOLKIT_ORIGINALESTIMATE("com.atlassian.jira.toolkit:originalestimate"), //$NON-NLS-1$

	TOOLKIT_PARTICIPANTS("com.atlassian.jira.toolkit:participants"), //$NON-NLS-1$

	TOOLKIT_REPORTERDOMAIN("com.atlassian.jira.toolkit:reporterdomain"), //$NON-NLS-1$

	TOOLKIT_RESOLVEDDATE("com.atlassian.jira.toolkit:resolveddate"), //$NON-NLS-1$

	TOOLKIT_SUPPORTTOOLS("com.atlassian.jira.toolkit:supporttools"), //$NON-NLS-1$

	TOOLKIT_USERPROPERTY("com.atlassian.jira.toolkit:userproperty"), //$NON-NLS-1$

	TOOLKIT_VELOCITYMESSAGE("com.atlassian.jira.toolkit:velocitymessage"), //$NON-NLS-1$

	TOOLKIT_VELOCITYVIEWMESSAGE("com.atlassian.jira.toolkit:velocityviewmessage"), //$NON-NLS-1$

	TOOLKIT_VIEWMESSAGE("com.atlassian.jira.toolkit:viewmessage"), //$NON-NLS-1$

	UNKNOWN(null),

	URL("com.atlassian.jira.plugin.system.customfieldtypes:url", TaskAttribute.TYPE_URL), //$NON-NLS-1$

	USERPICKER("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", TaskAttribute.TYPE_PERSON), //$NON-NLS-1$

	VERSION("com.atlassian.jira.plugin.system.customfieldtypes:version"); //$NON-NLS-1$

	public static JiraFieldType fromKey(String key) {
		if (key != null) {
			for (JiraFieldType type : values()) {
				if (key.equals(type.getKey())) {
					return type;
				}
			}
		}
		return JiraFieldType.UNKNOWN;
	}

	private final String key;

	private final String taskType;

	private JiraFieldType(String key) {
		this.key = key;
		this.taskType = null;
	}

	private JiraFieldType(String key, String taskType) {
		this.key = key;
		this.taskType = taskType;
	}

	public String getKey() {
		return key;
	}

	public String getTaskType() {
		return taskType;
	}

}
