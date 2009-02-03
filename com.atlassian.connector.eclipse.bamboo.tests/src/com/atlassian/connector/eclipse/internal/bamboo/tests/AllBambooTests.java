/**
 * 
 */
package com.atlassian.connector.eclipse.internal.bamboo.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All of the Bamboo tests for the Atlassian Eclipse Connector can be run from this class
 *
 * @author Thomas Ehrnhoefer
 */
public class AllBambooTests {

	private AllBambooTests() {
	}

	public static Test suite() {

		TestSuite suite = new TestSuite("Tests for Bamboo");
		// $JUnit-BEGIN$

		suite.addTest(AllBambooCoreTests.suite());
		suite.addTest(AllBambooUiTests.suite());

		// $JUnit-END$
		return suite;
	}
	
}
