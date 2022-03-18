package com.atlassian.connector.commons;

import com.atlassian.connector.commons.jira.FieldValueGenerator;
import com.atlassian.connector.commons.jira.JIRAActionField;
import org.codehaus.jettison.json.JSONObject;

/**
 * User: kalamon
 * Date: 26.11.12
 * Time: 10:30
 */
public interface FieldValueGeneratorFactory {
    FieldValueGenerator get(JIRAActionField field, JSONObject fieldDef);
}
