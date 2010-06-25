/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.jira.tests.core;

import junit.framework.TestCase;

import com.atlassian.connector.eclipse.internal.jira.core.service.JiraRemoteMessageException;
import com.atlassian.connector.eclipse.jira.tests.util.JiraTestUtil;

public class JiraRemoteMessageExceptionTest extends TestCase {

	public void testInvalidWorkflow4_1() throws Exception {
		JiraRemoteMessageException e = new JiraRemoteMessageException(
				JiraTestUtil.getMessage("web/invalid-workflow-4.1"));
		assertEquals(
				"It seems that you have tried to perform a workflow operation (Start Progress) that is not valid for the current state of this issue (PRONE-23538). The likely cause is that somebody has changed the issue recently, please look at the issue history for details.",
				e.getMessage());
	}

	public void testInvalidWorkflow3_13() throws Exception {
		String msg = "Workflow Action Invalid";
		JiraRemoteMessageException e = new JiraRemoteMessageException(
				JiraTestUtil.getMessage("web/invalid-workflow-3.13"));
		assertEquals(msg, e.getMessage());

	}
}
