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

package com.atlassian.connector.eclipse.jira.tests.ui;

import junit.framework.TestCase;

import org.eclipse.mylyn.tasks.core.TaskRepository;

import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import com.atlassian.connector.eclipse.internal.jira.ui.JiraUiUtil;

public class JiraUiUtilTest extends TestCase {

	/**
	 * Test method for
	 * {@link com.atlassian.connector.eclipse.internal.bamboo.core.BambooUtil#isUseFavourites(org.eclipse.mylyn.tasks.core.TaskRepository)}
	 * .
	 */
	public void testAdjustEstimateOptin() {
		TaskRepository repo = new TaskRepository(JiraCorePlugin.CONNECTOR_KIND, "http://mylyn.eclipse.org");

		// default option
		assertEquals(JiraWorkLog.AdjustEstimateMethod.LEAVE, JiraUiUtil.getAdjustEstimateOption(repo));

		JiraUiUtil.updateAdjustEstimateOption(JiraWorkLog.AdjustEstimateMethod.AUTO, repo);
		assertEquals(JiraWorkLog.AdjustEstimateMethod.AUTO, JiraUiUtil.getAdjustEstimateOption(repo));

		JiraUiUtil.updateAdjustEstimateOption(JiraWorkLog.AdjustEstimateMethod.REDUCE, repo);
		assertEquals(JiraWorkLog.AdjustEstimateMethod.REDUCE, JiraUiUtil.getAdjustEstimateOption(repo));

		JiraUiUtil.updateAdjustEstimateOption(JiraWorkLog.AdjustEstimateMethod.SET, repo);
		assertEquals(JiraWorkLog.AdjustEstimateMethod.SET, JiraUiUtil.getAdjustEstimateOption(repo));

		JiraUiUtil.updateAdjustEstimateOption(JiraWorkLog.AdjustEstimateMethod.LEAVE, repo);
		assertEquals(JiraWorkLog.AdjustEstimateMethod.LEAVE, JiraUiUtil.getAdjustEstimateOption(repo));
	}
}
