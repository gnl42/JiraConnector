package me.glindholm.jira.rest.client.internal.json;

import java.net.URI;
import java.net.URISyntaxException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.RemotelinkIcon;
import me.glindholm.jira.rest.client.api.domain.RemotelinkObject;
import me.glindholm.jira.rest.client.api.domain.RemotelinkStatus;

public class RemotelinkObjectJsonParser implements JsonObjectParser<RemotelinkObject> {
    private final RemotelinkIconJsonParser iconParser = new RemotelinkIconJsonParser();
    private final RemotelinkStatusJsonParser statusParser = new RemotelinkStatusJsonParser();

    @Override
    public RemotelinkObject parse(final JSONObject json) throws JSONException, URISyntaxException {
        final URI url = JsonParseUtil.parseURI(json.getString("url"));
        final String title = json.getString("title");
        final String summary = json.optString("summary", "");
        final RemotelinkIcon icon = iconParser.parse(json.getJSONObject("icon"));
        final RemotelinkStatus status = statusParser.parse(json.getJSONObject("status"));

        return new RemotelinkObject(url, title, summary, icon, status);
    }

}
