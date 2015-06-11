package it;

import com.atlassian.jira.nimblefunctests.annotation.JiraBuildNumberDependent;
import com.atlassian.jira.nimblefunctests.annotation.Restore;
import com.atlassian.jira.rest.client.api.AuditRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.AuditRecordBuilder;
import com.atlassian.jira.rest.client.api.domain.input.AuditRecordSearchInput;
import com.atlassian.jira.rest.client.api.domain.input.ComponentInput;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableWithSize;
import org.joda.time.*;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;


@Restore(TestConstants.DEFAULT_JIRA_DUMP_FILE)
public class AsynchronousAuditRestClientTest extends AbstractAsynchronousRestClientTest {

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
        // given
        final AuditRecordsData firstPageOfRecords = client.getAuditRestClient().getAuditRecords(new AuditRecordSearchInput(null, null, null, null, null)).claim();

        // when
        final int offset = 1;
        final AuditRecordsData offsetedAuditRecordsData = client.getAuditRestClient().getAuditRecords(new AuditRecordSearchInput(offset, null, null, null, null)).claim();

        // then
        final List<AuditRecord> offsetedAuditRecords = Lists.newArrayList(offsetedAuditRecordsData.getRecords());
        final List<AuditRecord> allAuditRecords = Lists.newArrayList(firstPageOfRecords.getRecords());
        assertThat(offsetedAuditRecords, hasSize(allAuditRecords.size() - offset));
        assertThat(offsetedAuditRecords.get(0), auditRecordWithId(allAuditRecords.get(offset).getId()));
    }

    @JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_6_3)
    @Test
    public void testGetRecordsWithLimit() {
        // given
        final AuditRecordsData firstPageOfRecords = client.getAuditRestClient().getAuditRecords(new AuditRecordSearchInput(null, null, null, null, null)).claim();

        // when
        final int limit = 1;
        final AuditRecordsData limitedAuditRecordsData = client.getAuditRestClient().getAuditRecords(new AuditRecordSearchInput(null, limit, null, null, null)).claim();

        // then
        final List<AuditRecord> limitedAuditRecords = Lists.newArrayList(limitedAuditRecordsData.getRecords());
        assertThat(limitedAuditRecords, hasSize(limit));
        final AuditRecord record = limitedAuditRecords.get(0);
        assertThat(record, auditRecordWithId(Iterables.get(firstPageOfRecords.getRecords(), 0).getId()));
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
        // given
        final AuditRestClient auditRestClient = client.getAuditRestClient();

        ImmutableList<AuditAssociatedItem> items = ImmutableList.of(
                new AuditAssociatedItem("", "admin", "USER", null, null),
                new AuditAssociatedItem("123", "Internal item", "PROJECT", null, ""));
        auditRestClient
                .addAuditRecord(new AuditRecordBuilder("user management", "Event with associated items")
                        .setAssociatedItems(items)
                        .build());

        ImmutableList<AuditChangedValue> changedValues = ImmutableList.of(new AuditChangedValue("Test", "to", "from"));
        auditRestClient
                .addAuditRecord(new AuditRecordBuilder("user management", "Event with changed values")
                        .setChangedValues(changedValues)
                        .build());

        auditRestClient
                .addAuditRecord(new AuditRecordBuilder("user management", "Adding new event").build());
        final int numberOfAddedRecords = 3;

        // when
        final Iterable<AuditRecord> auditRecords = auditRestClient.
                getAuditRecords(new AuditRecordSearchInput(null, numberOfAddedRecords, null, null, null))
                .claim()
                .getRecords();

        // then
        assertThat(auditRecords, IsIterableWithSize.<AuditRecord>iterableWithSize(numberOfAddedRecords));
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

    @JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_6_3)
    @Test
    public void shouldReturnNoRecordsWhenFilteringForTomorrow() {
        final DateTime tomorrow = new DateMidnight().plus(Period.days(1)).toDateTime();

        final AuditRecordsData auditRecordsData = client.getAuditRestClient().getAuditRecords(new AuditRecordSearchInput(null, null, null, tomorrow, tomorrow)).claim();

        assertThat(auditRecordsData.getRecords(), Matchers.<AuditRecord>emptyIterable());
    }

    @JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_6_3)
    @Test
    public void shouldReturnAllRecordsWhenFilteringToLatestCreationDate() {
        // given
        final AuditRecordsData firstPageOfRecords = client.getAuditRestClient().getAuditRecords(new AuditRecordSearchInput(null, null, null, null, null)).claim();
        final AuditRecord latestCreatedRecord = getLatestCreatedRecord(firstPageOfRecords);
        final DateTime latestCreatedDate = latestCreatedRecord.getCreated();

        // when
        final AuditRecordSearchInput toLatestSearchCriteria = new AuditRecordSearchInput(null, null, null, null, latestCreatedDate);
        final AuditRecordsData auditRecordsData = client.getAuditRestClient().getAuditRecords(toLatestSearchCriteria).claim();

        // then
        assertThat(auditRecordsData.getRecords(), hasSameIdsAs(firstPageOfRecords.getRecords()));
    }

    @JiraBuildNumberDependent(ServerVersionConstants.BN_JIRA_6_3)
    @Test
    public void shouldReturnLatestItemWhenFilteringFromLatestCreationDate() {
        final AuditRecord latestCreatedRecord = getLatestCreatedRecord();
        final DateTime latestCreatedDate = latestCreatedRecord.getCreated();
        final DateTime latestCreatedDateInStrangeTimezone = latestCreatedDate.toDateTime(DateTimeZone.forOffsetHours(-5));

        // when
        final AuditRecordSearchInput fromLatestSearchCriteria = new AuditRecordSearchInput(null, null, null, latestCreatedDateInStrangeTimezone, null);
        final AuditRecordsData auditRecordsData = client.getAuditRestClient().getAuditRecords(fromLatestSearchCriteria).claim();

        // then
        assertThat(auditRecordsData.getRecords(), Matchers.<AuditRecord>hasItem(auditRecordWithId(latestCreatedRecord.getId())));
    }

    private Matcher<AuditRecord> auditRecordWithId(final Long expectedId) {
        return new BaseMatcher<AuditRecord>() {
            @Override
            public boolean matches(Object o) {
                AuditRecord current = (AuditRecord) o;
                return current.getId().equals(expectedId);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Contains item with id: " + expectedId);
            }
        };
    }

    private Matcher<Iterable<? extends AuditRecord>> hasSameIdsAs(final Iterable<AuditRecord> auditLogRecords) {
        final List<Matcher<? super AuditRecord>> existingIdsMatchers = Lists.newArrayList(Iterables.transform(auditLogRecords, new Function<AuditRecord, Matcher<? super AuditRecord>>() {
            @Override
            public Matcher<AuditRecord> apply(final AuditRecord auditRecord) {
                return auditRecordWithId(auditRecord.getId());
            }
        }));
        return contains(existingIdsMatchers);
    }

    private AuditRecord getLatestCreatedRecord() {
        final AuditRecordsData allAuditRecordData = client.getAuditRestClient().getAuditRecords(new AuditRecordSearchInput(null, null, null, null, null)).claim();

        return getLatestCreatedRecord(allAuditRecordData);
    }

    private AuditRecord getLatestCreatedRecord(final AuditRecordsData allAuditRecordData) {
        final Ordering<AuditRecord> createdTimeAscendingOrdering = new Ordering<AuditRecord>() {
            @Override
            public int compare(@Nullable AuditRecord left, @Nullable AuditRecord right) {
                final DateTime leftCreatedTime = left.getCreated();
                final DateTime rightCreatedTime = right.getCreated();

                return leftCreatedTime.compareTo(rightCreatedTime);
            }
        };
        final AuditRecord latestAuditRecord = createdTimeAscendingOrdering.max(allAuditRecordData.getRecords());

        return latestAuditRecord;
    }
}
