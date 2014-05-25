package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.OperationGroup;
import com.atlassian.jira.rest.client.api.domain.OperationHeader;
import com.atlassian.jira.rest.client.api.domain.OperationLink;
import com.atlassian.jira.rest.client.api.domain.Operations;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class OperationsJsonParserTest {
	@Test
	public void testParse() throws Exception {
		OperationsJsonParser parser = new OperationsJsonParser();
		Operations actual = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/operations/valid.json"));
		assertThat(actual, is(new Operations(Collections.singleton(new OperationGroup(
				"opsbar-transitions",
				Collections.singleton(new OperationLink("action_id_4", "issueaction-workflow-transition",
						"Start Progress", "Start work on the issue", "/secure/WorkflowUIDispatcher.jspa?id=93813&action=4&atl_token=",
						10, null)),
				Collections.singleton(new OperationGroup(
						null,
						Collections.<OperationLink>emptyList(),
						Collections.<OperationGroup>emptyList(),
						new OperationHeader("opsbar-transitions_more", "Workflow", null, null),
						null)),
				null,
				20
		)))));
	}
}
