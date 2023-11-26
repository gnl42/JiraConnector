package me.glindholm.jira.rest.client.internal.json;

import java.net.URISyntaxException;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import me.glindholm.jira.rest.client.api.domain.Remotelink;

public class RemotelinksJsonParser implements JsonArrayParser<List<Remotelink>> {

    @Override
    public List<Remotelink> parse(final JSONArray json) throws JSONException, URISyntaxException {
        return JsonParseUtil.parseJsonArray(json, new RemotelinkJsonParser());
    }

}
