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

package me.glindholm.connector.eclipse.jira.tests.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.Test;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraComponent;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ComponentFilter;

public class ComponentFilterTest  {

	@Test
	public void testCopy() {
		final JiraComponent[] components = { new JiraComponent("1") };
		final var filter1 = new ComponentFilter(components, true);

		final var filter2 = filter1.copy();

		assertEquals(filter1.hasNoComponent(), filter2.hasNoComponent());
		assertEquals(filter1.getComponents().length, filter2.getComponents().length);
		assertEquals(filter1.getComponents()[0], filter2.getComponents()[0]);

		components[0] = new JiraComponent("2");
		assertEquals(components[0], filter1.getComponents()[0]);

		assertNotSame(filter1.getComponents()[0], filter2.getComponents()[0]);
		assertFalse(filter2.getComponents()[0].equals(filter1.getComponents()[0]));
	}
}
