package me.glindholm.jira.rest.client.internal.json;

import java.net.URISyntaxException;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.Page;

public class PageJsonParser<T> implements JsonObjectParser<Page<T>> {

    private final GenericJsonArrayParser<T> valuesParser;

    public PageJsonParser(GenericJsonArrayParser<T> valuesParser) {
        this.valuesParser = valuesParser;
    }

    @Override
    public Page<T> parse(JSONObject json) throws JSONException, URISyntaxException {
        final long start = json.getLong("startAt");
        final int size = json.getInt("maxResults");
        final long total = json.getLong("total");
        final List<T> values = valuesParser.parse(json.getJSONArray("values"));
        final boolean isLast = json.getBoolean("isLast");

        return new Page<>(start, size, total, values, isLast);
    }
}
