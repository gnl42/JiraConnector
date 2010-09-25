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
import com.atlassian.jira.restjavaclient.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.restjavaclient.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.restjavaclient.jersey.JerseyJiraRestClient;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public abstract class AbstractJerseyRestClientTest extends FuncTestCase {
    protected URI jiraUri;
    protected JerseyJiraRestClient client;
    protected URI jiraRestRootUri;
    protected URI jiraAuthRootUri;
	protected static final String ADMIN_USERNAME = "admin";
	protected static final String ADMIN_PASSWORD = "admin";
	protected final NullProgressMonitor pm = new NullProgressMonitor();
	protected static final String DEFAULT_JIRA_DUMP_FILE = "jira1-export.xml";

	public AbstractJerseyRestClientTest() {
    }

    public void configureJira() {
        administration.restoreData(DEFAULT_JIRA_DUMP_FILE);
    }

    @Override
    protected void setUpTest() {
        try {
            jiraUri = UriBuilder.fromUri(environmentData.getBaseUrl().toURI())/*.path(environmentData.getContext())*/.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        jiraRestRootUri = UriBuilder.fromUri(jiraUri).path("/rest/api/latest/").build();
        jiraAuthRootUri = UriBuilder.fromUri(jiraUri).path("/rest/auth/latest/").build();
        setClient(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

	protected void setClient(String username, String password) {
		client = new JerseyJiraRestClient(jiraUri, new BasicHttpAuthenticationHandler(username, password));
	}

	protected void setAnonymousMode() {
		client = new JerseyJiraRestClient(jiraUri, new AnonymousAuthenticationHandler());
	}
	
}
