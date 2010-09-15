package com.atlassian.connector.eclipse.internal.crucible.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All of the Crucible tests for the Atlassian Connector for Eclipse can be run from this class
 * 
 * @author Shawn Minto
 */
public final class AllCrucibleTests {

	private AllCrucibleTests() {
	}

	public static Test suite() {

		TestSuite suite = new TestSuite("Tests for Crucible");
		// $JUnit-BEGIN$

		suite.addTest(AllCrucibleCoreTests.suite());
		suite.addTest(AllCrucibleUiTests.suite());

		// $JUnit-END$
		return suite;
	}
}
