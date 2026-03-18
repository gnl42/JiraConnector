package me.glindholm.jira.rest.client.internal.json;

import java.net.URISyntaxException;

import me.glindholm.jira.rest.client.api.domain.RemotelinkApplication;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONException;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONObject;

public class RemotelinkApplicationJsonParser implements JsonObjectParser<RemotelinkApplication> {

    @Override
    public RemotelinkApplication parse(final JSONObject json) throws JSONException, URISyntaxException {
        final String type = json.optString("type", "");
        final String name = json.optString("name", "");

        return new RemotelinkApplication(type, name);
    }

}
