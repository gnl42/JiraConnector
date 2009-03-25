/**
 * 
 */
package com.atlassian.connector.eclipse.internal.fisheye.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All Fisheye tests can be run from this class
 * 
 * @author Thomas Ehrnhoefer
 * 
 */
public final class AllFisheyeTests {
	private AllFisheyeTests() {
	}

	public static Test suite() {

		TestSuite suite = new TestSuite("Tests for Fisheye");
		// $JUnit-BEGIN$

		suite.addTest(AllFisheyeCoreTests.suite());
		suite.addTest(AllFisheyeUiTests.suite());

		// $JUnit-END$
		return suite;
	}
}
