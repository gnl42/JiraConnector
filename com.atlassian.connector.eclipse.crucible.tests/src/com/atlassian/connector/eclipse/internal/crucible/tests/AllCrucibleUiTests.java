package com.atlassian.connector.eclipse.internal.crucible.tests;

import com.atlassian.connector.eclipse.internal.crucible.ui.ActiveReviewManagerTest;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleClientManagerUiTest;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtilTest;
import com.atlassian.connector.eclipse.internal.crucible.ui.RepositoryConnectorUiTest;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.UserLabelProviderTest;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.UserSorterTest;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.CrucibleRepositorySettingsPageTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All of the Crucible tests for the Atlassian Connector for Eclipse can be run from this class
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
		suite.addTestSuite(UserSorterTest.class);
		suite.addTestSuite(ActiveReviewManagerTest.class);
		suite.addTestSuite(CrucibleClientManagerUiTest.class);
		suite.addTestSuite(CrucibleRepositorySettingsPageTest.class);

		// $JUnit-END$
		return suite;
	}
}
