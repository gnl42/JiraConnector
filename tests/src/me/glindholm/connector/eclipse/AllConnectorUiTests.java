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

package me.glindholm.connector.eclipse;

import me.glindholm.connector.eclipse.internal.bamboo.tests.AllBambooUiTests;

import junit.framework.Test;
import junit.framework.TestSuite;

public final class AllConnectorUiTests {

	private AllConnectorUiTests() {
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Ui Test for me.glindholm.connector.eclipse.tests");

		return suite;
	}

}
