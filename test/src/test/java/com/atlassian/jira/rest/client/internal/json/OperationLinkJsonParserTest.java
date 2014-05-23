package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.api.domain.OperationLink;
import org.codehaus.jettison.json.JSONException;
import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class OperationLinkJsonParserTest {
	@Test
	public void testParseFull() throws Exception {
		test("/json/operationLink/valid.json", is(new OperationLink("comment-issue",
				"issueaction-comment-issue add-issue-comment", "Comment", "Comment on this issue",
				TestUtil.toUri("/secure/AddComment!default.jspa?id=10100"), 10,
				"aui-icon aui-icon-small aui-iconfont-comment icon-comment")));
	}

	@Test
	public void testParsePartial() throws Exception {
		test("/json/operationLink/partial.json", is(new OperationLink("comment-issue",
				"issueaction-comment-issue add-issue-comment", "Comment", "Comment on this issue",
				TestUtil.toUri("/secure/AddComment!default.jspa?id=10100"), 10,
				null)));
	}

	private void test(String resourcePath, Matcher<OperationLink> expected) throws JSONException {
		OperationLinkJsonParser parser = new OperationLinkJsonParser();
		OperationLink actual = parser.parse(ResourceUtil.getJsonObjectFromResource(resourcePath));
		assertThat(actual, expected);
	}
}