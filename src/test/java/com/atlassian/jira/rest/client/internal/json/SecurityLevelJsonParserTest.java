package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.TestUtil;
import com.atlassian.jira.rest.client.domain.SecurityLevel;
import org.junit.Assert;
import org.junit.Test;

/**
 * User: kalamon
 * Date: 05.12.12
 * Time: 10:52
 */
public class SecurityLevelJsonParserTest {
    @Test
    public void testParse() throws Exception {
        final SecurityLevelJsonParser parser = new SecurityLevelJsonParser();
        final SecurityLevel sl = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/securityLevel/valid.json"));
        Assert.assertEquals(new SecurityLevel(TestUtil.toUri("http://localhost:8083/rest/api/2/securitylevel/10001"), 10001L, "foo", "bar"), sl);
    }
}
