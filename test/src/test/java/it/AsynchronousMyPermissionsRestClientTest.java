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
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Permissions;
import com.atlassian.jira.rest.client.api.domain.input.MyPermissionsInput;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.base.Optional;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@RestoreOnce(TestConstants.DEFAULT_JIRA_DUMP_FILE)
public class AsynchronousMyPermissionsRestClientTest extends AbstractAsynchronousRestClientTest {

	@Override
	public void beforeMethod() {
		super.beforeMethod();

		setUser1(); // set non-admin user
	}

	@Test
	public void testHavePermission() throws Exception {
		testWorkIssuePermission(MyPermissionsInput.withIssue("TST-1"), true);
		testWorkIssuePermission(MyPermissionsInput.withIssue(10000), true); // TST-1
		testWorkIssuePermission(MyPermissionsInput.withProject("TST"), true);
		testWorkIssuePermission(MyPermissionsInput.withProject(10000), true); // TST
		testWorkIssuePermission(MyPermissionsInput.withIssue("RST-1"), false);
		testWorkIssuePermission(MyPermissionsInput.withIssue(10060), false); // RST-1
		testWorkIssuePermission(MyPermissionsInput.withProject("RST"), false);
		testWorkIssuePermission(MyPermissionsInput.withProject(10010), false); // RST
		testWorkIssuePermission(MyPermissionsInput.withAny(), true); // any = permission from TST
	}

	@Test
	public void testNonExisting() throws Exception {
		testError(MyPermissionsInput.withIssue("NONEXISTING-1"), 404);
		testError(MyPermissionsInput.withProject("NONEXISTINGPROJECT"), 404);
	}

	private void testWorkIssuePermission(MyPermissionsInput permissionsInput, boolean expectedPermission) {
		// when
		final Permissions permissions = client.getMyPermissionsRestClient().getMyPermissions(permissionsInput).claim();

		// then
		assertThat("Context " + permissionsInput, permissions.havePermission(Permissions.WORK_ISSUE), is(expectedPermission));
	}

	private void testError(MyPermissionsInput permissionsInput, int expectedStatus) {
		try {
			client.getMyPermissionsRestClient().getMyPermissions(permissionsInput).claim();
			fail("rest client should fail for input " + permissionsInput);
		} catch (RestClientException e) {
			assertThat("Context " + permissionsInput, e.getStatusCode(), is(Optional.of(expectedStatus)));
		}
	}
}
