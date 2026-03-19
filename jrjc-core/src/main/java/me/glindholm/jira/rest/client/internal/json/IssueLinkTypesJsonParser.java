package me.glindholm.jira.rest.client.internal.json;

import java.net.URISyntaxException;
import java.util.List;

import me.glindholm.jira.rest.client.api.domain.IssuelinksType;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONException;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONObject;

public class IssueLinkTypesJsonParser implements JsonObjectParser<List<IssuelinksType>> {
    private final IssuelinksTypeJsonParserV5 issueLinkTypeJsonParser = new IssuelinksTypeJsonParserV5();

    @Override
    public List<IssuelinksType> parse(final JSONObject json) throws JSONException, URISyntaxException {
        return JsonParseUtil.parseJsonArray(json.optJSONArray("issueLinkTypes"), issueLinkTypeJsonParser);
    }
}
