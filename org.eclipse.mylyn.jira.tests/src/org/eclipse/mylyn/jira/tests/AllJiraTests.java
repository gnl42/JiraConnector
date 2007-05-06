/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.jira.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Wesley Coelho (initial integration patch)
 * @author Steffen Pingel
 */
public class AllJiraTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for mylar.jira.tests");

		// $JUnit-BEGIN$
		suite.addTestSuite(JiraCustomQueryTest.class);
		suite.addTestSuite(JiraTaskTest.class);
		suite.addTestSuite(JiraServerFacadeTest.class);
		suite.addTestSuite(JiraTaskExternalizationTest.class);
		suite.addTestSuite(JiraFilterTest.class);
		suite.addTestSuite(JiraTaskArchiveTest.class);
		suite.addTestSuite(JiraRepositoryConnectorTest.class);
		suite.addTestSuite(JiraRpcServerTest.class);
		suite.addTestSuite(JiraAttachmentHandlerTest.class);
		// $JUnit-END$
		return suite;
	}

}
