package me.glindholm.jira.rest.client.internal.json;

import java.net.URISyntaxException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.RemotelinkApplication;

public class RemotelinkApplicationJsonParser implements JsonObjectParser<RemotelinkApplication> {

    @Override
    public RemotelinkApplication parse(final JSONObject json) throws JSONException, URISyntaxException {
        final String type = json.optString("type", "");
        final String name = json.optString("name", "");

        return new RemotelinkApplication(type, name);
    }

}
