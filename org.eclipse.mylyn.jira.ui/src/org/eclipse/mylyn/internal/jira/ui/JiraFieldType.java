/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui;

/**
 * @author Eugene Kuleshov
 */
public enum JiraFieldType {
	 TEXTFIELD("com.atlassian.jira.plugin.system.customfieldtypes:textfield"),
	 TEXTAREA("com.atlassian.jira.plugin.system.customfieldtypes:textarea"),
	 
	 SELECT("com.atlassian.jira.plugin.system.customfieldtypes:select"),
	 MULTISELECT("com.atlassian.jira.plugin.system.customfieldtypes:multiselect"),
	 CASCADINGSELECT("com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect"),

	 MULTICHECKBOXES("com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes"),
	 RADIOBUTTONS("com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons"),
	 
	 DATEPICKER("com.atlassian.jira.plugin.system.customfieldtypes:datepicker"),
	 DATETIME("com.atlassian.jira.plugin.system.customfieldtypes:datetime"),
	 
	 VERSION("com.atlassian.jira.plugin.system.customfieldtypes:version"),
	 MULTIVERSION("com.atlassian.jira.plugin.system.customfieldtypes:multiversion"),
	 
	 USERPICKER("com.atlassian.jira.plugin.system.customfieldtypes:userpicker"),
	 MULTIUSERPICKER("com.atlassian.jira.plugin.system.customfieldtypes:multiuserpicker"),
	 GROUPPICKER("com.atlassian.jira.plugin.system.customfieldtypes:grouppicker"),
	 MULTIGROUPPICKER("com.atlassian.jira.plugin.system.customfieldtypes:multigrouppicker"),
	 
	 PROJECT("com.atlassian.jira.plugin.system.customfieldtypes:project"),
	 URL("com.atlassian.jira.plugin.system.customfieldtypes:url"),
	 FLOATFIELD("com.atlassian.jira.plugin.system.customfieldtypes:float"),
	 READONLYFIELD("com.atlassian.jira.plugin.system.customfieldtypes:readonlyfield"),
	 
	 // read only fields from JIRA Toolkit plugin
	 ASSIGNEEDOMAIN("com.atlassian.jira.toolkit:assigneedomain"),
	 ATTACHMENTS("com.atlassian.jira.toolkit:attachments "),
	 COMMENTS("com.atlassian.jira.toolkit:comments"),
	 DAYSLASTCOMMENTED("com.atlassian.jira.toolkit:dayslastcommented"),
	 LASTUSERCOMMENTED("com.atlassian.jira.toolkit:lastusercommented"),
	 MESSAGE("com.atlassian.jira.toolkit:message"),
	 MULTIKEYFIELD("com.atlassian.jira.toolkit:multikeyfield"),
	 MULTIPROJECT("com.atlassian.jira.toolkit:multiproject"),
	 ORIGINALESTIMATE("com.atlassian.jira.toolkit:originalestimate"),
	 PARTICIPANTS("com.atlassian.jira.toolkit:participants"),
	 REPORTERDOMAIN("com.atlassian.jira.toolkit:reporterdomain"),
	 RESOLVEDDATE("com.atlassian.jira.toolkit:resolveddate"),
	 SUPPORTTOOLS("com.atlassian.jira.toolkit:supporttools"),
	 USERPROPERTY("com.atlassian.jira.toolkit:userproperty"),
	 VELOCITYMESSAGE("com.atlassian.jira.toolkit:velocitymessage"),
	 VELOCITYVIEWMESSAGE("com.atlassian.jira.toolkit:velocityviewmessage"),
	 VIEWMESSAGE("com.atlassian.jira.toolkit:viewmessage"), 

	 ISSUELINK("org.mylar.jira.issuelink"), // field that has link to a single issue
	 ISSUELINKS("org.mylar.jira.issuelinks"), // field that has list of links to issues

	 UNKNOWN(null); 

	 private String key;

	 JiraFieldType(String key) {
		this.key = key;
	 }
	 
	 public String getKey() {
		return key;
	}

	public static JiraFieldType valueByKey(String key) {
		if (key != null) {
			for (JiraFieldType type : values()) {
				if (key.equals(type.getKey())) {
					return type;
				}
			}
		}
		return JiraFieldType.UNKNOWN;
	}
	
}

