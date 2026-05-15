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

package me.glindholm.connector.eclipse.jira.tests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import me.glindholm.connector.eclipse.jira.tests.core.JiraClientFactoryServerUnrelatedTest;
import me.glindholm.connector.eclipse.jira.tests.core.JiraClientFactoryTest;
import me.glindholm.connector.eclipse.jira.tests.core.JiraCustomQueryTest;
import me.glindholm.connector.eclipse.jira.tests.core.JiraFilterTest;
import me.glindholm.connector.eclipse.jira.tests.core.JiraRepositoryConnectorTest;
import me.glindholm.connector.eclipse.jira.tests.core.JiraStackTraceDuplicateDetectorTest;
import me.glindholm.connector.eclipse.jira.tests.core.JiraTaskAttachmentHandlerTest;
import me.glindholm.connector.eclipse.jira.tests.core.JiraTaskDataHandlerTest;
import me.glindholm.connector.eclipse.jira.tests.core.JiraTaskExternalizationTest;
import me.glindholm.connector.eclipse.jira.tests.ui.JiraConnectorUiStandaloneTest;
import me.glindholm.connector.eclipse.jira.tests.ui.JiraConnectorUiTest;

/**
 * @author Wesley Coelho (initial integration patch)
 * @author Steffen Pingel
 */
@Suite
@SelectClasses({
	AllJiraHeadlessStandaloneTests.class,
	JiraConnectorUiStandaloneTest.class,
	JiraClientFactoryServerUnrelatedTest.class,
	// Need to run these tests with a fixture, so they are added in the suite() method
	JiraCustomQueryTest.class,
	JiraClientFactoryTest.class,
	JiraTaskExternalizationTest.class,
	JiraRepositoryConnectorTest.class,
	JiraTaskAttachmentHandlerTest.class,
	JiraTaskDataHandlerTest.class,
	JiraStackTraceDuplicateDetectorTest.class,
	JiraConnectorUiTest.class,
	JiraFilterTest.class
})
public class AllJiraTests {

	//    public static Test suite() {
	//        for (JiraFixture fixture : new JiraFixture[] { JiraFixture.DEFAULT }) {
	//         }
	//        return suite;
	//    }
}
