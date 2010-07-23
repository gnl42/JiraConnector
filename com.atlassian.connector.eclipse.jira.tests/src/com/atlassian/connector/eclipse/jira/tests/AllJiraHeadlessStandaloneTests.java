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

import com.atlassian.connector.eclipse.jira.tests.client.JiraClientTest;
import com.atlassian.connector.eclipse.jira.tests.util.JiraFixture;

/**
 * @author Steffen Pingel
 */
public class AllJiraHeadlessStandaloneTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Headless Standalone Tests for com.atlassian.connector.eclipse.jira.tests");
		/*suite.addTestSuite(JiraTimeFormatTest.class);
		suite.addTestSuite(JiraClientOfflineTest.class);
		suite.addTestSuite(FilterDefinitionConverterTest.class);
		suite.addTestSuite(JiraRssHandlerTest.class);
		suite.addTestSuite(JiraVersionTest.class);
		suite.addTestSuite(JiraClientCacheTest.class);
		suite.addTestSuite(WdhmUtilTest.class);
		suite.addTestSuite(VersionFilterTest.class);
		suite.addTestSuite(ComponentFilterTest.class);
		suite.addTestSuite(JiraCommentDateComparatorTest.class);
		suite.addTestSuite(JiraRemoteMessageExceptionTest.class);
		suite.addTestSuite(JiraUiUtilTest.class);*/
		// repository tests
		for (JiraFixture fixture : new JiraFixture[] { JiraFixture.DEFAULT }) {
			fixture.createSuite(suite);
			fixture.add(JiraClientTest.class);
			//fixture.add(JiraWebClientTest.class);
			fixture.done();
		}
		return suite;
	}
}
