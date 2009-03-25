/**
 * 
 */
package com.atlassian.connector.eclipse.internal.fisheye.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All Tests of the Fisheye Core plug-in can be run from this class
 * 
 * @author Thomas Ehrnhoefer
 * 
 */
public final class AllFisheyeCoreTests {

	private AllFisheyeCoreTests() {
	}

	public static Test suite() {

		TestSuite suite = new TestSuite("Tests for Fisheye Core");
		// $JUnit-BEGIN$

		// $JUnit-END$
		return suite;
	}
}
