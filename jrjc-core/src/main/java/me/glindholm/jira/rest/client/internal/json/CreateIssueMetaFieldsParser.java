package me.glindholm.jira.rest.client.internal.json;

import java.net.URISyntaxException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.CimFieldInfo;
import me.glindholm.jira.rest.client.api.domain.Page;

public class CreateIssueMetaFieldsParser implements JsonObjectParser<Page<CimFieldInfo>> {

    private final PageJsonParser<CimFieldInfo> pageParser = new PageJsonParser<>(new GenericJsonArrayParser<>(new CimFieldsInfoJsonParser()));

    @Override
    public Page<CimFieldInfo> parse(final JSONObject json) throws JSONException, URISyntaxException {
        return pageParser.parse(json);
    }
}
