package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.Page;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class CreateIssueMetaFieldsParser implements JsonObjectParser<Page<CimFieldInfo>> {

    private final PageJsonParser<CimFieldInfo> pageParser = new PageJsonParser<>(new GenericJsonArrayParser<CimFieldInfo>(new CimFieldsInfoJsonParser()));

    @Override
    public Page<CimFieldInfo> parse(JSONObject json) throws JSONException {
        return pageParser.parse(json);
    }
}
