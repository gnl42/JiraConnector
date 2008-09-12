/*******************************************************************************
* Copyright (c) 2004, 2008 Tasktop Technologies and others.
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

import org.eclipse.mylyn.jira.tests.client.JiraClientTest;
import org.eclipse.mylyn.jira.tests.client.JiraWebClientTest;
import org.eclipse.mylyn.jira.tests.ui.JiraConnectorUiTest;
import org.eclipse.mylyn.jira.tests.ui.JiraTimeFormatTest;

/**
 * @author Wesley Coelho (initial integration patch)
 * @author Steffen Pingel
 */
public class AllJiraTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for mylyn.jira.tests");
		// $JUnit-BEGIN$
		suite.addTestSuite(JiraTimeFormatTest.class);
		suite.addTestSuite(JiraCustomQueryTest.class);
		suite.addTestSuite(JiraClientFacadeTest.class);
		suite.addTestSuite(JiraTaskExternalizationTest.class);
		suite.addTestSuite(JiraFilterTest.class);
		suite.addTestSuite(JiraRepositoryConnectorTest.class);
		suite.addTestSuite(JiraClientTest.class);
		suite.addTestSuite(JiraWebClientTest.class);
		suite.addTestSuite(JiraTaskAttachmentHandlerTest.class);
		suite.addTestSuite(JiraTaskDataHandlerTest.class);
		suite.addTestSuite(JiraStackTraceDuplicateDetectorTest.class);
		suite.addTestSuite(JiraConnectorUiTest.class);
		// $JUnit-END$
		return suite;
	}

}
