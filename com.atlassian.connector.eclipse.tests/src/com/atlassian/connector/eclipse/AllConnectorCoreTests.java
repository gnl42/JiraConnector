/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse;

import com.atlassian.connector.eclipse.internal.bamboo.tests.AllBambooCoreTests;
import com.atlassian.connector.eclipse.internal.fisheye.tests.AllFisheyeCoreTests;

import junit.framework.Test;
import junit.framework.TestSuite;

public final class AllConnectorCoreTests {

	private AllConnectorCoreTests() {
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Core Test for com.atlassian.connector.eclipse.tests");

		suite.addTest(AllBambooCoreTests.suite());
		suite.addTest(AllFisheyeCoreTests.suite());

		return suite;
	}

}
