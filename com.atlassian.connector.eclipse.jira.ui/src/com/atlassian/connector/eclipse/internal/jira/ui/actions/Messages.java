/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.ui.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.atlassian.connector.eclipse.internal.jira.ui.actions.messages"; //$NON-NLS-1$

	public static String JiraAction_Issue_Changed;

	public static String JiraAction_Issue_Refresh_Try_Again;

	public static String JiraAction_Issue_Refresh;

	public static String JiraConnectorUiActions_Cannot_get_task_data;

	public static String JiraConnectorUiActions_Cannot_vote;

	public static String JiraConnectorUiActions_Reported_or_issue_closed;

	public static String JiraConnectorUiActions_Starting_to_watch_issue;

	public static String JiraConnectorUiActions_Starting_to_watch_issue_failed;

	public static String JiraConnectorUiActions_Vote;

	public static String JiraConnectorUiActions_Voting_failed;

	public static String JiraConnectorUiActions_Voting_for_issue;

	public static String JiraConnectorUiActions_Watch;

	public static String JiraUiUtil_Cannot_parse_logged_activity_time;

	public static String JiraUiUtil_Logged_activity_time_problem;

	public static String StartWorkAction_Cannot_parse_Mylyn_activity_delta;

	public static String StartWorkAction_cannot_perform;

	public static String StartWorkAction_enabled_for_single_selection;

	public static String StartWorkAction_start;

	public static String StartWorkAction_Start_Work_Failed;

	public static String StartWorkAction_Start_Work_Not_Available;

	public static String StartWorkAction_Start_Work;

	public static String StartWorkAction_Starting_Work;

	public static String StartWorkAction_Stop_Work_Failed;

	public static String StartWorkAction_Stop_Work;

	public static String StartWorkAction_Stoping_Work;

	public static String StartWorkAction_stop;

	public static String SelectWorkflowAction_Start_Title;

	public static String SelectWorkflowAction_Stop_Title;

	public static String SelectWorkflowAction_Label;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
