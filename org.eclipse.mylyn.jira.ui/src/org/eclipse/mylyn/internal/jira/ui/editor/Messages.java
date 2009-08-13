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

package org.eclipse.mylyn.internal.jira.ui.editor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.mylyn.internal.jira.ui.editor.messages"; //$NON-NLS-1$

	public static String WorkLogPart_Adjust_Estimate;

	public static String WorkLogPart_Auto_Adjust;

	public static String WorkLogPart_Auto_Adjust_Explanation_Tooltip;

	public static String WorkLogPart_Creator;

	public static String WorkLogPart_Date;

	public static String WorkLogPart_Description;

	public static String WorkLogPart_Leave_Existing_Estimate;

	public static String WorkLogPart_Leave_Existing_Explanation_Tooltip;

	public static String WorkLogPart_Log_Work_Done;

	public static String WorkLogPart_No_work_logged;

	public static String WorkLogPart_Start_Date;

	public static String WorkLogPart_Time_Spent;

	public static String WorkLogPart_Time_Spent_Explanation_Tooltip;

	public static String JiraEditorUtil_Time_Spent_Error_Decorator_Hover;

	public static String WorkLogPart_Work_Description;

	public static String WorkLogPart_Work_Log;

	public static String WorkLogPart_Worked;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
