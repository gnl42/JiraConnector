package me.glindholm.jira.rest.client.internal.json;

import java.net.URISyntaxException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.IssuelinksType;

public class IssueLinkTypesJsonParser implements JsonObjectParser<Iterable<IssuelinksType>> {
    private final IssuelinksTypeJsonParserV5 issueLinkTypeJsonParser = new IssuelinksTypeJsonParserV5();

    @Override
    public Iterable<IssuelinksType> parse(JSONObject json) throws JSONException, URISyntaxException {
        return JsonParseUtil.parseJsonArray(json.optJSONArray("issueLinkTypes"), issueLinkTypeJsonParser);
    }
}
