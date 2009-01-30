package com.atlassian.connector.eclipse.internal.crucible.tests;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtilTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All of the Crucible tests for the Atlassian Eclipse Connector can be run from this class
 * 
 * @author Shawn Minto
 */
public final class AllCrucibleUiTests {

	// TODO test repositoryConnectorUI getQueryWizard
	// TODO test VersionedCOmmentComparator
	// TODO test annotations model stuff
	// TODO test user sorter & label provider
	// TODO test notification manager

	private AllCrucibleUiTests() {
	}

	public static Test suite() {

		TestSuite suite = new TestSuite("Tests for Crucible");
		// $JUnit-BEGIN$

		suite.addTestSuite(CrucibleUiUtilTest.class);

		// $JUnit-END$
		return suite;
	}
}
