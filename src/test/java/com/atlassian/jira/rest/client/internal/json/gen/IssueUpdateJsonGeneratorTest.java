package com.atlassian.jira.rest.client.internal.json.gen;

import com.atlassian.jira.rest.client.JSONObjectMatcher;
import com.atlassian.jira.rest.client.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.jira.rest.client.internal.json.ResourceUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertThat;

/**
 * @since 1.1
 */
public class IssueUpdateJsonGeneratorTest {
    @Test
    public void testGenerate() throws Exception {
        final IssueUpdateJsonGenerator generator = new IssueUpdateJsonGenerator();
        List<FieldInput> fields = ImmutableList.of(
            new FieldInput("string", "String value"),
            new FieldInput("integer", 1),
            new FieldInput("long", 1L),
            new FieldInput("complex", new ComplexIssueInputFieldValue(ImmutableMap.<String, Object>of(
                "string", "string",
                "integer", 1,
                "long", 1L,
                "complex", ComplexIssueInputFieldValue.with("test", "id")
            )))
        );

        final JSONObject expected = ResourceUtil.getJsonObjectFromResource("/json/issueUpdate/valid.json");
        final JSONObject actual = generator.generate(fields);
        assertThat(expected, JSONObjectMatcher.isEqual(actual));
    }

    @Test
    public void testGenerateWithEmptyInput() throws Exception {
        final IssueUpdateJsonGenerator generator = new IssueUpdateJsonGenerator();
        final List<FieldInput> input = Lists.newArrayList();

        final JSONObject expected = ResourceUtil.getJsonObjectFromResource("/json/issueUpdate/empty.json");
        final JSONObject actual = generator.generate(input);
        assertThat(actual, JSONObjectMatcher.isEqual(expected));
    }

    @Test
    public void testGenerateWithNullInput() throws Exception {
        final IssueUpdateJsonGenerator generator = new IssueUpdateJsonGenerator();
        final List<FieldInput> input = null;

        final JSONObject expected = ResourceUtil.getJsonObjectFromResource("/json/issueUpdate/empty.json");
        final JSONObject actual = generator.generate(input);
        assertThat(actual, JSONObjectMatcher.isEqual(expected));
    }
}
