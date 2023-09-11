package me.glindholm.jira.rest.client.internal.json;

import java.net.URI;
import java.net.URISyntaxException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.RemotelinkIcon;

public class RemotelinkIconJsonParser implements JsonObjectParser<RemotelinkIcon> {
    @Override
    public RemotelinkIcon parse(final JSONObject json) throws JSONException, URISyntaxException {
        String str = json.optString("url16x16", null);

        final URI url16_16 = str == null ? null : JsonParseUtil.parseURI(str);

        str = json.optString("link", null);
        final URI link = str == null ? null : JsonParseUtil.parseURI(str);

        final String title = json.optString("title", "");

        return new RemotelinkIcon(url16_16, title, link);
    }

}
