package me.glindholm.jira.rest.client.internal.json;

import java.net.URISyntaxException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.RemotelinkIcon;
import me.glindholm.jira.rest.client.api.domain.RemotelinkStatus;

public class RemotelinkStatusJsonParser implements JsonObjectParser<RemotelinkStatus> {
    private final RemotelinkIconJsonParser iconParser = new RemotelinkIconJsonParser();

    @Override
    public RemotelinkStatus parse(final JSONObject json) throws JSONException, URISyntaxException {
        final Boolean resolved = json.optBoolean("resolved");
        final RemotelinkIcon icon = iconParser.parse(json.getJSONObject("icon"));
        return new RemotelinkStatus(resolved, icon);
    }

}
