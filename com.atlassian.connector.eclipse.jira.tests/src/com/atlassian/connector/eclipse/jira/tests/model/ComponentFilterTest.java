/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.jira.tests.model;

import junit.framework.TestCase;

import com.atlassian.connector.eclipse.internal.jira.core.model.Component;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ComponentFilter;

@SuppressWarnings("restriction")
public class ComponentFilterTest extends TestCase {

	public void testCopy() {
		Component[] components = new Component[] { new Component("1") };
		ComponentFilter filter1 = new ComponentFilter(components, true);

		ComponentFilter filter2 = filter1.copy();

		assertEquals(filter1.hasNoComponent(), filter2.hasNoComponent());
		assertEquals(filter1.getComponents().length, filter2.getComponents().length);
		assertEquals(filter1.getComponents()[0], filter2.getComponents()[0]);

		components[0] = new Component("2");
		assertEquals(components[0], filter1.getComponents()[0]);

		assertNotSame(filter1.getComponents()[0], filter2.getComponents()[0]);
		assertFalse(filter2.getComponents()[0].equals(filter1.getComponents()[0]));
	}
}
