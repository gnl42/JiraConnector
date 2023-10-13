package me.glindholm.jira.rest.client.internal.json;

import static junit.framework.Assert.assertEquals;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import me.glindholm.jira.rest.client.api.domain.IssueType;
import me.glindholm.jira.rest.client.api.domain.Page;

public class PageJsonParserTest {

    @Test
    public void testParse() throws Exception {
        JSONObject json =  ResourceUtil.getJsonObjectFromResource("/json/page/valid-page.json");
        PageJsonParser<IssueType> parser = new PageJsonParser<>(new GenericJsonArrayParser<>(new IssueTypeJsonParser()));

        Page<IssueType> page = parser.parse(json);

        assertEquals(5, page.getTotal());
        assertEquals(50, page.getMaxResults());
        assertEquals(0, page.getStartAt());
        assertEquals(true, page.isLast());
        assertEquals(5, page.getValues().size());
    }
}
