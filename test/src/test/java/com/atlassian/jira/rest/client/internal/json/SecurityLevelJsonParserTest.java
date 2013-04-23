/*
 * Copyright (C) 2013 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.SecurityLevel;
import org.codehaus.jettison.json.JSONException;
import org.junit.Test;

import java.net.URI;

import static com.atlassian.jira.rest.client.internal.json.ResourceUtil.getJsonObjectFromResource;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class SecurityLevelJsonParserTest {

	@Test
	public void testParse() throws JSONException {
		final SecurityLevelJsonParser parser = new SecurityLevelJsonParser();
		final SecurityLevel securityLevel = parser.parse(getJsonObjectFromResource("/json/securitylevel/valid.json"));

		assertThat(securityLevel.getSelf(), equalTo(URI.create("http://localhost:2990/jira/rest/api/2/securitylevel/10000")));
		assertThat(securityLevel.getName(), equalTo("Name of security level"));
		assertThat(securityLevel.getDescription(), equalTo("Description of this security level"));
		assertThat(securityLevel.getId(), equalTo(10000L));
	}
}
