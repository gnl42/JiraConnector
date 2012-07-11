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

import com.atlassian.jira.rest.client.IterableMatcher;
import com.atlassian.jira.rest.client.domain.BasicIssueType;
import com.atlassian.jira.rest.client.domain.BasicPriority;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.CreateIssueFieldInfo;
import com.atlassian.jira.rest.client.domain.CreateIssueIssueType;
import com.atlassian.jira.rest.client.domain.CreateIssueMetadata;
import com.atlassian.jira.rest.client.domain.CreateIssueMetadataProject;
import com.atlassian.jira.rest.client.domain.CustomFieldOption;
import com.atlassian.jira.rest.client.domain.EntityHelper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.codehaus.jettison.json.JSONException;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @since v1.0
 */
public class CreateIssueMetadataJsonParserTest {

	@Test
	public void testParse() throws JSONException, URISyntaxException {
		final CreateIssueMetadataJsonParser parser = new CreateIssueMetadataJsonParser();
		final CreateIssueMetadata createIssueMetadata = parser.parse(
				ResourceUtil.getJsonObjectFromResource("/json/createmeta/valid.json")
		);

		assertEquals(4, Iterables.size(createIssueMetadata.getProjects()));

		// test first project
		CreateIssueMetadataProject project = createIssueMetadata.getProjects().iterator().next();
		assertEquals("http://localhost:2990/jira/rest/api/2/project/ANONEDIT", project.getSelf().toString());
		assertEquals("ANONEDIT", project.getKey());
		assertEquals("Anonymous Editable Project", project.getName());
		assertEquals(ImmutableMap.of(
				"16x16", new URI("http://localhost:2990/jira/secure/projectavatar?size=small&pid=10030&avatarId=10011"),
				"48x48", new URI("http://localhost:2990/jira/secure/projectavatar?pid=10030&avatarId=10011")
		), project.getAvatarUris());


		// check some issue types
		assertThat(project.getIssueTypes(), IterableMatcher.hasOnlyElements(
			new CreateIssueIssueType(new URI("http://localhost:2990/jira/rest/api/latest/issuetype/1"), 1L, "Bug", false, "A problem which impairs or prevents the functions of the product.", new URI("http://localhost:2990/jira/images/icons/bug.gif"), Collections.<String, CreateIssueFieldInfo>emptyMap()),
			new CreateIssueIssueType(new URI("http://localhost:2990/jira/rest/api/latest/issuetype/2"), 2L, "New Feature", false, "A new feature of the product, which has yet to be developed.", new URI("http://localhost:2990/jira/images/icons/newfeature.gif"), Collections.<String, CreateIssueFieldInfo>emptyMap()),
			new CreateIssueIssueType(new URI("http://localhost:2990/jira/rest/api/latest/issuetype/3"), 3L, "Task", false, "A task that needs to be done.", new URI("http://localhost:2990/jira/images/icons/task.gif"), Collections.<String, CreateIssueFieldInfo>emptyMap()),
			new CreateIssueIssueType(new URI("http://localhost:2990/jira/rest/api/latest/issuetype/4"), 4L, "Improvement", false, "An improvement or enhancement to an existing feature or task.", new URI("http://localhost:2990/jira/images/icons/improvement.gif"), Collections.<String, CreateIssueFieldInfo>emptyMap()),
			new CreateIssueIssueType(new URI("http://localhost:2990/jira/rest/api/latest/issuetype/5"), 5L, "Sub-task", true, "The sub-task of the issue", new URI("http://localhost:2990/jira/images/icons/issue_subtask.gif"), Collections.<String, CreateIssueFieldInfo>emptyMap())
		));
	}

	@Test
	public void testParseWithFieldsExpanded() throws JSONException {
		final CreateIssueMetadataJsonParser parser = new CreateIssueMetadataJsonParser();
		final CreateIssueMetadata createIssueMetadata = parser.parse(
				ResourceUtil.getJsonObjectFromResource("/json/createmeta/valid-with-fields-expanded.json")
		);

		assertEquals(4, Iterables.size(createIssueMetadata.getProjects()));

		// get project with issue types expanded
		final CreateIssueMetadataProject project = EntityHelper.findEntityByName(
				createIssueMetadata.getProjects(), "Anonymous Editable Project"
		);
		assertNotNull(project);
		assertEquals(5, Iterables.size(project.getIssueTypes()));

		// get issue type and check if fields was parsed successfully
		final CreateIssueIssueType issueType = EntityHelper.findEntityByName(project.getIssueTypes(), "Bug");
		final Map<String,CreateIssueFieldInfo> issueTypeFields = issueType.getFields();
		assertEquals(19, issueTypeFields.size());

		// test system field "components"
		final CreateIssueFieldInfo componentsFieldInfo = issueTypeFields.get("components");
		final CreateIssueFieldInfo expectedComponentsFieldInfo = new CreateIssueFieldInfo(
				"components", false, "Component/s", new FieldSchema("array", "component", "components", null, null),
				Sets.newHashSet(StandardOperation.ADD, StandardOperation.REMOVE, StandardOperation.SET),
				Collections.emptyList(), null
		);
		assertEquals(expectedComponentsFieldInfo, componentsFieldInfo);

		// check custom field with allowed values
		final CreateIssueFieldInfo cf1001 = issueTypeFields.get("customfield_10001");
		assertEquals(new FieldSchema("string", null, null, "com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons", 10001L), cf1001.getSchema());
		assertEquals(3, Iterables.size(cf1001.getAllowedValues()));
		assertThat(cf1001.getOperations(), IterableMatcher.hasOnlyElements(StandardOperation.SET));

		// check allowed values types
		assertAllowedValuesOfType(issueTypeFields.get("issuetype").getAllowedValues(), BasicIssueType.class);
		assertAllowedValuesOfType(issueTypeFields.get("priority").getAllowedValues(), BasicPriority.class);
		assertAllowedValuesOfType(issueTypeFields.get("customfield_10001").getAllowedValues(), CustomFieldOption.class);
		assertAllowedValuesOfType(issueTypeFields.get("project").getAllowedValues(), BasicProject.class);
		assertAllowedValuesOfType(issueTypeFields.get("customfield_10010").getAllowedValues(), BasicProject.class);
	}
	
	private void assertAllowedValuesOfType(final Iterable<Object> allowedValues, Class type) {
		final Iterator<Object> iterator = allowedValues.iterator();
		assertTrue(iterator.hasNext());
		while (iterator.hasNext()) {
			final Object obj = iterator.next();
			assertNotNull(obj);
			assertTrue(type.isAssignableFrom(obj.getClass()));
		}
	}
}
