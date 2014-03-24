package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.AuditRecord;
import org.junit.Test;

/**
 * TODO: Document this class / interface here
 *
 * @since v2.0
 */
public class AuditRecordJsonParserTest {

    private final AuditRecordJsonParser parser = new AuditRecordJsonParser();

    @Test
    public void testParse() throws Exception {
        final AuditRecord record = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/auditRecord/valid.json"));
        final AuditRecord record2 = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/auditRecord/valid_no_optional_filelds.json"));
    }
}
