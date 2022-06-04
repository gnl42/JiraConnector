package it;

import com.atlassian.jira.rest.client.api.AuditRestClient;
import com.atlassian.jira.rest.client.api.List;
import com.atlassian.jira.rest.client.api.domain.AuditAssociatedItem;
import com.atlassian.jira.rest.client.api.domain.AuditChangedValue;
import com.atlassian.jira.rest.client.api.domain.AuditRecord;
import com.atlassian.jira.rest.client.api.domain.AuditRecordsData;
import com.atlassian.jira.rest.client.api.domain.input.AuditRecordBuilder;
import com.atlassian.jira.rest.client.api.domain.input.AuditRecordSearchInput;
import com.atlassian.jira.rest.client.api.domain.input.UserInput;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableWithSize;
import org.joda.time.DateMidnight;
import java.time.OffsetDateTime;
import org.joda.time.OffsetDateTimeZone;
import org.joda.time.Period;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AsynchronousAuditRestClientTest extends AbstractAsynchronousRestClientTest {

    private static final String USER_MANAGEMENT = "user management";
    private static final String FULL_NAME_JOHN = "John";
    private static final UserInput USER_JOHN = new UserInput(
            "key-john",
            "john",
            "john",
            "john@test",
            FULL_NAME_JOHN,
            "johnNotification",
            new ArrayList<>()
    );

    private static final String FULL_NAME_WILL = "Will";
    private static final UserInput USER_WILL = new UserInput(
            "key-will",
            "will",
            "will",
            "will@test",
            FULL_NAME_WILL,
            "willNotification",
            new ArrayList<>()
    );

    @Test
    public void testGetAllRecordsAndFilterThemByGivenAuditEvent() {
        client.getUserClient().createUser(USER_JOHN).claim();

        final List<AuditRecord> records = StreamSupport
                .stream(getAllAuditRecords().getRecords().spliterator(), false)
                .filter(input -> input.getSummary().equals("User created") && input.getCategory().equals(USER_MANAGEMENT))
                .filter(val -> findPersonThroughChangedValueFullName(val, FULL_NAME_JOHN))
                .collect(Collectors.toList());

        assertThat(records, hasSize(1));
        final AuditRecord record = records.get(0);
        assertThat(record.getAssociatedItems(), iterableWithSize(0));
        final Map<String, List<AuditChangedValue>> changedValueMap = createMap(record.getChangedValues());

        changedValueMap.values().forEach(list -> assertThat(list, hasSize(1)));

        final AuditChangedValue username = changedValueMap.get("Username").get(0);
        assertThat(username.getChangedTo(), equalTo("john"));

        final AuditChangedValue fullName = changedValueMap.get("Full name").get(0);
        assertThat(fullName.getChangedTo(), equalTo(FULL_NAME_JOHN));

        final AuditChangedValue email = changedValueMap.get("Email").get(0);
        assertThat(email.getChangedTo(), equalTo("john@test"));

        final AuditChangedValue active = changedValueMap.get("Active / Inactive").get(0);
        assertThat(active.getChangedTo(), equalTo("Active"));
    }

    /**
     * Condition allAuditRecordsSize > offsetedAuditRecordsSize is necessary due to Advanced Audit log introduced in
     * Jira 8.8.0. After Jira 8.8.0 it is hard to predict the number of audit events due to generation events from the
     * framework itself. One such an example event is "search performed".
     */
    @Test
    public void testGetRecordsWithOffset() {
        // given
        final AuditRecordsData allAuditRecords = getAllAuditRecords();

        // when
        final int offset = 3;
        final AuditRecordsData offsetedAuditRecordsData = client
                .getAuditRestClient()
                .getAuditRecords(new AuditRecordSearchInput(offset, null, null, null, null))
                .claim();

        // then
        final int offsetedAuditRecordsSize = Lists.newArrayList(offsetedAuditRecordsData.getRecords()).size();
        final int allAuditRecordsSize = Lists.newArrayList(allAuditRecords.getRecords()).size();

        assertTrue(String.format("Querying all entries should have return more entries that with offset(%d): all=%d, with offset=%d",
                offset, allAuditRecordsSize, offsetedAuditRecordsSize),
                allAuditRecordsSize > offsetedAuditRecordsSize);
    }

    @Test
    public void testGetRecordsWithLimit() {
        // when
        final int limit = 1;
        final AuditRecordsData limitedAuditRecordsData = client
                .getAuditRestClient()
                .getAuditRecords(new AuditRecordSearchInput(null, limit, null, null, null))
                .claim();

        // then
        final List<AuditRecord> limitedAuditRecords = Lists.newArrayList(limitedAuditRecordsData.getRecords());
        assertThat(limitedAuditRecords, hasSize(limit));
    }

    @Test
    public void testGetRecordsWithFilter() {
        client.getUserClient().createUser(USER_WILL).claim();
        final AuditRecordsData auditRecordsData = client
                .getAuditRestClient()
                .getAuditRecords(new AuditRecordSearchInput(null, null, "User created", null, null))
                .claim();
        final List<AuditRecord> records = StreamSupport
                .stream(auditRecordsData.getRecords().spliterator(), false)
                .filter(val -> findPersonThroughChangedValueFullName(val, FULL_NAME_WILL))
                .collect(Collectors.toList());
        assertThat(Iterables.size(records), is(1));

        final AuditRecord record = auditRecordsData.getRecords().iterator().next();
        assertThat(record.getCategory(), is(USER_MANAGEMENT));
    }

    @Test
    public void testAddSimpleRecord() {
        // given
        final AuditRestClient auditRestClient = client.getAuditRestClient();

        ImmutableList<AuditAssociatedItem> items = ImmutableList.of(
                new AuditAssociatedItem("", "admin", "USER", null, null),
                new AuditAssociatedItem("123", "Internal item", "PROJECT", null, ""));
        auditRestClient
                .addAuditRecord(new AuditRecordBuilder(USER_MANAGEMENT, "Event with associated items")
                        .setAssociatedItems(items)
                        .build());

        ImmutableList<AuditChangedValue> changedValues = ImmutableList.of(new AuditChangedValue("Test", "to", "from"));
        auditRestClient
                .addAuditRecord(new AuditRecordBuilder(USER_MANAGEMENT, "Event with changed values")
                        .setChangedValues(changedValues)
                        .build());

        auditRestClient
                .addAuditRecord(new AuditRecordBuilder(USER_MANAGEMENT, "Adding new event").build());
        final int numberOfAddedRecords = 3;

        // when
        final Iterable<AuditRecord> auditRecords = auditRestClient.
                getAuditRecords(new AuditRecordSearchInput(null, numberOfAddedRecords, null, null, null))
                .claim()
                .getRecords();

        // then
        assertThat(auditRecords, IsIterableWithSize.iterableWithSize(numberOfAddedRecords));
        AuditRecord record = Iterables.get(auditRecords, 0);
        assertThat(record.getSummary(), is("Adding new event"));
        assertThat(record.getCategory(), is(USER_MANAGEMENT));
        assertThat(record.getAssociatedItems().isSupported(), is(false));
        assertThat(record.getChangedValues(), IsIterableWithSize.iterableWithSize(0));
        assertThat(record.getRemoteAddress(), notNullValue());
        assertThat(record.getCreated(), notNullValue());

        record = Iterables.get(auditRecords, 1);
        assertThat(record.getSummary(), is("Event with changed values"));
        assertThat(record.getAssociatedItems().isSupported(), is(false));
        assertThat(record.getChangedValues(), IsIterableWithSize.iterableWithSize(1));
        AuditChangedValue value = Iterables.get(record.getChangedValues(), 0);
        assertThat(value.getChangedFrom(), is("from"));
        assertThat(value.getChangedTo(), is("to"));
        assertThat(value.getFieldName(), is("Test"));

        record = Iterables.get(auditRecords, 2);
        assertThat(record.getSummary(), is("Event with associated items"));
        assertThat(record.getAssociatedItems(), IsIterableWithSize.iterableWithSize(2));
        assertThat(record.getChangedValues(), IsIterableWithSize.iterableWithSize(0));

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

    @Test
    public void shouldReturnNoRecordsWhenFilteringForTomorrow() {
        final OffsetDateTime tomorrow = new DateMidnight().plus(Period.days(1)).toOffsetDateTime();

        final AuditRecordsData auditRecordsData = client.getAuditRestClient().getAuditRecords(new AuditRecordSearchInput(null, null, null, tomorrow, tomorrow)).claim();

        assertThat(auditRecordsData.getRecords(), Matchers.emptyIterable());
    }

    @Test
    public void shouldReturnAllRecordsWhenFilteringToLatestCreationDate() {
        // given
        final AuditRecordsData firstPageOfRecords = getAllAuditRecords();
        final AuditRecord latestCreatedRecord = getLatestCreatedRecord(firstPageOfRecords);
        final OffsetDateTime latestCreatedDate = latestCreatedRecord.getCreated();

        // when
        final AuditRecordSearchInput toLatestSearchCriteria = new AuditRecordSearchInput(null, null, null, null, latestCreatedDate);
        final AuditRecordsData auditRecordsData = client.getAuditRestClient().getAuditRecords(toLatestSearchCriteria).claim();

        // then
        assertThat(auditRecordsData.getRecords(), hasSameIdsAs(firstPageOfRecords.getRecords()));
    }

    @Test
    public void shouldReturnLatestItemWhenFilteringFromLatestCreationDate() {
        final AuditRecord latestCreatedRecord = getLatestCreatedRecord();
        final OffsetDateTime latestCreatedDate = latestCreatedRecord.getCreated();
        final OffsetDateTime latestCreatedDateInStrangeTimezone = latestCreatedDate.toOffsetDateTime(OffsetDateTimeZone.forOffsetHours(-5));

        // when
        final AuditRecordSearchInput fromLatestSearchCriteria = new AuditRecordSearchInput(null, null, null, latestCreatedDateInStrangeTimezone, null);
        final AuditRecordsData auditRecordsData = client.getAuditRestClient().getAuditRecords(fromLatestSearchCriteria).claim();

        // then
        assertThat(auditRecordsData.getRecords(), Matchers.hasItem(auditRecordWithId(latestCreatedRecord.getId())));
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
        final AuditRecordsData allAuditRecordData = getAllAuditRecords();

        return getLatestCreatedRecord(allAuditRecordData);
    }

    private AuditRecord getLatestCreatedRecord(final AuditRecordsData allAuditRecordData) {
        final Ordering<AuditRecord> createdTimeAscendingOrdering = new Ordering<AuditRecord>() {
            @Override
            public int compare(@Nullable AuditRecord left, @Nullable AuditRecord right) {
                final OffsetDateTime leftCreatedTime = left.getCreated();
                final OffsetDateTime rightCreatedTime = right.getCreated();

                return leftCreatedTime.compareTo(rightCreatedTime);
            }
        };
        final AuditRecord latestAuditRecord = createdTimeAscendingOrdering.max(allAuditRecordData.getRecords());

        return latestAuditRecord;
    }

    private Map<String, List<AuditChangedValue>> createMap(final List<AuditChangedValue> changedValues) {
        return StreamSupport
                .stream(changedValues.spliterator(), false)
                .collect(Collectors.groupingBy(AuditChangedValue::getFieldName));
    }

    private AuditRecordsData getAllAuditRecords() {
        return client
                .getAuditRestClient()
                .getAuditRecords(new AuditRecordSearchInput(null, null, null, null, null))
                .claim();
    }

    private boolean findPersonThroughChangedValueFullName(final AuditRecord record, final String fullName) {
        final List<AuditChangedValue> changedValues = record.getChangedValues();
        return StreamSupport.stream(changedValues.spliterator(), false)
                .filter(val -> val.getFieldName().equals("Full name") && val.getChangedTo().equals(fullName))
                .count() == 1;
    }
}
