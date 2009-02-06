package com.atlassian.connector.eclipse.internal.crucible.tests;

import com.atlassian.connector.eclipse.internal.crucible.ui.ActiveReviewManagerTest;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleClientManagerUiTest;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtilTest;
import com.atlassian.connector.eclipse.internal.crucible.ui.RepositoryConnectorUiTest;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.UserContentProviderTest;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.UserLabelProviderTest;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.UserSorterTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All of the Crucible tests for the Atlassian Eclipse Connector can be run from this class
 * 
 * @author Shawn Minto
 */
public final class AllCrucibleUiTests {

	// TODO test notification manager

	// TODO test annotations model stuff

	private AllCrucibleUiTests() {
	}

	public static Test suite() {

		TestSuite suite = new TestSuite("Tests for Crucible");
		// $JUnit-BEGIN$

		suite.addTestSuite(CrucibleUiUtilTest.class);
		suite.addTestSuite(RepositoryConnectorUiTest.class);
		suite.addTestSuite(UserLabelProviderTest.class);
		suite.addTestSuite(UserContentProviderTest.class);
		suite.addTestSuite(UserSorterTest.class);
		suite.addTestSuite(ActiveReviewManagerTest.class);
		suite.addTestSuite(CrucibleClientManagerUiTest.class);

		// $JUnit-END$
		return suite;
	}
}
