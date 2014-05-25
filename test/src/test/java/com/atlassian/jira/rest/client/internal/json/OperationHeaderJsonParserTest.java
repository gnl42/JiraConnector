package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.OperationHeader;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class OperationHeaderJsonParserTest {

	@Test
	public void testParseIdLabel() throws Exception {
		OperationHeaderJsonParser parser = new OperationHeaderJsonParser();
		OperationHeader actual = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/operationHeader/valid-id-label.json"));
		assertThat(actual, is(new OperationHeader("opsbar-transitions_more", "Workflow", null, null)));
	}

	@Test
	public void testParseLabelTitleIconClass() throws Exception {
		OperationHeaderJsonParser parser = new OperationHeaderJsonParser();
		OperationHeader actual = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/operationHeader/valid-label-title-iconClass.json"));
		assertThat(actual, is(new OperationHeader(null, "Views", "View this issue in another format", "icon-view")));
	}
}
