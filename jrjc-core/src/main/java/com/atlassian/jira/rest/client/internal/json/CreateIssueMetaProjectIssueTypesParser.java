package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Page;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class CreateIssueMetaProjectIssueTypesParser implements JsonObjectParser<Page<IssueType>> {

    private final GenericJsonArrayParser<IssueType> issueTypeParser = new GenericJsonArrayParser<IssueType>(new IssueTypeJsonParser());
    private final PageJsonParser<IssueType> pageParser = new PageJsonParser<>(issueTypeParser);

    @Override
    public Page<IssueType> parse(JSONObject json) throws JSONException {
        return pageParser.parse(json);
    }
}
