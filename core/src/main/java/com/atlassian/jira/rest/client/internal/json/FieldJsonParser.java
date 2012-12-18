package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.FieldSchema;
import com.atlassian.jira.rest.client.api.domain.FieldType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * JSON parser for JIRA fields.
 */
public class FieldJsonParser implements JsonObjectParser<Field> {

	private final FieldSchemaJsonParser schemaJsonParser = new FieldSchemaJsonParser();

	@Override
	public Field parse(final JSONObject jsonObject) throws JSONException {
		final String id = jsonObject.getString("id");
		final String name = jsonObject.getString("name");
		final Boolean orderable = jsonObject.getBoolean("orderable");
		final Boolean navigable = jsonObject.getBoolean("navigable");
		final Boolean searchable = jsonObject.getBoolean("searchable");
		final FieldType custom = jsonObject.getBoolean("custom") ? FieldType.CUSTOM : FieldType.JIRA;
		final FieldSchema schema = jsonObject.has("schema") ? schemaJsonParser.parse(jsonObject.getJSONObject("schema")) : null;
		return new Field(id, name, custom, orderable, navigable, searchable, schema);
	}

	public static GenericJsonArrayParser<Field> createFieldsArrayParser() {
		return GenericJsonArrayParser.create(new FieldJsonParser());
	}
}
