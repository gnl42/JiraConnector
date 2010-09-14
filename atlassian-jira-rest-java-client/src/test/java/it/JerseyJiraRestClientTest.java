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
        final ApacheHttpClient httpClient = ApacheHttpClient.create();
        final WebResource sessionResource = httpClient.resource(IntegrationTestUtil.concat(jiraAuthRootUri, "/session"));
        JSONObject json = new JSONObject();
        json.put("username", "admin");
        json.put("password", "admin2");
        final JSONObject resJs = sessionResource.post(JSONObject.class, json);
        AuthenticationJsonParser parser = new AuthenticationJsonParser();
        final Authentication authentication = parser.parse(resJs);
        System.out.println(authentication);
        sessionResource.cookie(new Cookie(authentication.getSession().getName(), authentication.getSession().getValue()));
        final JSONObject jsonObject = sessionResource.get(JSONObject.class);
        System.out.println(jsonObject);
    }



	@Test
	public void temporaryOnly() throws Exception {
		final Issue issue = client.getIssueClient().getIssue(new IssueArgsBuilder("TST-2").withAttachments(false).withComments(true).build(),
				new NullProgressMonitor());
		System.out.println(issue);
	}

}
