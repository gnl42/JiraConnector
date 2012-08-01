/*
 * Copyright (C) 2012 Atlassian
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
import com.atlassian.jira.nimblefunctests.annotation.RestoreOnce;
import com.atlassian.jira.rest.client.GetCreateIssueMetadataOptionsBuilder;
import com.atlassian.jira.rest.client.IntegrationTestUtil;
import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.domain.BasicComponent;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.BasicPriority;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.domain.CimIssueType;
import com.atlassian.jira.rest.client.domain.CimProject;
import com.atlassian.jira.rest.client.domain.CustomFieldOption;
import com.atlassian.jira.rest.client.domain.EntityHelper;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.IssueFieldId;
import com.atlassian.jira.rest.client.domain.TimeTracking;
import com.atlassian.jira.rest.client.domain.input.CannotTransformValueException;
import com.atlassian.jira.rest.client.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.jira.rest.client.domain.input.IssueInput;
import com.atlassian.jira.rest.client.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.rest.client.internal.ServerVersionConstants.BN_JIRA_5;
import static com.google.common.collect.Iterables.toArray;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.*;

// Ignore "May produce NPE" warnings, as we know what we are doing in tests
@SuppressWarnings("ConstantConditions")
// Restore data only once as we just creates issues here - tests doesn't change any settings and doesn't rely on other issues
@RestoreOnce("jira3-export-for-creating-issue-tests.xml")
public class JerseyIssueRestClientCreateIssueTest extends AbstractJerseyRestClientTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testCreateIssue() {
		// collect CreateIssueMetadata for project with key TST
		final IssueRestClient issueClient = client.getIssueClient();
		final Iterable<CimProject> metadataProjects = issueClient.getCreateIssueMetadata(
				new GetCreateIssueMetadataOptionsBuilder().withProjectKeys("TST").withExpandedIssueTypesFields().build(), pm);

		// select project and issue
		assertEquals(1, Iterables.size(metadataProjects));
		final CimProject project = metadataProjects.iterator().next();
		final CimIssueType issueType = EntityHelper.findEntityByName(project.getIssueTypes(), "Bug");

		// grab the first component
		final Iterable<Object> allowedValuesForComponents = issueType.getField(IssueFieldId.COMPONENTS_FIELD).getAllowedValues();
		assertNotNull(allowedValuesForComponents);
		assertTrue(allowedValuesForComponents.iterator().hasNext());
		final BasicComponent component = (BasicComponent) allowedValuesForComponents.iterator().next();

		// grab the first priority
		final Iterable<Object> allowedValuesForPriority = issueType.getField(IssueFieldId.PRIORITY_FIELD).getAllowedValues();
		assertNotNull(allowedValuesForPriority);
		assertTrue(allowedValuesForPriority.iterator().hasNext());
		final BasicPriority priority = (BasicPriority) allowedValuesForPriority.iterator().next();

		// build issue input
		final String summary = "My new issue!";
		final String description = "Some description";
		final BasicUser assignee = IntegrationTestUtil.USER1;
		final List<String> affectedVersionsNames = Collections.emptyList();
		final DateTime dueDate = new DateTime(new Date().getTime());
		final ArrayList<String> fixVersionsNames = Lists.newArrayList("1.1");

		// prepare IssueInput
		final IssueInputBuilder issueInputBuilder = new IssueInputBuilder(project, issueType, summary)
				.setDescription(description)
				.setAssignee(assignee)
				.setAffectedVersionsNames(affectedVersionsNames)
				.setFixVersionsNames(fixVersionsNames)
				.setComponents(component)
				.setDueDate(dueDate)
				.setPriority(priority);

		// create
		final BasicIssue basicCreatedIssue = issueClient.createIssue(issueInputBuilder.build(), pm);
		assertNotNull(basicCreatedIssue.getKey());

		// get issue and check if everything was set as we expected
		final Issue createdIssue = issueClient.getIssue(basicCreatedIssue.getKey(), pm);
		assertNotNull(createdIssue);

		assertEquals(basicCreatedIssue.getKey(), createdIssue.getKey());
		assertEquals(project.getKey(), createdIssue.getProject().getKey());
		assertEquals(issueType.getId(), createdIssue.getIssueType().getId());
		assertEquals(summary, createdIssue.getSummary());
		assertEquals(description, createdIssue.getDescription());

		final BasicUser actualAssignee = createdIssue.getAssignee();
		assertNotNull(actualAssignee);
		assertEquals(assignee.getSelf(), actualAssignee.getSelf());

		final Iterable<String> actualAffectedVersionsNames = EntityHelper.toNamesList(createdIssue.getAffectedVersions());
		assertThat(affectedVersionsNames, containsInAnyOrder(toArray(actualAffectedVersionsNames, String.class)));

		final Iterable<String> actualFixVersionsNames = EntityHelper.toNamesList(createdIssue.getFixVersions());
		assertThat(fixVersionsNames, containsInAnyOrder(toArray(actualFixVersionsNames, String.class)));

		assertTrue(createdIssue.getComponents().iterator().hasNext());
		assertEquals(component.getId(), createdIssue.getComponents().iterator().next().getId());

		// strip time from dueDate
		final DateTime expectedDueDate = JsonParseUtil.parseDate(JsonParseUtil.formatDate(dueDate));
		assertEquals(expectedDueDate, createdIssue.getDueDate());

		final BasicPriority actualPriority = createdIssue.getPriority();
		assertNotNull(actualPriority);
		assertEquals(priority.getId(), actualPriority.getId());
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testCreateIssueWithOnlyRequiredFields() {
		// collect CreateIssueMetadata for project with key TST
		final IssueRestClient issueClient = client.getIssueClient();
		final Iterable<CimProject> metadataProjects = issueClient.getCreateIssueMetadata(
				new GetCreateIssueMetadataOptionsBuilder().withProjectKeys("TST").withExpandedIssueTypesFields().build(),
				pm
		);

		// select project and issue
		assertEquals(1, Iterables.size(metadataProjects));
		final CimProject project = metadataProjects.iterator().next();
		final CimIssueType issueType = EntityHelper.findEntityByName(project.getIssueTypes(), "Bug");

		// build issue input
		final String summary = "My new issue!";

		// create
		final IssueInput issueInput = new IssueInputBuilder(project, issueType, summary).build();
		final BasicIssue basicCreatedIssue = issueClient.createIssue(issueInput, pm);
		assertNotNull(basicCreatedIssue.getKey());

		// get issue and check if everything was set as we expected
		final Issue createdIssue = issueClient.getIssue(basicCreatedIssue.getKey(), pm);
		assertNotNull(createdIssue);

		assertEquals(basicCreatedIssue.getKey(), createdIssue.getKey());
		assertEquals(project.getKey(), createdIssue.getProject().getKey());
		assertEquals(issueType.getId(), createdIssue.getIssueType().getId());
		assertEquals(summary, createdIssue.getSummary());
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testCreateIssueWithoutSummary() {
		final IssueRestClient issueClient = client.getIssueClient();

		thrown.expect(RestClientException.class);
		thrown.expectMessage("You must specify a summary of the issue.");

		final IssueInput issueInput = new IssueInputBuilder("TST", 1L).build();
		issueClient.createIssue(issueInput, pm);
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testCreateIssueWithNotExistentProject() {
		final IssueRestClient issueClient = client.getIssueClient();

		thrown.expect(RestClientException.class);
		thrown.expectMessage("project is required");

		final IssueInput issueInput = new IssueInputBuilder("BAD", 1L, "Should fail").build();
		issueClient.createIssue(issueInput, pm);
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testCreateIssueWithNotExistentIssueType() {
		final IssueRestClient issueClient = client.getIssueClient();

		thrown.expect(RestClientException.class);
		thrown.expectMessage("valid issue type is required");

		final IssueInput issueInput = new IssueInputBuilder("TST", 666L, "Should fail").build();
		issueClient.createIssue(issueInput, pm);
	}


	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testCreateIssueWithoutProject() {
		final IssueRestClient issueClient = client.getIssueClient();

		thrown.expect(RestClientException.class);
		thrown.expectMessage("project is required");

		final IssueInput issueInput = new IssueInput(ImmutableMap.of(
				"summary", new FieldInput("summary", "Summary"),
				"issuetype", new FieldInput("issuetype", ComplexIssueInputFieldValue.with("id", "1"))
		));
		issueClient.createIssue(issueInput, pm);
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testCreateIssueWithInvalidAdditionalField() {
		final IssueRestClient issueClient = client.getIssueClient();
		final String fieldId = "invalidField";

		thrown.expect(RestClientException.class);
		thrown.expectMessage(String
				.format("Field '%s' cannot be set. It is not on the appropriate screen, or unknown.", fieldId));

		final IssueInput issueInput = new IssueInputBuilder("TST", 1L, "Should fail")
				.setFieldValue(fieldId, "test")
				.build();
		issueClient.createIssue(issueInput, pm);
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testCreateIssueWithFieldValueThatIsNotAllowed() {
		final IssueRestClient issueClient = client.getIssueClient();
		final BasicPriority invalidPriority = new BasicPriority(null, 666L, "Invalid Priority");

		thrown.expect(RestClientException.class);
		thrown.expectMessage(String
				.format("Invalid value '%s' passed for customfield 'My Radio buttons'. Allowed values are: 10000[abc], 10001[Another], 10002[The last option], -1", invalidPriority
						.getId()));

		final IssueInput issueInput = new IssueInputBuilder("TST", 1L, "Should fail")
				.setFieldValue("customfield_10001", invalidPriority)
				.build();
		issueClient.createIssue(issueInput, pm);
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testCreateIssueAsAnonymous() {
		setAnonymousMode();

		final IssueRestClient issueClient = client.getIssueClient();

		final IssueInput issueInput = new IssueInputBuilder("ANONEDIT", 1L, "Anonymously created issue").build();
		final BasicIssue createdIssue = issueClient.createIssue(issueInput, pm);

		assertNotNull(createdIssue);
		assertNotNull(createdIssue.getKey());
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testCreateIssueAsAnonymousWhenNotAllowed() {
		setAnonymousMode();
		final IssueRestClient issueClient = client.getIssueClient();

		thrown.expect(RestClientException.class);
		thrown.expectMessage("Anonymous users do not have permission to create issues in this project. Please try logging in first.");

		// TODO: add summary when JIRA bug is fixed (JRADEV-13412)
		final IssueInput issueInput = new IssueInputBuilder("TST", 1L/*, "Issue created by testCreateIssueAsAnonymousWhenNotAllowed"*/)
				.build();
		issueClient.createIssue(issueInput, pm);
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testJiradev13412BugNotFixedIfThisFailsThenCorrectAffectedTests() {
		// This test checks if JRADEV-13412 is fixed.
		// TODO: When fixed please correct testCreateIssueAsAnonymousWhenNotAllowed, testCreateIssueWithoutCreateIssuePermission, testCreateIssueWithoutBrowseProjectPermission and remove this test.
		//
		// We should get something like that when this is fixed:
		//    Anonymous users do not have permission to create issues in this project. Please try logging in first.
		// instead of:
		//    Field 'summary' cannot be set. It is not on the appropriate screen, or unknown.
		setAnonymousMode();
		final IssueRestClient issueClient = client.getIssueClient();

		thrown.expect(RestClientException.class);
		thrown.expectMessage("Field 'summary' cannot be set. It is not on the appropriate screen, or unknown.");

		final IssueInput issueInput = new IssueInputBuilder("TST", 1L, "Sample summary").build();
		issueClient.createIssue(issueInput, pm);
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testCreateIssueWithAssigneeWhenNotAllowedToAssignIssue() {
		setUser2();
		final IssueRestClient issueClient = client.getIssueClient();

		thrown.expect(RestClientException.class);
		thrown.expectMessage("Field 'assignee' cannot be set. It is not on the appropriate screen, or unknown.");

		final IssueInput issueInput = new IssueInputBuilder("TST", 1L, "Issue created by testCreateIssueWithAssigneeWhenNotAllowedToAssignIssue")
				.setAssignee(IntegrationTestUtil.USER_ADMIN)
				.build();
		issueClient.createIssue(issueInput, pm);
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testCreateIssueWithoutCreateIssuePermission() {
		setUser1();
		final IssueRestClient issueClient = client.getIssueClient();

		thrown.expect(RestClientException.class);
		thrown.expectMessage("You do not have permission to create issues in this project.");

		// TODO: add summary when JIRA bug is fixed (JRADEV-13412)
		final IssueInput issueInput = new IssueInputBuilder("NCIFU", 1L/*, "Issue created by testCreateIssueWithoutCreateIssuePermission"*/)
				.build();
		issueClient.createIssue(issueInput, pm);
	}


	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void testCreateIssueWithoutBrowseProjectPermission() {
		setUser1();
		final IssueRestClient issueClient = client.getIssueClient();

		thrown.expect(RestClientException.class);
		thrown.expectMessage("You do not have permission to create issues in this project.");

		// TODO: add summary when JIRA bug is fixed (JRADEV-13412)
		final IssueInput issueInput = new IssueInputBuilder("RST", 1L/*, "Issue created by testCreateIssueWithoutBrowseProjectPermission"*/)
				.build();
		issueClient.createIssue(issueInput, pm);
	}

	@JiraBuildNumberDependent(BN_JIRA_5)
	@Test
	public void interactiveUseCase() throws CannotTransformValueException {
		final IssueRestClient issueClient = client.getIssueClient();

		// get project list with fields expanded
		final Iterable<CimProject> metadataProjects = issueClient.getCreateIssueMetadata(
				new GetCreateIssueMetadataOptionsBuilder().withExpandedIssueTypesFields().build(),
				pm
		);
		log.log("Available projects: ");
		for (CimProject p : metadataProjects) {
			log.log(MessageFormat.format("\t* [{0}] {1}", p.getKey(), p.getName()));
		}
		log.log("");
		assertTrue("There is no project to select!", metadataProjects.iterator().hasNext());

		// select project
		final CimProject project = metadataProjects.iterator().next();
		log.log(MessageFormat.format("Selected project: [{0}] {1}\n", project.getKey(), project.getName()));

		// select issue type
		log.log("Available issue types for selected project:");
		for (CimIssueType t : project.getIssueTypes()) {
			log.log(MessageFormat.format("\t* [{0}] {1}", t.getId(), t.getName()));
		}
		log.log("");

		final CimIssueType issueType = project.getIssueTypes().iterator().next();
		log.log(MessageFormat.format("Selected issue type: [{0}] {1}\n", issueType.getId(), issueType.getName()));

		final IssueInputBuilder builder = new IssueInputBuilder(project.getKey(), issueType.getId());

		// fill fields
		log.log("Filling fields:");
		for (Map.Entry<String, CimFieldInfo> entry : issueType.getFields().entrySet()) {
			final CimFieldInfo fieldInfo = entry.getValue();
			final String fieldCustomType = fieldInfo.getSchema().getCustom();
			final String fieldType = fieldInfo.getSchema().getType();
			final String fieldId = fieldInfo.getId();

			if ("project".equals(fieldId) || "issuetype".equals(fieldId)) {
				// this field was already set by IssueInputBuilder constructor - skip it
				continue;
			}

			log.log(MessageFormat.format("\t* [{0}] {1}\n\t\t| schema: {2}\n\t\t| required: {3}", fieldId, fieldInfo
					.getName(), fieldInfo.getSchema(), fieldInfo.isRequired()));

			// choose value for this field
			Object value = null;
			final Iterable<Object> allowedValues = fieldInfo.getAllowedValues();
			if (allowedValues != null) {
				log.log("\t\t| field only accepts those values:");
				for (Object val : allowedValues) {
					log.log("\t\t\t* " + val);
				}
				if (allowedValues.iterator().hasNext()) {
					final boolean expectedArray = "array".equals(fieldType);
					Object singleValue = allowedValues.iterator().next();

					if ("com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect".equals(fieldCustomType)) {
						// select option with children - if any
						final Iterable<Object> optionsWithChildren = Iterables.filter(allowedValues, new Predicate<Object>() {
							@Override
							public boolean apply(Object input) {
								return ((CustomFieldOption) input).getChildren().iterator().hasNext();
							}
						});

						if (optionsWithChildren.iterator().hasNext()) {
							// there is option with children - set it
							final CustomFieldOption option = (CustomFieldOption) optionsWithChildren.iterator().next();
							value = new CustomFieldOption(option.getId(), option.getSelf(), option.getValue(),
								Collections.<CustomFieldOption>emptyList(), option.getChildren().iterator().next());
						}
						else {
							// no sub-values available, set only top level value
							value = allowedValues.iterator().next();
						}
					}
					else {
						value = expectedArray ? Collections.singletonList(singleValue) : singleValue;	
					}
					log.log("\t\t| selecting value: " + value);
				} else {
					log.log("\t\t| there is no allowed value - leaving field blank");
				}
			} else {
				if ("com.atlassian.jirafisheyeplugin:jobcheckbox".equals(fieldCustomType)) {
					value = "false";
				}
				else if ("com.atlassian.jira.plugin.system.customfieldtypes:url".equals(fieldCustomType)) {
					value = "http://www.atlassian.com/";
				}
				else if ("string".equals(fieldType)) {
					value = "This is simple string value for field " + fieldId + " named " + fieldInfo.getName() + ".";
				} else if ("number".equals(fieldType)) {
					value = 124;
				} else if ("user".equals(fieldType)) {
					value = IntegrationTestUtil.USER_ADMIN;
				} else if ("array".equals(fieldType) && "user".equals(fieldInfo.getSchema().getItems())) {
					value = ImmutableList.of(IntegrationTestUtil.USER_ADMIN);
				} else if ("group".equals(fieldType)) {
					// TODO change to group object when implemented
					value = ComplexIssueInputFieldValue.with("name", IntegrationTestUtil.GROUP_JIRA_ADMINISTRATORS);
				} else if ("array".equals(fieldType) && "group".equals(fieldInfo.getSchema().getItems())) {
					// TODO change to group object when implemented
					value = ImmutableList.of(ComplexIssueInputFieldValue.with("name", IntegrationTestUtil.GROUP_JIRA_ADMINISTRATORS));
				} else if ("date".equals(fieldType)) {
					value = JsonParseUtil.formatDate(new DateTime());
				} else if ("datetime".equals(fieldType)) {
					value = JsonParseUtil.formatDateTime(new DateTime());
				} else if ("array".equals(fieldType) && "string".equals(fieldInfo.getSchema().getItems())) {
					value = ImmutableList.of("one", "two", "three");
				} else if ("timetracking".equals(fieldType)) {
					value = new TimeTracking(60, 40, null); // time spent is not allowed
				} else {
					if (fieldInfo.isRequired()) {
						fail("I don't know how to fill that required field, sorry.");
					} else {
						log.log("\t\t| field value is not required, leaving blank");
					}
				}
			}
			if (value == null) {
				log.log("\t\t| value is null, skipping filed");
			} else {
				log.log(MessageFormat.format("\t\t| setting value => {0}", value));
				builder.setFieldValue(fieldId, value);
			}
		}
		log.log("");

		// all required data is provided, let's create issue
		final IssueInput issueInput = builder.build();

		final BasicIssue basicCreatedIssue = issueClient.createIssue(issueInput, pm);
		assertNotNull(basicCreatedIssue);

		final Issue createdIssue = issueClient.getIssue(basicCreatedIssue.getKey(), pm);
		assertNotNull(createdIssue);

		log.log("Created new issue successfully, key: " + basicCreatedIssue.getKey());

		// assert few fields
		IssueInputBuilder actualBuilder = new IssueInputBuilder(createdIssue.getProject(), createdIssue
				.getIssueType(), createdIssue.getSummary())
				.setPriority(createdIssue.getPriority())
				.setReporter(createdIssue.getReporter())
				.setAssignee(createdIssue.getAssignee())
				.setDescription(createdIssue.getDescription());

		final Collection<FieldInput> actualValues = actualBuilder.build().getFields().values();
		final Collection<FieldInput> expectedValues = issueInput.getFields().values();

		assertThat(expectedValues, hasItems(toArray(actualValues, FieldInput.class)));
	}
}