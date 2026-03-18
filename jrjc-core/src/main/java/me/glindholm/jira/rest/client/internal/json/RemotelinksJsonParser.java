package me.glindholm.jira.rest.client.internal.json;

import java.net.URISyntaxException;
import java.util.List;

import me.glindholm.jira.rest.client.api.domain.Remotelink;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONArray;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONException;

public class RemotelinksJsonParser implements JsonArrayParser<List<Remotelink>> {

    @Override
    public List<Remotelink> parse(final JSONArray json) throws JSONException, URISyntaxException {
        return JsonParseUtil.parseJsonArray(json, new RemotelinkJsonParser());
    }

}
