/*******************************************************************************
 * Copyright (c) 2004, 2008 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

/**
 * @author Eugene Kuleshov
 */
public enum JiraFieldType {

	CASCADINGSELECT("com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect"),

	DATE("com.atlassian.jira.plugin.system.customfieldtypes:datepicker", TaskAttribute.TYPE_DATE),

	DATETIME("com.atlassian.jira.plugin.system.customfieldtypes:datetime", TaskAttribute.TYPE_DATE),

	FLOATFIELD("com.atlassian.jira.plugin.system.customfieldtypes:float"),

	GROUPPICKER("com.atlassian.jira.plugin.system.customfieldtypes:grouppicker"),

	// field that has link to a single issue
	ISSUELINK("org.mylar.jira.issuelink", TaskAttribute.TYPE_TASK_DEPENDENCY),

	// field that has list of links to issues
	ISSUELINKS("org.mylar.jira.issuelinks", TaskAttribute.TYPE_TASK_DEPENDENCY),

	MULTICHECKBOXES("com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes"),

	MULTIGROUPPICKER("com.atlassian.jira.plugin.system.customfieldtypes:multigrouppicker"),

	MULTISELECT("com.atlassian.jira.plugin.system.customfieldtypes:multiselect", TaskAttribute.TYPE_MULTI_SELECT),

	MULTIUSERPICKER("com.atlassian.jira.plugin.system.customfieldtypes:multiuserpicker", TaskAttribute.TYPE_PERSON),

	MULTIVERSION("com.atlassian.jira.plugin.system.customfieldtypes:multiversion"),

	PROJECT("com.atlassian.jira.plugin.system.customfieldtypes:project", TaskAttribute.TYPE_SINGLE_SELECT),

	RADIOBUTTONS("com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons"),

	READONLYFIELD("com.atlassian.jira.plugin.system.customfieldtypes:readonlyfield"),

	SELECT("com.atlassian.jira.plugin.system.customfieldtypes:select", TaskAttribute.TYPE_SINGLE_SELECT),

	TEXTAREA("com.atlassian.jira.plugin.system.customfieldtypes:textarea", TaskAttribute.TYPE_LONG_TEXT),

	TEXTFIELD("com.atlassian.jira.plugin.system.customfieldtypes:textfield", TaskAttribute.TYPE_SHORT_TEXT),

	// read only fields from JIRA Toolkit plug-in

	TOOLKIT_ASSIGNEEDOMAIN("com.atlassian.jira.toolkit:assigneedomain"),

	TOOLKIT_ATTACHMENTS("com.atlassian.jira.toolkit:attachments "),

	TOOLKIT_COMMENTS("com.atlassian.jira.toolkit:comments"),

	TOOLKIT_DAYSLASTCOMMENTED("com.atlassian.jira.toolkit:dayslastcommented"),

	TOOLKIT_LASTUSERCOMMENTED("com.atlassian.jira.toolkit:lastusercommented"),

	TOOLKIT_MESSAGE("com.atlassian.jira.toolkit:message"),

	TOOLKIT_MULTIKEYFIELD("com.atlassian.jira.toolkit:multikeyfield"),

	TOOLKIT_MULTIPROJECT("com.atlassian.jira.toolkit:multiproject"),

	TOOLKIT_ORIGINALESTIMATE("com.atlassian.jira.toolkit:originalestimate"),

	TOOLKIT_PARTICIPANTS("com.atlassian.jira.toolkit:participants"),

	TOOLKIT_REPORTERDOMAIN("com.atlassian.jira.toolkit:reporterdomain"),

	TOOLKIT_RESOLVEDDATE("com.atlassian.jira.toolkit:resolveddate"),

	TOOLKIT_SUPPORTTOOLS("com.atlassian.jira.toolkit:supporttools"),

	TOOLKIT_USERPROPERTY("com.atlassian.jira.toolkit:userproperty"),

	TOOLKIT_VELOCITYMESSAGE("com.atlassian.jira.toolkit:velocitymessage"),

	TOOLKIT_VELOCITYVIEWMESSAGE("com.atlassian.jira.toolkit:velocityviewmessage"),

	TOOLKIT_VIEWMESSAGE("com.atlassian.jira.toolkit:viewmessage"),

	UNKNOWN(null),

	URL("com.atlassian.jira.plugin.system.customfieldtypes:url", TaskAttribute.TYPE_URL),

	USERPICKER("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", TaskAttribute.TYPE_PERSON),

	VERSION("com.atlassian.jira.plugin.system.customfieldtypes:version");

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
