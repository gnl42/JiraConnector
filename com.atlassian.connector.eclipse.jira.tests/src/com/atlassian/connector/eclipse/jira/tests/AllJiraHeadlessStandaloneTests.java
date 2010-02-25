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

package com.atlassian.connector.eclipse.jira.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.atlassian.connector.eclipse.jira.tests.client.JiraClientOfflineTest;
import com.atlassian.connector.eclipse.jira.tests.client.JiraClientTest;
import com.atlassian.connector.eclipse.jira.tests.client.JiraRssHandlerTest;
import com.atlassian.connector.eclipse.jira.tests.client.JiraWebClientTest;
import com.atlassian.connector.eclipse.jira.tests.core.FilterDefinitionConverterTest;
import com.atlassian.connector.eclipse.jira.tests.core.JiraClientCacheTest;
import com.atlassian.connector.eclipse.jira.tests.core.JiraTimeFormatTest;
import com.atlassian.connector.eclipse.jira.tests.model.ComponentFilterTest;
import com.atlassian.connector.eclipse.jira.tests.model.JiraVersionTest;
import com.atlassian.connector.eclipse.jira.tests.model.VersionFilterTest;
import com.atlassian.connector.eclipse.jira.tests.ui.WdhmUtilTest;
import com.atlassian.connector.eclipse.jira.tests.util.JiraFixture;

/**
 * @author Steffen Pingel
 */
public class AllJiraHeadlessStandaloneTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Headless Standalone Tests for com.atlassian.connector.eclipse.jira.tests");
		suite.addTestSuite(JiraTimeFormatTest.class);
		suite.addTestSuite(JiraClientOfflineTest.class);
		suite.addTestSuite(FilterDefinitionConverterTest.class);
		suite.addTestSuite(JiraRssHandlerTest.class);
		suite.addTestSuite(JiraVersionTest.class);
		suite.addTestSuite(JiraClientCacheTest.class);
		suite.addTestSuite(WdhmUtilTest.class);
		suite.addTestSuite(VersionFilterTest.class);
		suite.addTestSuite(ComponentFilterTest.class);
		// repository tests
		for (JiraFixture fixture : JiraFixture.ALL) {
			fixture.createSuite(suite);
			fixture.add(JiraClientTest.class);
			fixture.add(JiraWebClientTest.class);
			fixture.done();
		}
		return suite;
	}

}
