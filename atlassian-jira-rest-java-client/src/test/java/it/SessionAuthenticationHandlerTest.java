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

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.restjavaclient.NullProgressMonitor;
import com.atlassian.jira.restjavaclient.auth.SessionAuthenticationHandler;
import com.atlassian.jira.restjavaclient.domain.Session;
import com.atlassian.jira.restjavaclient.jersey.JerseyJiraRestClient;
import static org.junit.Assert.*;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class SessionAuthenticationHandlerTest extends AbstractJerseyRestClientTest {
	@Override
	protected void setUpTest() {
		super.setUpTest();
		// @todo fix this test as cookie based authentication does not work yet
//        client = new JerseyJiraRestClient(jiraUri, new SessionAuthenticationHandler("admin", "admin"));
	}

	public void testGetCurrentSession() {
		final Session session = client.getSessionClient().getCurrentSession(new NullProgressMonitor());
		assertEquals(ADMIN_USERNAME, session.getUsername());
	}
}
