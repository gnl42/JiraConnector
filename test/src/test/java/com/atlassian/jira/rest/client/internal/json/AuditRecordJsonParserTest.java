package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.AuditAssociatedItem;
import com.atlassian.jira.rest.client.api.domain.AuditChangedValue;
import com.atlassian.jira.rest.client.api.domain.AuditRecord;
import org.junit.Test;

import java.util.Iterator;

import static org.hamcrest.CoreMatchers.*;

import static org.junit.Assert.assertThat;

/**
 *
 * @since v2.0
 */
public class AuditRecordJsonParserTest {

    private final AuditRecordJsonParser parser = new AuditRecordJsonParser();

    @Test
    public void testParseValidResponse() throws Exception {
        final AuditRecord records = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/auditRecord/valid.json"));
        assertThat(records.getSummary(), is("User added to group"));
        assertThat(records.getRemoteAddress(), is("127.0.0.1"));
        assertThat(records.getCreated(), is(1395674708606L));
        assertThat(records.getCategory(), is("group management"));

        assertThat(records.getObjectItem().getId(), nullValue());
        assertThat(records.getObjectItem().getName(), is("jira-developers"));
        assertThat(records.getObjectItem().getTypeName(), is("GROUP"));
        assertThat(records.getObjectItem().getParentId(), nullValue());
        assertThat(records.getObjectItem().getParentName(), nullValue());

        final AuditAssociatedItem item = records.getAssociatedItem().iterator().next();
        assertThat(item.getId(), is("admin"));
        assertThat(item.getName(), is("admin"));
        assertThat(item.getTypeName(), is("USER"));
        assertThat(item.getParentId(), is("1"));
        assertThat(item.getParentName(), is("JIRA Internal Directory"));

        assertThat(records.getChangedValues(), notNullValue());

        final Iterator<AuditChangedValue> iterator = records.getChangedValues().iterator();
        final AuditChangedValue valueItem1 = iterator.next();
        assertThat(valueItem1.getFieldName(), is("Username"));
        assertThat(valueItem1.getChangedFrom(), nullValue());
        assertThat(valueItem1.getChangedTo(), is("admin"));

        final AuditChangedValue valueItem2 = iterator.next();
        assertThat(valueItem2.getFieldName(), is("Full Name"));
        assertThat(valueItem2.getChangedFrom(), is("administrator"));
        assertThat(valueItem2.getChangedTo(), is("admin"));

        final AuditChangedValue valueItem3 = iterator.next();
        assertThat(valueItem3.getFieldName(), is("Email"));
        assertThat(valueItem3.getChangedTo(), is("admin@local.com"));

        final AuditChangedValue valueItem4 = iterator.next();
        assertThat(valueItem4.getFieldName(), is("Active / Inactive"));
        assertThat(valueItem4.getChangedTo(), is("Active"));
    }

    @Test
    public void testParseResponseWithoutSomeOptionalFields() throws Exception {
        final AuditRecord records = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/auditRecord/valid_no_optional_filelds.json"));
        assertThat(records.getSummary(), is("User added to group"));
        assertThat(records.getRemoteAddress(), nullValue());
        assertThat(records.getCreated(), is(1395674708606L));
        assertThat(records.getCategory(), is("group management"));

        assertThat(records.getObjectItem().getId(), nullValue());
        assertThat(records.getObjectItem().getName(), is("jira-developers"));
        assertThat(records.getObjectItem().getTypeName(), is("GROUP"));
        assertThat(records.getObjectItem().getParentId(), nullValue());
        assertThat(records.getObjectItem().getParentName(), nullValue());

        final AuditAssociatedItem item = records.getAssociatedItem().iterator().next();
        assertThat(item.getName(), is("admin"));
        assertThat(item.getTypeName(), is("USER"));
        assertThat(item.getId(),  nullValue());
        assertThat(item.getParentId(), nullValue());
        assertThat(item.getParentName(), nullValue());

        assertThat(records.getChangedValues().iterator().hasNext(), is(false));

    }

}
