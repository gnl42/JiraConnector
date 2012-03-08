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

import com.atlassian.jira.rest.client.internal.ServerVersionConstants;

/**
 * Base class for tests reloading each time (before each test method) the state of JIRA from an external
 * dump (export) XML file.
 *
 * @since v0.1
 */
public abstract class AbstractRestoringJiraStateJerseyRestClientTest extends AbstractJerseyRestClientTest {
	@Override
	protected void setUpTest() {
		super.setUpTest();
		administration.restoreData(getJiraDumpFile());
	}


	protected String getJiraDumpFile() {
		return DEFAULT_JIRA_DUMP_FILE;
	}

	protected boolean doesJiraSupportRestIssueLinking() {
		return client.getMetadataClient().getServerInfo(pm).getBuildNumber() >= ServerVersionConstants.BN_JIRA_4_3;
	}
}
