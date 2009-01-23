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

import org.eclipse.mylyn.jira.tests.core.JiraClientFactoryTest;
import org.eclipse.mylyn.jira.tests.core.JiraCustomQueryTest;
import org.eclipse.mylyn.jira.tests.core.FilterDefinitionConverterTest;
import org.eclipse.mylyn.jira.tests.core.JiraFilterTest;
import org.eclipse.mylyn.jira.tests.core.JiraRepositoryConnectorTest;
import org.eclipse.mylyn.jira.tests.core.JiraStackTraceDuplicateDetectorTest;
import org.eclipse.mylyn.jira.tests.core.JiraTaskAttachmentHandlerTest;
import org.eclipse.mylyn.jira.tests.core.JiraTaskDataHandlerTest;
import org.eclipse.mylyn.jira.tests.core.JiraTaskExternalizationTest;
import org.eclipse.mylyn.jira.tests.ui.JiraConnectorUiTest;

/**
 * @author Wesley Coelho (initial integration patch)
 * @author Steffen Pingel
 */
public class AllJiraTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Tests for org.eclipse.mylyn.jira.tests");
		suite.addTest(AllJiraHeadlessStandaloneTests.suite());
		suite.addTestSuite(JiraCustomQueryTest.class);
		suite.addTestSuite(JiraClientFactoryTest.class);
		suite.addTestSuite(JiraTaskExternalizationTest.class);
		suite.addTestSuite(JiraFilterTest.class);
		suite.addTestSuite(JiraRepositoryConnectorTest.class);
		suite.addTestSuite(JiraTaskAttachmentHandlerTest.class);
		suite.addTestSuite(JiraTaskDataHandlerTest.class);
		suite.addTestSuite(JiraStackTraceDuplicateDetectorTest.class);
		suite.addTestSuite(JiraConnectorUiTest.class);
		suite.addTestSuite(FilterDefinitionConverterTest.class);
		return suite;
	}

}
