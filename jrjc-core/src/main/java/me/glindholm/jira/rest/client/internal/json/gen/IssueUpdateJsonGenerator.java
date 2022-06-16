package me.glindholm.jira.rest.client.internal.json.gen;

import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.input.FieldInput;

public class IssueUpdateJsonGenerator implements JsonGenerator<List<FieldInput>> {
    private final ComplexIssueInputFieldValueJsonGenerator generator = new ComplexIssueInputFieldValueJsonGenerator();

    @Override
    public JSONObject generate(List<FieldInput> fieldInputs) throws JSONException {
        final JSONObject fields = new JSONObject();
        if (fieldInputs != null) {
            for (final FieldInput field : fieldInputs) {
                final Object fieldValue = field.getValue() == null ? JSONObject.NULL
                        : generator.generateFieldValueForJson(field.getValue());

                fields.put(field.getId(), fieldValue);
            }
        }
        return fields;
    }
}
