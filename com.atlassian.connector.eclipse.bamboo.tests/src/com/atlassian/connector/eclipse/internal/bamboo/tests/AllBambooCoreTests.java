/**
 * 
 */
package com.atlassian.connector.eclipse.internal.bamboo.tests;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooClientManagerTest;
import com.atlassian.connector.eclipse.internal.bamboo.core.BambooRepositoryConnectorTest;
import com.atlassian.connector.eclipse.internal.bamboo.core.BuildPlanManagerTest;
import com.atlassian.connector.eclipse.internal.bamboo.core.DicrectClickThroughTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All of the Bamboo tests for the Atlassian Connector for Eclipse can be run from this class
 * 
 * @author Thomas Ehrnhoefer
 */
public class AllBambooCoreTests {
	private AllBambooCoreTests() {
	}

	public static Test suite() {

		TestSuite suite = new TestSuite("Tests for Bamboo");
		// $JUnit-BEGIN$
		suite.addTestSuite(BambooClientManagerTest.class);
		suite.addTestSuite(BambooRepositoryConnectorTest.class);
		suite.addTestSuite(BuildPlanManagerTest.class);
		suite.addTestSuite(DicrectClickThroughTest.class);
		// $JUnit-END$
		return suite;
	}
}
