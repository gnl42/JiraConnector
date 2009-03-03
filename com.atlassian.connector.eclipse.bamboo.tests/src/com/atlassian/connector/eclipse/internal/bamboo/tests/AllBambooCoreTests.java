/**
 * 
 */
package com.atlassian.connector.eclipse.internal.bamboo.tests;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooClientManagerTest;
import com.atlassian.connector.eclipse.internal.bamboo.core.BambooRepositoryConnectorTest;
import com.atlassian.connector.eclipse.internal.bamboo.core.BuildPlanManagerTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All of the Bamboo tests for the Atlassian Eclipse Connector can be run from this class
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
		// $JUnit-END$
		return suite;
	}
}
