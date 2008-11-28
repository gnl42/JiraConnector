package com.atlassian.connector.eclipse.internal.crucible.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All of the Crucible tests for the Atlassian Eclipse Connector can be run from this class
 * 
 * @author Shawn Minto
 */
public class AllCrucibleTests {

	public static Test suite() {

		TestSuite suite = new TestSuite("Tests for Crucible");
		// $JUnit-BEGIN$

//		suite.addTestSuite(CLass.class);

		// $JUnit-END$
		return suite;
	}
}
