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

import com.atlassian.jira.restjavaclient.IntegrationTestUtil;
import com.atlassian.jira.restjavaclient.IssueArgsBuilder;
import com.atlassian.jira.restjavaclient.NullProgressMonitor;
import com.atlassian.jira.restjavaclient.TestUtil;
import com.atlassian.jira.restjavaclient.domain.Authentication;
import com.atlassian.jira.restjavaclient.domain.Issue;
import com.atlassian.jira.restjavaclient.json.AuthenticationJsonParser;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import javax.ws.rs.core.Cookie;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class JerseyJiraRestClientTest extends AbstractJerseyRestClientTest {

    @Test
    public void testEmpty() throws JSONException {
        // for the sake of mvn integration-test
    }



	@Test
	public void temporaryOnly() throws Exception {
		final Issue issue = client.getIssueClient().getIssue("TST-2", new NullProgressMonitor());
		System.out.println(issue);
	}

}
