package me.glindholm.connector.commons.jira;

import me.glindholmjira.rest.client.domain.input.FieldInput;

import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * User: kalamon
 * Date: 26.11.12
 * Time: 10:21
 */
public interface FieldValueGenerator {
    FieldInput generateJrJcFieldValue(JIRAIssue issue, JIRAActionField field, JSONObject fieldMetadata) throws JSONException, RemoteApiException;
}
