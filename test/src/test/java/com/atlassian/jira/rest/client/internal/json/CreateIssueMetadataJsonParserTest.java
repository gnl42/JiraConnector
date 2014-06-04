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

package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.codehaus.jettison.json.JSONException;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

import java.util.Collections;
import java.util.Map;

import static com.atlassian.jira.rest.client.TestUtil.toUri;
import static org.junit.Assert.assertEquals;

/**
 * @since v1.0
 */
public class CreateIssueMetadataJsonParserTest {

	@Test
	public void testParse() throws JSONException {
		final CreateIssueMetadataJsonParser parser = new CreateIssueMetadataJsonParser();
		final Iterable<CimProject> createMetaProjects = parser.parse(
				ResourceUtil.getJsonObjectFromResource("/json/createmeta/valid.json")
		);

		Assert.assertEquals(4, Iterables.size(createMetaProjects));

		// test first project
		final CimProject project = createMetaProjects.iterator().next();
		assertEquals("http://localhost:2990/jira/rest/api/2/project/ANONEDIT", project.getSelf().toString());
		assertEquals("ANONEDIT", project.getKey());
		assertEquals("Anonymous Editable Project", project.getName());
		Assert.assertEquals(ImmutableMap.of(
				"16x16", toUri("http://localhost:2990/jira/secure/projectavatar?size=small&pid=10030&avatarId=10011"),
				"48x48", toUri("http://localhost:2990/jira/secure/projectavatar?pid=10030&avatarId=10011")
		), project.getAvatarUris());


		// check some issue types
		Assert.assertThat(project.getIssueTypes(), IsIterableContainingInAnyOrder.containsInAnyOrder(
				new CimIssueType(toUri("http://localhost:2990/jira/rest/api/latest/issuetype/1"), 1L, "Bug", false,
						"A problem which impairs or prevents the functions of the product.", toUri("http://localhost:2990/jira/images/icons/bug.gif"),
						Collections.<String, CimFieldInfo>emptyMap()),
				new CimIssueType(toUri("http://localhost:2990/jira/rest/api/latest/issuetype/2"), 2L, "New Feature", false,
						"A new feature of the product, which has yet to be developed.", toUri("http://localhost:2990/jira/images/icons/newfeature.gif"),
						Collections.<String, CimFieldInfo>emptyMap()),
				new CimIssueType(toUri("http://localhost:2990/jira/rest/api/latest/issuetype/3"), 3L, "Task", false,
						"A task that needs to be done.", toUri("http://localhost:2990/jira/images/icons/task.gif"),
						Collections.<String, CimFieldInfo>emptyMap()),
				new CimIssueType(toUri("http://localhost:2990/jira/rest/api/latest/issuetype/4"), 4L, "Improvement", false,
						"An improvement or enhancement to an existing feature or task.", toUri("http://localhost:2990/jira/images/icons/improvement.gif"),
						Collections.<String, CimFieldInfo>emptyMap()),
				new CimIssueType(toUri("http://localhost:2990/jira/rest/api/latest/issuetype/5"), 5L, "Sub-task", true,
						"The sub-task of the issue", toUri("http://localhost:2990/jira/images/icons/issue_subtask.gif"),
						Collections.<String, CimFieldInfo>emptyMap())
		));
	}

	@Test
	public void testParseWithFieldsExpanded() throws JSONException {
		final CreateIssueMetadataJsonParser parser = new CreateIssueMetadataJsonParser();
		final Iterable<CimProject> createMetaProjects = parser.parse(
				ResourceUtil.getJsonObjectFromResource("/json/createmeta/valid-with-fields-expanded.json")
		);

		Assert.assertEquals(4, Iterables.size(createMetaProjects));

		// get project with issue types expanded
		final CimProject project = EntityHelper.findEntityByName(
				createMetaProjects, "Anonymous Editable Project"
		);
		Assert.assertNotNull(project);
		Assert.assertEquals(5, Iterables.size(project.getIssueTypes()));

		// get issue type and check if fields was parsed successfully
		final CimIssueType issueType = EntityHelper.findEntityByName(project.getIssueTypes(), "Bug");
		final Map<String, CimFieldInfo> issueTypeFields = issueType.getFields();
		Assert.assertEquals(19, issueTypeFields.size());

		// test system field "components"
		final CimFieldInfo componentsFieldInfo = issueTypeFields.get("components");
		final CimFieldInfo expectedComponentsFieldInfo = new CimFieldInfo(
				"components", false, "Component/s", new FieldSchema("array", "component", "components", null, null),
				Sets.newHashSet(StandardOperation.ADD, StandardOperation.REMOVE, StandardOperation.SET),
				Collections.emptyList(), null
		);
		Assert.assertEquals(expectedComponentsFieldInfo, componentsFieldInfo);

		// check custom field with allowed values
		final CimFieldInfo cf1001 = issueTypeFields.get("customfield_10001");
		assertEquals(new FieldSchema("string", null, null, "com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons", 10001L), cf1001
				.getSchema());
		Assert.assertEquals(3, Iterables.size(cf1001.getAllowedValues()));
		Assert.assertThat(cf1001.getOperations(), IsIterableContainingInAnyOrder.containsInAnyOrder(StandardOperation.SET));

		// check allowed values types
		assertAllowedValuesOfType(issueTypeFields.get("issuetype").getAllowedValues(), IssueType.class);
		assertAllowedValuesOfType(issueTypeFields.get("priority").getAllowedValues(), BasicPriority.class);
		assertAllowedValuesOfType(issueTypeFields.get("customfield_10001").getAllowedValues(), CustomFieldOption.class);
		assertAllowedValuesOfType(issueTypeFields.get("project").getAllowedValues(), BasicProject.class);
		assertAllowedValuesOfType(issueTypeFields.get("customfield_10010").getAllowedValues(), BasicProject.class);
	}

	private void assertAllowedValuesOfType(final Iterable<Object> allowedValues, Class type) {
		Assert.assertThat(allowedValues, JUnitMatchers.everyItem(Matchers.instanceOf(type)));
	}
}
