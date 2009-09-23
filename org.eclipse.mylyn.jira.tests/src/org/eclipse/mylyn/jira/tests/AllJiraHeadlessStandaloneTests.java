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

package org.eclipse.mylyn.jira.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.mylyn.jira.tests.client.JiraClientOfflineTest;
import org.eclipse.mylyn.jira.tests.client.JiraClientTest;
import org.eclipse.mylyn.jira.tests.client.JiraRssHandlerTest;
import org.eclipse.mylyn.jira.tests.client.JiraWebClientTest;
import org.eclipse.mylyn.jira.tests.core.FilterDefinitionConverterTest;
import org.eclipse.mylyn.jira.tests.core.JiraTimeFormatTest;
import org.eclipse.mylyn.jira.tests.model.JiraVersionTest;
import org.eclipse.mylyn.jira.tests.util.JiraFixture;

/**
 * @author Steffen Pingel
 */
public class AllJiraHeadlessStandaloneTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Headless Standalone Tests for org.eclipse.mylyn.jira.tests");
		suite.addTestSuite(JiraTimeFormatTest.class);
		suite.addTestSuite(JiraClientOfflineTest.class);
		suite.addTestSuite(FilterDefinitionConverterTest.class);
		suite.addTestSuite(JiraRssHandlerTest.class);
		suite.addTestSuite(JiraVersionTest.class);
		// repository tests
		for (JiraFixture fixture : JiraFixture.ALL) {
			TestSuite fixtureSuite = fixture.createSuite();
			fixture.add(fixtureSuite, JiraClientTest.class);
			fixture.add(fixtureSuite, JiraWebClientTest.class);
			suite.addTest(fixtureSuite);
		}
		return suite;
	}

}
