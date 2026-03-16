package me.glindholm.jira.rest.client.internal.json;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import me.glindholm.jira.rest.client.TestUtil;
import me.glindholm.jira.rest.client.api.domain.IssuelinksType;

public class IssueLinkTypesJsonParserTest {
    @Test
    public void testParse() throws Exception {
        IssueLinkTypesJsonParser parser = new IssueLinkTypesJsonParser();
        final List<IssuelinksType> issueLinks = parser.parse(ResourceUtil
                .getJsonObjectFromResource("/json/issueLinks/issue-links-5.0.json"));
        assertEquals(8, issueLinks.size());
        assertEquals(new IssuelinksType(TestUtil.toUri("https://jdog.atlassian.com/rest/api/2/issueLinkType/10160"),
                "10160", "Bonfire Testing", "discovered while testing", "testing discovered"), issueLinks.get(0));

        assertEquals(new IssuelinksType(TestUtil.toUri("https://jdog.atlassian.com/rest/api/2/issueLinkType/10020"),
                "10020", "Relates", "is related to", "relates to"), issueLinks.get(issueLinks.size() - 1));
    }
}
