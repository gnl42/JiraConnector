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

package com.atlassian.connector.eclipse.internal.crucible.ui;

import com.atlassian.connector.eclipse.ui.commons.DecoratedResource;
import com.spartez.util.junit3.TestUtil;

import org.eclipse.core.resources.IResource;
import org.mockito.Mockito;

import junit.framework.TestCase;

@SuppressWarnings("restriction")
public class DecoratedResourceTest extends TestCase {

	public void testEquals() {
		IResource res1 = Mockito.mock(IResource.class);

		DecoratedResource dRes11 = new DecoratedResource(res1);
		DecoratedResource dRes12 = new DecoratedResource(res1, true, "a", "b");
		DecoratedResource dRes13 = new DecoratedResource(res1, true, "a", "c");
		DecoratedResource dRes14 = new DecoratedResource(res1, false, "a", "b");

		assertEquals(dRes11, dRes11);
		assertEquals(dRes11, dRes12);
		assertEquals(dRes11, dRes13);
		assertEquals(dRes11, dRes14);
		assertEquals(dRes12, dRes13);
		assertEquals(dRes12, dRes14);
		assertEquals(dRes13, dRes14);

		DecoratedResource dRes2 = new DecoratedResource(Mockito.mock(IResource.class));

		TestUtil.assertNotEquals(dRes2, dRes11);
	}
}
