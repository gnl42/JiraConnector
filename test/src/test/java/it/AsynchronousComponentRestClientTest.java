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

import com.atlassian.jira.nimblefunctests.annotation.JiraBuildNumberDependent;
import com.atlassian.jira.nimblefunctests.annotation.Restore;
import com.atlassian.jira.rest.client.IntegrationTestUtil;
import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.api.domain.AssigneeType;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.Component;
import com.atlassian.jira.rest.client.api.domain.EntityHelper;
import com.atlassian.jira.rest.client.api.domain.input.ComponentInput;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.collect.Iterables;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.client.api.domain.EntityHelper.findEntityByName;
import static com.atlassian.jira.rest.client.internal.ServerVersionConstants.BN_JIRA_4_4;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Restore(TestConstants.DEFAULT_JIRA_DUMP_FILE)
public class AsynchronousComponentRestClientTest extends AbstractAsynchronousRestClientTest {

	@Test
	public void testGetComponent() throws Exception {
		final BasicComponent basicComponent = findEntityByName(client.getProjectClient().getProject("TST").claim()
				.getComponents(), "Component A");
		final Component component = client.getComponentClient().getComponent(basicComponent.getSelf()).claim();
		assertEquals("Component A", component.getName());
		assertEquals("this is some description of component A", component.getDescription());
		assertEquals(IntegrationTestUtil.USER_ADMIN_60, component.getLead());
	}

	@Test
	@JiraBuildNumberDependent(BN_JIRA_4_4)
	public void testGetComponentOnJira4xOrNewerShouldContainNotNullId() throws Exception {
		final BasicComponent basicComponent = findEntityByName(client.getProjectClient().getProject("TST").claim()
				.getComponents(), "Component A");
		final Component component = client.getComponentClient().getComponent(basicComponent.getSelf()).claim();
		assertEquals("Component A", component.getName());
		assertEquals("this is some description of component A", component.getDescription());
		assertEquals(Long.valueOf(10000), component.getId());
		assertEquals(IntegrationTestUtil.USER_ADMIN_60, component.getLead());
	}

