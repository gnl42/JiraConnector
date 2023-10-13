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

import com.atlassian.jira.rest.client.IntegrationTestUtil;
import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.Session;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import java.time.OffsetDateTime;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.rest.client.internal.json.TestConstants.ADMIN_PASSWORD;
import static com.atlassian.jira.rest.client.internal.json.TestConstants.ADMIN_USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
public class AsynchronousSessionRestClientTest extends AbstractAsynchronousRestClientTest {

    private final JiraRestClientFactory clientFactory = new AsynchronousJiraRestClientFactory();

    private static boolean alreadyRestored;

    @Before
    public void setup() {
        if (!alreadyRestored) {
            IntegrationTestUtil.restoreAppropriateJiraData(TestConstants.DEFAULT_JIRA_DUMP_FILE, administration);
            alreadyRestored = true;
        }
    }

    @Test
    public void testValidSession() {
        final Session session = client.getSessionClient().getCurrentSession().claim();
        assertEquals(ADMIN_USERNAME, session.getUsername());

    }

    @Test
    public void testInvalidCredentials() {
        client = clientFactory.createWithBasicHttpAuthentication(jiraUri, ADMIN_USERNAME, ADMIN_PASSWORD + "invalid");
        TestUtil.assertErrorCode(401, new Runnable() {
            @Override
            public void run() {
                client.getSessionClient().getCurrentSession().claim();
            }
        });
    }

    @Test
    public void testGetCurrentSession() throws Exception {
        final Session session = client.getSessionClient().getCurrentSession().claim();
        assertEquals(ADMIN_USERNAME, session.getUsername());

        // that is not a mistake - username and the password for this user is the same
        client = clientFactory.createWithBasicHttpAuthentication(jiraUri, TestConstants.USER1.getName(), TestConstants.USER1
                .getName());
        final Session session2 = client.getSessionClient().getCurrentSession().claim();
        assertEquals(TestConstants.USER1.getName(), session2.getUsername());
        final OffsetDateTime lastFailedLoginDate = session2.getLoginInfo().getLastFailedLoginDate();

        final JiraRestClient client2 = clientFactory.createWithBasicHttpAuthentication(jiraUri, TestConstants.USER1
                .getName(), "bad-ppassword");
        final OffsetDateTime now = new OffsetDateTime();
        TestUtil.assertErrorCode(401, new Runnable() {
            @Override
            public void run() {
                client2.getSessionClient().getCurrentSession().claim();
            }
        });
        while (!new OffsetDateTime().isAfter(lastFailedLoginDate)) {
            Thread.sleep(20);
        }

        final Session sessionAfterFailedLogin = client.getSessionClient().getCurrentSession().claim();
        assertTrue(sessionAfterFailedLogin.getLoginInfo().getLastFailedLoginDate().isAfter(lastFailedLoginDate));
        assertTrue(sessionAfterFailedLogin.getLoginInfo().getLastFailedLoginDate().isAfter(now));
    }

}
