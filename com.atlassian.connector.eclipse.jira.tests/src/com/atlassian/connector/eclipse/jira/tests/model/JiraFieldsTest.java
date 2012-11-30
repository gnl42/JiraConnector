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

import com.atlassian.connector.eclipse.internal.jira.core.model.filter.JiraFields;

public class JiraFieldsTest extends TestCase {
	/**
	 * Check few values against {@link JiraFields#getClassic()}, {@link JiraFields#getJql()},
	 * {@link JiraFields#getJql()}
	 */
	public void testClassicVsJqlVsToString() {
		// all values are the same
		assertEquals(JiraFields.ASSIGNEE.toString(), JiraFields.ASSIGNEE.getClassic());
		assertEquals(JiraFields.ASSIGNEE.getJql(), JiraFields.ASSIGNEE.getClassic());

		// different, toString returns classic version
		assertEquals(JiraFields.FIX_VERSION.toString(), JiraFields.FIX_VERSION.getClassic());
		assertFalse(JiraFields.FIX_VERSION.getJql().equals(JiraFields.FIX_VERSION.getClassic()));
	}
}
