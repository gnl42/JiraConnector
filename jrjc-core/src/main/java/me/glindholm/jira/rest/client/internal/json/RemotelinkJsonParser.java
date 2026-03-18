package me.glindholm.jira.rest.client.internal.json;

import java.net.URI;
import java.net.URISyntaxException;

import me.glindholm.jira.rest.client.api.domain.Remotelink;
import me.glindholm.jira.rest.client.api.domain.RemotelinkApplication;
import me.glindholm.jira.rest.client.api.domain.RemotelinkObject;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONException;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONObject;

public class RemotelinkJsonParser implements JsonObjectParser<Remotelink> {
    private final RemotelinkApplicationJsonParser applicationParser = new RemotelinkApplicationJsonParser();
    private final RemotelinkObjectJsonParser objectParser = new RemotelinkObjectJsonParser();

    @Override
    public Remotelink parse(final JSONObject json) throws JSONException, URISyntaxException {
        final String globalId = json.optString("globalId", null);
        final String relationship = json.optString("relationship", "");
        final Long id = json.getLong("id");
        final URI self = JsonParseUtil.getSelfUri(json);

        final RemotelinkApplication application = applicationParser.parse(json.getJSONObject("application"));
        final RemotelinkObject object = objectParser.parse(json.getJSONObject("object"));

        return new Remotelink(globalId, relationship, id, self, application, object);
    }

}
