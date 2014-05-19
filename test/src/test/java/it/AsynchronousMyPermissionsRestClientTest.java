/*
 * Copyright (C) 2014 Atlassian
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

import com.atlassian.jira.nimblefunctests.annotation.RestoreOnce;
import com.atlassian.jira.rest.client.api.domain.Permission;
import com.atlassian.jira.rest.client.api.domain.Permissions;
import com.atlassian.jira.rest.client.api.domain.input.MyPermissionsInput;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

@RestoreOnce(TestConstants.DEFAULT_JIRA_DUMP_FILE)
public class AsynchronousMyPermissionsRestClientTest extends AbstractAsynchronousRestClientTest {

	@Test
	public void testGetMyPermissions() throws Exception {
		// when
		final Permissions permissions = client.getMyPermissionsRestClient()
				.getMyPermissions(MyPermissionsInput.withIssueKey("TST-1"))
				.claim();

		// then
		final Permission worklogDeleteOwn = permissions.getPermission("WORKLOG_DELETE_OWN");
		assertThat(worklogDeleteOwn, notNullValue());
		assertThat(worklogDeleteOwn.getId(), is(42));
		assertThat(worklogDeleteOwn.getKey(), is("WORKLOG_DELETE_OWN"));
		assertThat(worklogDeleteOwn.getName(), is("Delete Own Worklogs"));
		assertThat(worklogDeleteOwn.getDescription(), is("Ability to delete own worklogs made on issues."));
		assertThat(worklogDeleteOwn.havePermission(), is(true));
	}
}