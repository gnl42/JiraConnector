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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.atlassian.connector.eclipse.internal.jira.core.messages"; //$NON-NLS-1$

	static {
		// load message values from bundle file
		reloadMessages();
	}

	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String JiraAttribute_Affects_Versions;

	public static String JiraAttribute_Assigned_to;

	public static String JiraAttribute_Components;

	public static String JiraAttribute_Created;

	public static String JiraAttribute_Description;

	public static String JiraAttribute_DUEDATE;

	public static String JiraAttribute_Environment;

	public static String JiraAttribute_Estimate;

	public static String JiraAttribute_Fix_Versions;

	public static String JiraAttribute_Key;

	public static String JiraAttribute_Linked_ids;

	public static String JiraAttribute_Modified;

	public static String JiraAttribute_New_Comment;

	public static String JiraAttribute_Viewable_by;

	public static String JiraAttribute_Original_Estimate;

	public static String JiraAttribute_Parent;

	public static String JiraAttribute_Parent_ID;

	public static String JiraAttribute_Priority;

	public static String JiraAttribute_Project;

	public static String JiraAttribute_Reported_by;

	public static String JiraAttribute_Resolution;

	public static String JiraAttribute_Security_Level;

	public static String JiraAttribute_Status;

	public static String JiraAttribute_Subtask_ids;

	public static String JiraAttribute_Subtasks;

	public static String JiraAttribute_Summary;

	public static String JiraAttribute_Time_Spent;

	public static String JiraAttribute_Type;

	public static String JiraAttribute_unknown;

	public static String JiraAttribute_URL;

	public static String JiraAttribute_Votes;

	public static String JiraCorePlugin_JIRA_description;

	public static String JiraRepositoryConnector_Getting_changed_tasks;

	public static String JiraRepositoryConnector_The_JIRA_query_is_invalid;

	public static String JiraRepositoryConnector_Query_Repository;

	public static String JiraRepositoryConnector_The_repository_returned_an_unknown_project;

	public static String JiraTaskAttachmentHandler_Getting_attachment;

	public static String JiraTaskAttachmentHandler_Sending_attachment;

	public static String JiraTaskDataHandler_Creating_subtask;

	public static String JiraTaskDataHandler_Getting_task;

	public static String JiraTaskDataHandler_Leave;

	public static String JiraTaskDataHandler_Leave_as_X;

	public static String JiraTaskDataHandler_Sending_task;

	public static String WorkLogConverter_Auto_Adjust_Estimate;

	public static String WorkLogConverter_Author;

	public static String WorkLogConverter_Comment;

	public static String WorkLogConverter_Created;

	public static String WorkLogConverter_Group_Level;

	public static String WorkLogConverter_Id;

	public static String WorkLogConverter_Role_Level;

	public static String WorkLogConverter_Start_Date;

	public static String WorkLogConverter_Time;

	public static String WorkLogConverter_Updated;
}
