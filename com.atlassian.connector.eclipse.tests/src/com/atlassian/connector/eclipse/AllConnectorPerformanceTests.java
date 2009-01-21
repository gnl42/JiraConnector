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

import junit.framework.Test;
import junit.framework.TestSuite;

public final class AllConnectorPerformanceTests {

	private AllConnectorPerformanceTests() {
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Performance tests for com.atlassian.connector.eclipse.tests");
		return suite;
	}

}
