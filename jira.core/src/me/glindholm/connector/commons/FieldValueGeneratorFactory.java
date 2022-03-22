package me.glindholm.connector.commons;

import org.codehaus.jettison.json.JSONObject;

import me.glindholm.connector.commons.jira.FieldValueGenerator;
import me.glindholm.connector.commons.jira.JIRAActionField;

/**
 * User: kalamon
 * Date: 26.11.12
 * Time: 10:30
 */
public interface FieldValueGeneratorFactory {
    FieldValueGenerator get(JIRAActionField field, JSONObject fieldDef);
}
