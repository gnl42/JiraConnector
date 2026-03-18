package me.glindholm.jira.rest.client.internal.json.gen;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.glindholm.jira.rest.client.api.domain.input.FieldInput;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONObject;

public class IssueUpdateJsonGenerator implements JsonGenerator<List<FieldInput>> {
    private final ComplexIssueInputFieldValueJsonGenerator generator = new ComplexIssueInputFieldValueJsonGenerator();

    @Override
    public JSONObject generate(final List<FieldInput> fieldInputs) throws JsonProcessingException {
        final JSONObject fields = new JSONObject();
        if (fieldInputs != null) {
            for (final FieldInput field : fieldInputs) {
                final Object fieldValue = field.getValue() == null ? JSONObject.NULL : generator.generateFieldValueForJson(field.getValue());

                fields.put(field.getId(), fieldValue);
            }
        }
        return fields;
    }
}
