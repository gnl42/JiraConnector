package me.glindholm.jira.rest.client.internal.json;

import java.net.URISyntaxException;

import me.glindholm.jira.rest.client.api.domain.IssueType;
import me.glindholm.jira.rest.client.api.domain.Page;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONException;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONObject;

public class CreateIssueMetaProjectIssueTypesParser implements JsonObjectParser<Page<IssueType>> {

    private final GenericJsonArrayParser<IssueType> issueTypeParser = new GenericJsonArrayParser<>(new IssueTypeJsonParser());
    private final PageJsonParser<IssueType> pageParser = new PageJsonParser<>(issueTypeParser);

    @Override
    public Page<IssueType> parse(final JSONObject json) throws JSONException, URISyntaxException {
        return pageParser.parse(json);
    }
}
