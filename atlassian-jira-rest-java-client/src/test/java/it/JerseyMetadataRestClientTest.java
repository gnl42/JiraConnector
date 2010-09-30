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

import com.atlassian.jira.restjavaclient.domain.ServerInfo;
import junit.framework.TestCase;
import org.joda.time.DateTime;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class JerseyMetadataRestClientTest extends AbstractRestoringJiraStateJerseyRestClientTest {
	public void testGetServerInfo() throws Exception {
		final ServerInfo serverInfo = client.getMetadataClient().getServerInfo(pm);
		assertEquals("Your Company JIRA", serverInfo.getServerTitle());
		assertTrue(serverInfo.getBuildDate().isBeforeNow());
		assertTrue(serverInfo.getServerTime().isAfter(new DateTime().minusMinutes(5)));
		assertTrue(serverInfo.getServerTime().isBefore(new DateTime().plusMinutes(5)));
	}
}
