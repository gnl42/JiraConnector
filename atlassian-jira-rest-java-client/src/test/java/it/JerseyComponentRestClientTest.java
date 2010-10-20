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
import com.atlassian.jira.rest.client.domain.BasicComponent;
import com.atlassian.jira.rest.client.domain.Component;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.collect.Iterables;

import javax.ws.rs.core.Response;

public class JerseyComponentRestClientTest extends AbstractRestoringJiraStateJerseyRestClientTest {
	public void testGetComponent() throws Exception {
		final BasicComponent basicComponent = Iterables.get(client.getProjectClient().getProject("TST", pm).getComponents(), 0);
		final Component component = client.getComponentClient().getComponent(basicComponent.getSelf(), pm);
		assertEquals("Component A", component.getName());
		assertEquals("this is some description of component A", component.getDescription());
		assertEquals(IntegrationTestUtil.USER_ADMIN, component.getLead());
	}

	public void testGetInvalidComponent() throws Exception {
		final BasicComponent basicComponent = Iterables.get(client.getProjectClient().getProject("TST", pm).getComponents(), 0);
		final String uriForUnexistingComponent = basicComponent.getSelf().toString() + "1234";
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND,  "The component with id "
				+ TestUtil.getLastPathSegment(basicComponent.getSelf()) + "1234 does not exist.", new Runnable() {
			@Override
			public void run() {
				client.getComponentClient().getComponent(TestUtil.toUri(uriForUnexistingComponent), pm);
			}
		});
	}

	public void testGetComponentFromRestrictedProject() throws Exception {
		final BasicComponent basicComponent = Iterables.get(client.getProjectClient().getProject("RST", pm).getComponents(), 0);
		assertEquals("One Great Component", client.getComponentClient().getComponent(basicComponent.getSelf(), pm).getName());

		// now as unauthorized user
		setClient(TestConstants.USER2_USERNAME, TestConstants.USER2_PASSWORD);
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "The user user does not have permission to complete this operation.", new Runnable() {
			@Override
			public void run() {
				client.getComponentClient().getComponent(basicComponent.getSelf(), pm).getName();
			}
		});

		setAnonymousMode();
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "This user does not have permission to complete this operation.", new Runnable() {
			@Override
			public void run() {
				client.getComponentClient().getComponent(basicComponent.getSelf(), pm).getName();
			}
		});
	}
}
