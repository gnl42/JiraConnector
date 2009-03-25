/**
 * 
 */
package com.atlassian.connector.eclipse.internal.fisheye.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All tests of the Fisheye UI plug-in can be run form here
 * 
 * @author Thomas Ehrnhoefer
 * 
 */
public final class AllFisheyeUiTests {

	private AllFisheyeUiTests() {
	}

	public static Test suite() {

		TestSuite suite = new TestSuite("Tests for Fisheye UI");
		// $JUnit-BEGIN$

		// $JUnit-END$
		return suite;
	}
}