	@Test
	public void testGetInvalidComponent() throws Exception {
		final BasicComponent basicComponent = Iterables.get(client.getProjectClient().getProject("TST").claim()
				.getComponents(), 0);
		final String uriForUnexistingComponent = basicComponent.getSelf().toString() + "1234";
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "The component with id "
				+ TestUtil.getLastPathSegment(basicComponent.getSelf()) + "1234 does not exist.", new Runnable() {
			@Override
			public void run() {
				client.getComponentClient().getComponent(TestUtil.toUri(uriForUnexistingComponent)).claim();
			}
		});
	}

	@Test
	public void testGetComponentFromRestrictedProject() throws Exception {
		final BasicComponent basicComponent = Iterables.getOnlyElement(client.getProjectClient().getProject("RST").claim()
				.getComponents());
		assertEquals("One Great Component", client.getComponentClient().getComponent(basicComponent.getSelf()).claim().getName());

		// now as unauthorized user
		setClient(TestConstants.USER2_USERNAME, TestConstants.USER2_PASSWORD);
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER ?
				"The component with id 10010 does not exist."
				: "The user user does not have permission to complete this operation.", new Runnable() {
			@Override
			public void run() {
				client.getComponentClient().getComponent(basicComponent.getSelf()).claim().getName();
			}
		});

		setAnonymousMode();
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER ?
				"The component with id 10010 does not exist."
				: "This user does not have permission to complete this operation.", new Runnable() {
			@Override
			public void run() {
				client.getComponentClient().getComponent(basicComponent.getSelf()).claim().getName();
			}
		});
	}

	@Test
	@JiraBuildNumberDependent(BN_JIRA_4_4)
	public void testCreateAndRemoveComponent() {
		final Iterable<BasicComponent> components = client.getProjectClient().getProject("TST").claim().getComponents();
		assertEquals(2, Iterables.size(components));
		final BasicComponent basicComponent = Iterables.get(components, 0);
		final BasicComponent basicComponent2 = Iterables.get(components, 1);
		final String componentName = "my component";
		final ComponentInput componentInput = new ComponentInput(componentName, "a description", null, null);
		final Component component = client.getComponentClient().createComponent("TST", componentInput).claim();
		assertEquals(componentInput.getName(), component.getName());
		assertEquals(componentInput.getDescription(), component.getDescription());
		assertNull(component.getLead());
		assertProjectHasComponents(basicComponent.getName(), basicComponent2.getName(), componentName);

		client.getComponentClient().removeComponent(basicComponent.getSelf(), null).claim();
		assertProjectHasComponents(basicComponent2.getName(), componentName);
		client.getComponentClient().removeComponent(basicComponent2.getSelf(), null).claim();
		assertProjectHasComponents(componentName);
		client.getComponentClient().removeComponent(component.getSelf(), null).claim();
		assertProjectHasComponents();

	}

	@Test
	@JiraBuildNumberDependent(BN_JIRA_4_4)
	public void testCreateAndRemoveComponentAsUnauthorizedUsers() {
		final Iterable<BasicComponent> components = client.getProjectClient().getProject("TST").claim().getComponents();
		assertEquals(2, Iterables.size(components));
		final BasicComponent basicComponent = Iterables.get(components, 0);

		final ComponentInput componentInput = new ComponentInput("my component", "a description", null, null);
		setUser1();

		final Response.Status expectedForbiddenErrorCode =
				(doesJiraReturnCorrectErrorCodeForForbiddenOperation()) ? Response.Status.FORBIDDEN
						: Response.Status.UNAUTHORIZED;
		TestUtil.assertErrorCode(expectedForbiddenErrorCode, "The user wseliga does not have permission to complete this operation.", new Runnable() {
			@Override
			public void run() {
				client.getComponentClient().removeComponent(basicComponent.getSelf(), null).claim();
			}
		});
		TestUtil.assertErrorCode(expectedForbiddenErrorCode, "You cannot edit the configuration of this project.", new Runnable() {
			@Override
			public void run() {
				client.getComponentClient().createComponent("TST", componentInput).claim();
			}
		});

		setAnonymousMode();
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER ?
				"The component with id 10000 does not exist."
				: "This user does not have permission to complete this operation.", new Runnable() {
			@Override
			public void run() {
				client.getComponentClient().removeComponent(basicComponent.getSelf(), null).claim();
			}
		});

		if (IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER) {
			TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "No project could be found with key 'TST'.", new Runnable() {
				@Override
				public void run() {
					client.getComponentClient().createComponent("TST", componentInput).claim();
				}
			});
		} else {
			// IMO for anonymous access still Response.Status.UNAUTHORIZED should be returned - JRADEV-7671
			TestUtil.assertErrorCode(expectedForbiddenErrorCode, "You cannot edit the configuration of this project.", new Runnable() {
				@Override
				public void run() {
					client.getComponentClient().createComponent("TST", componentInput).claim();
				}
			});
		}


		setAdmin();
		// now let's try to add a component with colliding name
		final ComponentInput dupeComponentInput = new ComponentInput(basicComponent.getName(), "a description", null, null);
		TestUtil.assertErrorCode(Response.Status.BAD_REQUEST, "A component with the name Component A already exists in this project.", new Runnable() {
			@Override
			public void run() {
				client.getComponentClient().createComponent("TST", dupeComponentInput).claim();
			}
		});

		// now let's try to add a component for a non existing project
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, "No project could be found with key 'FAKE'.", new Runnable() {
			@Override
			public void run() {
				client.getComponentClient().createComponent("FAKE", componentInput).claim();
			}
		});

	}


	@SuppressWarnings({"ConstantConditions"})
	@Test
	@JiraBuildNumberDependent(BN_JIRA_4_4)
	public void testCreateComponentWithLead() {
		final ComponentInput componentInput = new ComponentInput("my component name", "a description", "admin", AssigneeType.COMPONENT_LEAD);
		final Component component = client.getComponentClient().createComponent("TST", componentInput).claim();
		assertNotNull(component.getAssigneeInfo());
		assertEquals(IntegrationTestUtil.USER_ADMIN_60, component.getAssigneeInfo().getAssignee());
		assertEquals(AssigneeType.COMPONENT_LEAD, component.getAssigneeInfo().getAssigneeType());
		assertTrue(component.getAssigneeInfo().isAssigneeTypeValid());
		assertEquals(IntegrationTestUtil.USER_ADMIN_60, component.getAssigneeInfo().getRealAssignee());
		assertEquals(AssigneeType.COMPONENT_LEAD, component.getAssigneeInfo().getRealAssigneeType());

		final ComponentInput componentInput2 = new ComponentInput("my component name2", "a description", IntegrationTestUtil.USER1
				.getName(), AssigneeType.UNASSIGNED);
		final Component component2 = client.getComponentClient().createComponent("TST", componentInput2).claim();
		assertNotNull(component2.getAssigneeInfo());
		assertNull(component2.getAssigneeInfo().getAssignee());
		assertEquals(AssigneeType.UNASSIGNED, component2.getAssigneeInfo().getAssigneeType());
		assertFalse(component2.getAssigneeInfo().isAssigneeTypeValid());
		assertEquals(IntegrationTestUtil.USER_ADMIN_60, component2.getAssigneeInfo().getRealAssignee());
		assertEquals(AssigneeType.PROJECT_DEFAULT, component2.getAssigneeInfo().getRealAssigneeType());
	}


	@Test
	@JiraBuildNumberDependent(BN_JIRA_4_4)
	public void testUpdateComponent() {
		final BasicComponent basicComponent = Iterables.get(client.getProjectClient().getProject("TST").claim()
				.getComponents(), 0);
		final Component component = client.getComponentClient().getComponent(basicComponent.getSelf()).claim();
		final String newName = basicComponent.getName() + "updated";
		Component adjustedComponent = new Component(component.getSelf(), component.getId(), newName, component
				.getDescription(), component.getLead(), component.getAssigneeInfo());

		Component updatedComponent = client.getComponentClient().updateComponent(basicComponent
				.getSelf(), new ComponentInput(newName, null, null, null)).claim();
		assertEquals(adjustedComponent, updatedComponent);
		assertEquals(adjustedComponent, client.getComponentClient().getComponent(basicComponent.getSelf()).claim());

		final String newDescription = "updated description";
		adjustedComponent = new Component(component.getSelf(), component
				.getId(), newName, newDescription, IntegrationTestUtil.USER1_60, component.getAssigneeInfo());
		updatedComponent = client.getComponentClient().updateComponent(basicComponent
				.getSelf(), new ComponentInput(null, newDescription, IntegrationTestUtil.USER1_60.getName(), null)).claim();
		assertEquals(adjustedComponent, updatedComponent);

		adjustedComponent = new Component(component.getSelf(), component
				.getId(), newName, newDescription, IntegrationTestUtil.USER1_60,
				new Component.AssigneeInfo(IntegrationTestUtil.USER1_60, AssigneeType.COMPONENT_LEAD, IntegrationTestUtil.USER1_60, AssigneeType.COMPONENT_LEAD, true));

		updatedComponent = client.getComponentClient().updateComponent(basicComponent
				.getSelf(), new ComponentInput(null, newDescription, IntegrationTestUtil.USER1
				.getName(), AssigneeType.COMPONENT_LEAD)).claim();
		assertEquals(adjustedComponent, updatedComponent);


		// now with non-assignable assignee (thus we are inheriting assignee from project settings and component-level settings are ignored)
		adjustedComponent = new Component(component.getSelf(), component
				.getId(), newName, newDescription, IntegrationTestUtil.USER2_60,
				new Component.AssigneeInfo(IntegrationTestUtil.USER2_60, AssigneeType.COMPONENT_LEAD, IntegrationTestUtil.USER_ADMIN_60, AssigneeType.PROJECT_DEFAULT, false));

		updatedComponent = client.getComponentClient().updateComponent(basicComponent
				.getSelf(), new ComponentInput(null, newDescription, IntegrationTestUtil.USER2
				.getName(), AssigneeType.COMPONENT_LEAD)).claim();
		assertEquals(adjustedComponent, updatedComponent);

	}


	@Test
	@JiraBuildNumberDependent(BN_JIRA_4_4)
	public void testGetComponentRelatedIssuesCount() {
		final BasicComponent bc = findEntityByName(client.getProjectClient().getProject("TST").claim()
				.getComponents(), "Component A");
		assertEquals(1, client.getComponentClient().getComponentRelatedIssuesCount(bc.getSelf()).claim().intValue());
		final ComponentInput componentInput = new ComponentInput("my component name", "a description", "admin", AssigneeType.COMPONENT_LEAD);
		final Component component = client.getComponentClient().createComponent("TST", componentInput).claim();
		assertEquals(0, client.getComponentClient().getComponentRelatedIssuesCount(component.getSelf()).claim().intValue());

		client.getComponentClient().removeComponent(bc.getSelf(), component.getSelf()).claim();
		assertEquals(1, client.getComponentClient().getComponentRelatedIssuesCount(component.getSelf()).claim().intValue());

		// smelly error code/message returned here - JRA-25062
		setAnonymousMode();
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER ?
				"The component with id 10000 does not exist."
				: "This user does not have permission to complete this operation.", new Runnable() {
			@Override
			public void run() {
				client.getComponentClient().getComponentRelatedIssuesCount(component.getSelf()).claim();
			}
		});

		setAdmin();
		final BasicComponent restrictedComponent = Iterables.getOnlyElement(client.getProjectClient().getProject("RST").claim()
				.getComponents());
		setUser1();
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND, IntegrationTestUtil.TESTING_JIRA_5_OR_NEWER ?
				"The component with id 10010 does not exist."
				: "The user wseliga does not have permission to complete this operation.", new Runnable() {
			@Override
			public void run() {
				client.getComponentClient().getComponentRelatedIssuesCount(restrictedComponent.getSelf()).claim();
			}
		});

		setAdmin();
		TestUtil.assertErrorCode(Response.Status.NOT_FOUND,
				"The component with id " + TestUtil.getLastPathSegment(restrictedComponent.getSelf())
						+ "999 does not exist.", new Runnable() {
			@Override
			public void run() {
				client.getComponentClient().getComponentRelatedIssuesCount(TestUtil.toUri(restrictedComponent.getSelf() + "999"))
						.claim();
			}
		});

	}


	private boolean doesJiraReturnCorrectErrorCodeForForbiddenOperation() {
		return client.getMetadataClient().getServerInfo().claim().getBuildNumber() >= ServerVersionConstants.BN_JIRA_5;
	}


	private void assertProjectHasComponents(String... names) {
		assertThat(Iterables.transform(client.getProjectClient().getProject("TST").claim().getComponents(),
				EntityHelper.GET_ENTITY_NAME_FUNCTION), containsInAnyOrder(names));
	}

}
