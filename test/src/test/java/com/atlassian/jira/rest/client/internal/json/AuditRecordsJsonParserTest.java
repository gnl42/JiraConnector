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
public class AuditRecordsJsonParserTest {

    private final AuditRecordsJsonParser parser = new AuditRecordsJsonParser();

    @Test
    public void testParseValidResponse() throws Exception {
        final Iterable<AuditRecord> records = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/auditRecord/valid.json"));
        final Iterator<AuditRecord> recordsIterator = records.iterator();
        final AuditRecord firstRecord = recordsIterator.next();
        assertThat(firstRecord.getSummary(), is("User added to group"));
        assertThat(firstRecord.getRemoteAddress(), is("127.0.0.1"));
        assertThat(firstRecord.getCreated(), is(1395674708606L));
        assertThat(firstRecord.getCategory(), is("group management"));
        assertThat(firstRecord.getAuthorKey(), is("admin"));

        assertThat(firstRecord.getObjectItem().getId(), nullValue());
        assertThat(firstRecord.getObjectItem().getName(), is("jira-developers"));
        assertThat(firstRecord.getObjectItem().getTypeName(), is("GROUP"));
        assertThat(firstRecord.getObjectItem().getParentId(), nullValue());
        assertThat(firstRecord.getObjectItem().getParentName(), nullValue());

        final AuditAssociatedItem firstItem = firstRecord.getAssociatedItem().iterator().next();
        assertThat(firstItem.getId(), is("admin"));
        assertThat(firstItem.getName(), is("admin"));
        assertThat(firstItem.getTypeName(), is("USER"));
        assertThat(firstItem.getParentId(), is("1"));
        assertThat(firstItem.getParentName(), is("JIRA Internal Directory"));

        assertThat(firstRecord.getChangedValues(), notNullValue());

        final Iterator<AuditChangedValue> iterator = firstRecord.getChangedValues().iterator();
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

        //check second firstRecord which does not contain optional fields
        final AuditRecord secondRecord = recordsIterator.next();
        assertThat(secondRecord.getSummary(), is("User added to group"));
        assertThat(secondRecord.getRemoteAddress(), nullValue());
        assertThat(secondRecord.getCreated(), is(1395674708606L));
        assertThat(secondRecord.getCategory(), is("group management"));
        assertThat(secondRecord.getAuthorKey(), is("admin"));

        assertThat(secondRecord.getObjectItem().getId(), nullValue());
        assertThat(secondRecord.getObjectItem().getName(), is("jira-developers"));
        assertThat(secondRecord.getObjectItem().getTypeName(), is("GROUP"));
        assertThat(secondRecord.getObjectItem().getParentId(), nullValue());
        assertThat(secondRecord.getObjectItem().getParentName(), nullValue());

        final AuditAssociatedItem secondItem = secondRecord.getAssociatedItem().iterator().next();
        assertThat(secondItem.getName(), is("admin"));
        assertThat(secondItem.getTypeName(), is("USER"));
        assertThat(secondItem.getId(),  nullValue());
        assertThat(secondItem.getParentId(), nullValue());
        assertThat(secondItem.getParentName(), nullValue());

        assertThat(secondRecord.getChangedValues().iterator().hasNext(), is(false));
    }
}
