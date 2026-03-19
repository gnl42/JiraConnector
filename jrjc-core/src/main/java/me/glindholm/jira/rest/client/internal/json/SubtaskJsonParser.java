package me.glindholm.jira.rest.client.internal.json;

import java.net.URI;

import me.glindholm.jira.rest.client.api.domain.IssueType;
import me.glindholm.jira.rest.client.api.domain.Status;
import me.glindholm.jira.rest.client.api.domain.Subtask;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONException;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONObject;

public class SubtaskJsonParser implements JsonObjectParser<Subtask> {
    private final IssueTypeJsonParser issueTypeJsonParser = new IssueTypeJsonParser();
    private final StatusJsonParser statusJsonParser = new StatusJsonParser();

    @Override
    public Subtask parse(final JSONObject json) throws JSONException {
        final URI issueUri = JsonParseUtil.parseURI(json.getString("self"));
        final String issueKey = json.getString("key");
        final JSONObject fields = json.getJSONObject("fields");
        final String summary = fields.getString("summary");
        final Status status = statusJsonParser.parse(fields.getJSONObject("status"));
        final IssueType issueType = issueTypeJsonParser.parse(fields.getJSONObject("issuetype"));
        return new Subtask(issueKey, issueUri, summary, issueType, status);
    }
}
