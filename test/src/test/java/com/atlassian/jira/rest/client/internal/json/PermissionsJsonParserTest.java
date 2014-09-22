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
package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.Permission;
import com.atlassian.jira.rest.client.api.domain.Permissions;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class PermissionsJsonParserTest {

	@Test
	public void testParse() throws Exception {
		final PermissionsJsonParser parser = new PermissionsJsonParser();
		final Permissions permissions = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/mypermission/valid.json"));

		assertThat(permissions.havePermission("WORKLOG_EDIT_OWN"), is(true));
		assertThat(permissions.havePermission("WORKLOG_DELETE_OWN"), is(false));
		Permission worklogDeleteOwn = permissions.getPermission("WORKLOG_DELETE_OWN");
		assertThat(worklogDeleteOwn, notNullValue());
		assertThat(worklogDeleteOwn.getId(), is(42));
		assertThat(worklogDeleteOwn.getKey(), is("WORKLOG_DELETE_OWN"));
		assertThat(worklogDeleteOwn.getName(), is("Delete Own Worklogs"));
		assertThat(worklogDeleteOwn.getDescription(), is("Ability to delete own worklogs made on issues."));
		assertThat(worklogDeleteOwn.havePermission(), is(false));
	}
}