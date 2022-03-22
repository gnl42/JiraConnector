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

package me.glindholm.connector.eclipse.internal.jira.core;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

/**
 * @author Eugene Kuleshov
 */
public enum JiraFieldType {

	CASCADINGSELECT("me.glindholm.jira.plugin.system.customfieldtypes:cascadingselect"), //$NON-NLS-1$

	DATE("me.glindholm.jira.plugin.system.customfieldtypes:datepicker", TaskAttribute.TYPE_DATE), //$NON-NLS-1$

	DATETIME("me.glindholm.jira.plugin.system.customfieldtypes:datetime", TaskAttribute.TYPE_DATETIME), //$NON-NLS-1$

	FLOATFIELD("me.glindholm.jira.plugin.system.customfieldtypes:float", IJiraConstants.TYPE_NUMBER), //$NON-NLS-1$

	GROUPPICKER("me.glindholm.jira.plugin.system.customfieldtypes:grouppicker", TaskAttribute.TYPE_SHORT_TEXT), //$NON-NLS-1$

	// field that has link to a single issue
	ISSUELINK("org.mylar.jira.issuelink", TaskAttribute.TYPE_TASK_DEPENDENCY), //$NON-NLS-1$

	// field that has list of links to issues
	ISSUELINKS("org.mylar.jira.issuelinks", TaskAttribute.TYPE_TASK_DEPENDENCY), //$NON-NLS-1$

	LABELS("me.glindholm.jira.plugin.labels:labels", TaskAttribute.TYPE_SHORT_TEXT), //$NON-NLS-1$
	LABELSS("me.glindholm.jira.plugin.system.customfieldtypes:labels", TaskAttribute.TYPE_SHORT_TEXT), //$NON-NLS-1$

	MULTICHECKBOXES(
			"me.glindholm.jira.plugin.system.customfieldtypes:multicheckboxes", TaskAttribute.TYPE_MULTI_SELECT), //$NON-NLS-1$

	MULTIGROUPPICKER(
			"me.glindholm.jira.plugin.system.customfieldtypes:multigrouppicker", TaskAttribute.TYPE_SHORT_TEXT), //$NON-NLS-1$

	MULTISELECT("me.glindholm.jira.plugin.system.customfieldtypes:multiselect", TaskAttribute.TYPE_MULTI_SELECT), //$NON-NLS-1$

	MULTIUSERPICKER("me.glindholm.jira.plugin.system.customfieldtypes:multiuserpicker", TaskAttribute.TYPE_PERSON), //$NON-NLS-1$

	MULTIVERSION("me.glindholm.jira.plugin.system.customfieldtypes:multiversion"), //$NON-NLS-1$

	PROJECT("me.glindholm.jira.plugin.system.customfieldtypes:project", TaskAttribute.TYPE_SINGLE_SELECT), //$NON-NLS-1$

	RADIOBUTTONS("me.glindholm.jira.plugin.system.customfieldtypes:radiobuttons", TaskAttribute.TYPE_SINGLE_SELECT), //$NON-NLS-1$

	READONLYFIELD("me.glindholm.jira.plugin.system.customfieldtypes:readonlyfield"), //$NON-NLS-1$

	SELECT("me.glindholm.jira.plugin.system.customfieldtypes:select", TaskAttribute.TYPE_SINGLE_SELECT), //$NON-NLS-1$

	TEXTAREA("me.glindholm.jira.plugin.system.customfieldtypes:textarea", TaskAttribute.TYPE_LONG_TEXT), //$NON-NLS-1$

	TEXTFIELD("me.glindholm.jira.plugin.system.customfieldtypes:textfield", TaskAttribute.TYPE_SHORT_TEXT), //$NON-NLS-1$

	// read only fields from JIRA Toolkit plug-in

	TOOLKIT_ASSIGNEEDOMAIN("me.glindholm.jira.toolkit:assigneedomain"), //$NON-NLS-1$

	TOOLKIT_ATTACHMENTS("me.glindholm.jira.toolkit:attachments "), //$NON-NLS-1$

	TOOLKIT_COMMENTS("me.glindholm.jira.toolkit:comments"), //$NON-NLS-1$

	TOOLKIT_DAYSLASTCOMMENTED("me.glindholm.jira.toolkit:dayslastcommented"), //$NON-NLS-1$

	TOOLKIT_LASTUSERCOMMENTED("me.glindholm.jira.toolkit:lastusercommented"), //$NON-NLS-1$

	TOOLKIT_MESSAGE("me.glindholm.jira.toolkit:message"), //$NON-NLS-1$

	TOOLKIT_MULTIKEYFIELD("me.glindholm.jira.toolkit:multikeyfield"), //$NON-NLS-1$

	TOOLKIT_MULTIPROJECT("me.glindholm.jira.toolkit:multiproject"), //$NON-NLS-1$

	TOOLKIT_ORIGINALESTIMATE("me.glindholm.jira.toolkit:originalestimate"), //$NON-NLS-1$

	TOOLKIT_PARTICIPANTS("me.glindholm.jira.toolkit:participants"), //$NON-NLS-1$

	TOOLKIT_REPORTERDOMAIN("me.glindholm.jira.toolkit:reporterdomain"), //$NON-NLS-1$

	TOOLKIT_RESOLVEDDATE("me.glindholm.jira.toolkit:resolveddate"), //$NON-NLS-1$

	TOOLKIT_SUPPORTTOOLS("me.glindholm.jira.toolkit:supporttools"), //$NON-NLS-1$

	TOOLKIT_USERPROPERTY("me.glindholm.jira.toolkit:userproperty"), //$NON-NLS-1$

	TOOLKIT_VELOCITYMESSAGE("me.glindholm.jira.toolkit:velocitymessage"), //$NON-NLS-1$

	TOOLKIT_VELOCITYVIEWMESSAGE("me.glindholm.jira.toolkit:velocityviewmessage"), //$NON-NLS-1$

	TOOLKIT_VIEWMESSAGE("me.glindholm.jira.toolkit:viewmessage"), //$NON-NLS-1$

	UNKNOWN(null),

	RANK("com.pyxis.greenhopper.jira:gh-global-rank"), //$NON-NLS-1$

	URL("me.glindholm.jira.plugin.system.customfieldtypes:url", TaskAttribute.TYPE_URL), //$NON-NLS-1$

	USERPICKER("me.glindholm.jira.plugin.system.customfieldtypes:userpicker", TaskAttribute.TYPE_PERSON), //$NON-NLS-1$

	VERSION("me.glindholm.jira.plugin.system.customfieldtypes:version"), //$NON-NLS-1$

	EPIC_LABEL("com.pyxis.greenhopper.jira:gh-epic-label", TaskAttribute.TYPE_SHORT_TEXT); //$NON-NLS-1$

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
