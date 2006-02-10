/*******************************************************************************
 * Copyright (c) 2006 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.jira.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Wesley Coelho (initial integration patch)
 */
public class AllJiraTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for mylar.jira.tests");

		// $JUnit-BEGIN$
		suite.addTestSuite(JiraServerFacadeTest.class);
		suite.addTestSuite(JiraTaskExternalizationTest.class);
		suite.addTestSuite(JiraFilterTest.class);
		suite.addTestSuite(JiraTaskArchiveTest.class);
		// $JUnit-END$
		return suite;
	}
}
