package me.glindholm.theplugin.jira;

import me.glindholmjira.rest.client.domain.FavouriteFilter;
import me.glindholmjira.rest.client.internal.json.FavouriteFilterJsonParser;
import me.glindholmjira.rest.client.internal.json.GenericJsonArrayParser;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * User: kalamon
 * Date: 02.04.13
 * Time: 16:56
 */
public class RestParserTest extends TestCase {
    private static JSONObject getJsonObjectFromResource(String resourcePath) {
        final String s = getStringFromResource(resourcePath);
        try {
            return new JSONObject(s);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static JSONArray getJsonArrayFromResource(String resourcePath) {
        final String s = getStringFromResource(resourcePath);
        try {
            return new JSONArray(s);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    private static String getStringFromResource(String resourcePath) {
        final String s;
        try {
            final InputStream is = RestParserTest.class.getResourceAsStream(resourcePath);
            if (is == null) {
                throw new IOException("Cannot open resource [" + resourcePath + "]");
            }
            s = IOUtils.toString(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return s;
    }

    public void testPl2818() {
        GenericJsonArrayParser<FavouriteFilter> favouriteFiltersJsonParser = GenericJsonArrayParser.create(new FavouriteFilterJsonParser());
        try {
            JSONArray array = getJsonArrayFromResource("/mock/jira/pl-2818.json");
            Iterable<FavouriteFilter> filters = favouriteFiltersJsonParser.parse(array);
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }
}
