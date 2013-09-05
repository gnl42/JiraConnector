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

package com.atlassian.connector.eclipse.jira.tests.core;

import junit.framework.TestCase;

import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.mylyn.tests.util.TestFixture;

import com.atlassian.connector.eclipse.internal.jira.core.JiraClientFactory;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraServiceUnavailableException;
import com.atlassian.connector.eclipse.internal.jira.core.service.rest.JiraRestClientAdapter;

public class JiraClientFactoryServerUnrelatedTest extends TestCase {

	private JiraClientFactory clientFactory;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		clientFactory = JiraClientFactory.getDefault();
		TestFixture.resetTaskListAndRepositories();
	}

	@Override
	protected void tearDown() throws Exception {
		clientFactory.logOutFromAll();
	}

	public void testValidate() throws Exception {
		// invalid URL		
		try {
			clientFactory.validateConnection(new WebLocation("http://non.existant/repository", "user", "password"),
					null);
			fail("Expected exception");
		} catch (JiraServiceUnavailableException e) {
		}

		// not found		
		try {
			clientFactory.validateConnection(
					new WebLocation("https://www.atlassian.com/not-found", "user", "password"), null);
			fail("Expected exception");
		} catch (JiraServiceUnavailableException e) {
			assertTrue(e.getMessage().contains(JiraRestClientAdapter.HTTP_404));
		}

		// RPC not enabled
		// test url does not work
//		try {
//			clientFactory.validateConnection(new WebLocation("http://mylyn.eclipse.org/jira-invalid", "user",
//					"password"), null);
//			fail("Expected exception");
//		} catch (JiraServiceUnavailableException e) {
//			assertEquals("JIRA RPC services are not enabled. Please contact your JIRA administrator.", e.getMessage());
//		}

		// HTTP error
		// test url does not work
//		try {
//			clientFactory.validateConnection(new WebLocation("http://mylyn.eclipse.org/jira-proxy-error", "user",
//					"password"), null);
//			fail("Expected exception");
//		} catch (JiraServiceUnavailableException e) {
//			assertEquals("JIRA RPC services are not enabled. Please contact your JIRA administrator.", e.getMessage());
//		}
	}
}
