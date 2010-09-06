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

import com.atlassian.jira.restjavaclient.IssueArgsBuilder;
import com.atlassian.jira.restjavaclient.NullProgressMonitor;
import com.atlassian.jira.restjavaclient.domain.Issue;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class JerseyJiraRestClientTest extends AbstractJerseyRestClientTest {

    public JerseyJiraRestClientTest() throws URISyntaxException {
        super();
    }

    @Test
    public void emptyTest() {
        // for the sake of mvn test
    }



	@Test
	public void temporaryOnly() throws Exception {
		final Issue issue = client.getIssueClient().getIssue(new IssueArgsBuilder("TST-2").withAttachments(false).withComments(true).build(),
				new NullProgressMonitor());
		System.out.println(issue);
	}

}
