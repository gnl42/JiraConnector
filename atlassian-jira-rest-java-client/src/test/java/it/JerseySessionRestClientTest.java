/*
 * Copyright (C) 2010 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it;

import com.atlassian.jira.restjavaclient.NullProgressMonitor;
import com.atlassian.jira.restjavaclient.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.restjavaclient.domain.Session;
import com.atlassian.jira.restjavaclient.jersey.JerseyJiraRestClient;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class JerseySessionRestClientTest extends AbstractJerseyRestClientTest {
	public void testValidSession() {
		final Session session = client.getSessionClient().getCurrentSession(new NullProgressMonitor());
		assertEquals(ADMIN_USERNAME, session.getUsername());

	}

	public void testInvalidCredentials() {
		client = new JerseyJiraRestClient(jiraUri, new BasicHttpAuthenticationHandler(ADMIN_USERNAME, ADMIN_PASSWORD + "invalid"));
		try {
			client.getSessionClient().getCurrentSession(new NullProgressMonitor());
			fail(UniformInterfaceException.class + " exception expected");
		} catch (UniformInterfaceException e) {
			assertEquals(401, e.getResponse().getStatus());
		}
	}
}
