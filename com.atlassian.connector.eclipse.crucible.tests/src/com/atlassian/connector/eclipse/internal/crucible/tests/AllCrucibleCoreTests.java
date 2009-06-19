package com.atlassian.connector.eclipse.internal.crucible.tests;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleClientManagerTest;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnectorTest;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleTaskMapperTest;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtilTest;
import com.atlassian.connector.eclipse.internal.crucible.core.VersionedCommentDateComparatorTest;
import com.atlassian.connector.eclipse.internal.crucible.core.configuration.EclipseCrucibleServerCfgTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All of the Crucible tests for the Atlassian Connector for Eclipse can be run from this class
 * 
 * @author Shawn Minto
 */
public final class AllCrucibleCoreTests {

	// TODO test the cache - cache a partial review

	private AllCrucibleCoreTests() {
	}

	public static Test suite() {

		TestSuite suite = new TestSuite("Tests for Crucible");
		// $JUnit-BEGIN$

		suite.addTestSuite(CrucibleRepositoryConnectorTest.class);
		suite.addTestSuite(CrucibleUtilTest.class);
		suite.addTestSuite(VersionedCommentDateComparatorTest.class);
		suite.addTestSuite(CrucibleClientManagerTest.class);
		suite.addTestSuite(CrucibleTaskMapperTest.class);
		suite.addTestSuite(EclipseCrucibleServerCfgTest.class);

		// $JUnit-END$
		return suite;
	}
}
