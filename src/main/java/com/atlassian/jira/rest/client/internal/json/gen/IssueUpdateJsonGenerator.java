package com.atlassian.jira.rest.client.internal.json.gen;

import com.atlassian.jira.rest.client.domain.input.FieldInput;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class IssueUpdateJsonGenerator implements JsonGenerator<Iterable<FieldInput>> {
    private final ComplexIssueInputFieldValueJsonGenerator generator = new ComplexIssueInputFieldValueJsonGenerator();

    @Override
    public JSONObject generate(Iterable<FieldInput> fieldInputs) throws JSONException {
        final JSONObject fields = new JSONObject();
        if (fieldInputs != null) {
            for (final FieldInput field : fieldInputs) {
                if (field.getValue() != null) {
                    fields.put(field.getId(), generator.generateFieldValueForJson(field.getValue()));
                }
            }
        }
        return fields;
    }
}
