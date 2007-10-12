/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests;

import org.eclipse.mylyn.internal.jira.ui.JiraConnectorUiTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Wesley Coelho (initial integration patch)
 * @author Steffen Pingel
 */
public class AllJiraTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for mylyn.jira.tests");

		// $JUnit-BEGIN$
		suite.addTestSuite(JiraCustomQueryTest.class);
		suite.addTestSuite(JiraTaskTest.class);
		suite.addTestSuite(JiraClientFacadeTest.class);
		suite.addTestSuite(JiraTaskExternalizationTest.class);
		suite.addTestSuite(JiraFilterTest.class);
		suite.addTestSuite(JiraTaskArchiveTest.class);
		suite.addTestSuite(JiraRepositoryConnectorTest.class);
		suite.addTestSuite(JiraRpcClientTest.class);
		suite.addTestSuite(JiraAttachmentHandlerTest.class);
		suite.addTestSuite(JiraTaskDataHandlerTest.class);
		suite.addTestSuite(JiraStackTraceDuplicateDetectorTest.class);

		suite.addTestSuite(JiraConnectorUiTest.class);
		// $JUnit-END$
		return suite;
	}

}
