package com.atlassian.connector.eclipse.internal.crucible.tests;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnectorTest;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtilTest;
import com.atlassian.connector.eclipse.internal.crucible.core.VersionedCommentDateComparatorTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All of the Crucible tests for the Atlassian Eclipse Connector can be run from this class
 * 
 * @author Shawn Minto
 */
public final class AllCrucibleCoreTests {

	// TODO make a test to ensure that changing the creds gives us a new httpclient so the state is changed
	// TODO test the cache - cache a partial review
	// TODO test the crucible task mapper 
	// TODO test CrucibleServerCfg equals and hashcode

	private AllCrucibleCoreTests() {
	}

	public static Test suite() {

		TestSuite suite = new TestSuite("Tests for Crucible");
		// $JUnit-BEGIN$

		suite.addTestSuite(CrucibleRepositoryConnectorTest.class);
		suite.addTestSuite(CrucibleUtilTest.class);
		suite.addTestSuite(VersionedCommentDateComparatorTest.class);

		// $JUnit-END$
		return suite;
	}
}
