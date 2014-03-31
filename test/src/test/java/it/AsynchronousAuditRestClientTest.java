package it;

import com.atlassian.jira.nimblefunctests.annotation.JiraBuildNumberDependent;
import com.atlassian.jira.nimblefunctests.annotation.Restore;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.AuditRecordBuilder;
import com.atlassian.jira.rest.client.api.domain.input.AuditRecordSearchInput;
import com.atlassian.jira.rest.client.api.domain.input.ComponentInput;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableWithSize;
import org.hamcrest.core.IsNull;
import org.junit.Test;

import java.util.Iterator;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;


@Restore(TestConstants.DEFAULT_JIRA_DUMP_FILE)
public class AsynchronousAuditRestClientTest  extends AbstractAsynchronousRestClientTest {

    @JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_6_3)
    @Test
    public void testGetRecords() {

        final Component component = client.getComponentClient().createComponent("TST", new ComponentInput("New TST Component", null, null, null)).claim();
        assertNotNull(component);

        final AuditRecordsData auditRecordsData = client.getAuditRestClient().getAuditRecords(new AuditRecordSearchInput(null, null, null, null, null)).claim();
        final Iterable<AuditRecord> filterResult = Iterables.filter(auditRecordsData.getRecords(), new Predicate<AuditRecord>() {
            @Override
            public boolean apply(final AuditRecord input) {
                return input.getSummary().equals("Project component created") &&
                        input.getObjectItem().getName().equals("New TST Component");
            }
        });

        final Iterator<AuditRecord> iterator = filterResult.iterator();
        assertThat(iterator.hasNext(), is(true));
        final AuditRecord record = iterator.next();
        assertThat(record.getAuthorKey(), is("admin"));
        assertThat(record.getObjectItem().getTypeName(), is("PROJECT_COMPONENT"));
        assertThat(record.getCreated(), is(Matchers.notNullValue()));

        final Iterator<AuditAssociatedItem> itemIterator = record.getAssociatedItems().iterator();
        final AuditAssociatedItem item1 = itemIterator.next();
        assertThat(item1.getName(), is("Test Project"));
        assertThat(item1.getTypeName(), is("PROJECT"));

        final AuditAssociatedItem item2 = itemIterator.next();
        assertThat(item2.getName(), is("admin"));
        assertThat(item2.getTypeName(), is("USER"));
        assertThat(item2.getParentId(), is("1"));
        assertThat(item2.getParentName(), is("JIRA Internal Directory"));

        final Iterator<AuditChangedValue> valuesIterator = record.getChangedValues().iterator();
        final AuditChangedValue value1 = valuesIterator.next();
        assertThat(value1.getFieldName(), is("Name"));
        assertThat(value1.getChangedTo(), is("New TST Component"));

         final AuditChangedValue value2 = valuesIterator.next();
        assertThat(value2.getFieldName(), is("Default Assignee"));
        assertThat(value2.getChangedTo(), is("Project Default"));
    }

    @JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_6_3)
    @Test
    public void testGetRecordsWithOffset() {
        final AuditRecordsData auditRecordsData = client.getAuditRestClient().getAuditRecords(new AuditRecordSearchInput(1, null, null, null, null)).claim();
        assertThat(Iterables.size(auditRecordsData.getRecords()), is(2));

        final AuditRecord record = auditRecordsData.getRecords().iterator().next();
        assertThat(record.getId(), is(10001L));
    }

    @JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_6_3)
    @Test
    public void testGetRecordsWithLimit() {
        final AuditRecordsData auditRecordsData = client.getAuditRestClient().getAuditRecords(new AuditRecordSearchInput(null, 1, null, null, null)).claim();
        assertThat(Iterables.size(auditRecordsData.getRecords()), is(1));

        final AuditRecord record = auditRecordsData.getRecords().iterator().next();
        assertThat(record.getId(), is(10002L));
    }

    @JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_6_3)
    @Test
    public void testGetRecordsWithFilter() {
        final AuditRecordsData auditRecordsData = client.getAuditRestClient().getAuditRecords(new AuditRecordSearchInput(null, null, "reporter", null, null)).claim();
        assertThat(Iterables.size(auditRecordsData.getRecords()), is(1));

        final AuditRecord record = auditRecordsData.getRecords().iterator().next();
        assertThat(record.getId(), is(10001L));
    }

    @JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_6_3)
    @Test
    public void testAddSimpleRecord() {
        ImmutableList<AuditAssociatedItem> items = ImmutableList.of(
                new AuditAssociatedItem("", "admin", "USER", null, null),
                new AuditAssociatedItem("123", "Internal item", "PROJECT", null, ""));
        client.getAuditRestClient().addAuditRecord(new AuditRecordBuilder("user management", "Event with associated items")
                .setAssociatedItems(items)
                .build());

        ImmutableList<AuditChangedValue> changedValues = ImmutableList.of(new AuditChangedValue("Test", "to", "from"));
        client.getAuditRestClient().addAuditRecord(new AuditRecordBuilder("user management", "Event with changed values")
                .setChangedValues(changedValues)
                .build());

        client.getAuditRestClient().addAuditRecord(new AuditRecordBuilder("user management", "Adding new event").build());

        final Iterable<AuditRecord> auditRecords = client.getAuditRestClient().getAuditRecords(null).claim().getRecords();
        assertThat(auditRecords, IsIterableWithSize.<AuditRecord>iterableWithSize(6));

        AuditRecord record = Iterables.get(auditRecords, 0);
        assertThat(record.getSummary(), is("Adding new event"));
        assertThat(record.getCategory(), is("user management"));
        assertThat(record.getObjectItem(), nullValue());
        assertThat(record.getAssociatedItems().isSupported(), is(false));
        assertThat(record.getAuthorKey(), is("admin"));
        assertThat(record.getChangedValues(), IsIterableWithSize.<AuditChangedValue>iterableWithSize(0));
        assertThat(record.getRemoteAddress(), notNullValue());
        assertThat(record.getCreated(), notNullValue());

        record = Iterables.get(auditRecords, 1);
        assertThat(record.getSummary(), is("Event with changed values"));
        assertThat(record.getAssociatedItems().isSupported(), is(false));
        assertThat(record.getChangedValues(), IsIterableWithSize.<AuditChangedValue>iterableWithSize(1));
        AuditChangedValue value = Iterables.get(record.getChangedValues(), 0);
        assertThat(value.getChangedFrom(), is("from"));
        assertThat(value.getChangedTo(), is("to"));
        assertThat(value.getFieldName(), is("Test"));

        record = Iterables.get(auditRecords, 2);
        assertThat(record.getSummary(), is("Event with associated items"));
        assertThat(record.getAssociatedItems(), IsIterableWithSize.<AuditAssociatedItem>iterableWithSize(2));
        assertThat(record.getChangedValues(), IsIterableWithSize.<AuditChangedValue>iterableWithSize(0));

        AuditAssociatedItem item = Iterables.get(record.getAssociatedItems(), 0);
        assertThat(item.getId(), nullValue());
        assertThat(item.getName(), is("admin"));
        assertThat(item.getParentId(), nullValue());
        assertThat(item.getParentName(), nullValue());
        assertThat(item.getTypeName(), is("USER"));

        item = Iterables.get(record.getAssociatedItems(), 1);
        assertThat(item.getId(), is("123"));
        assertThat(item.getName(), is("Internal item"));
        assertThat(item.getParentId(), nullValue());
        assertThat(item.getParentName(), nullValue());
        assertThat(item.getTypeName(), is("PROJECT"));
    }
}
