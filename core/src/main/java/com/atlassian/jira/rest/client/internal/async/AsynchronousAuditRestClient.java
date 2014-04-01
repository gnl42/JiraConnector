package com.atlassian.jira.rest.client.internal.async;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.AuditRestClient;
import com.atlassian.jira.rest.client.api.domain.AuditRecordInput;
import com.atlassian.jira.rest.client.api.domain.AuditRecordsData;
import com.atlassian.jira.rest.client.api.domain.input.AuditRecordSearchInput;
import com.atlassian.jira.rest.client.internal.json.AuditRecordsJsonParser;
import com.atlassian.jira.rest.client.internal.json.Dates;
import com.atlassian.jira.rest.client.internal.json.gen.AuditRecordInputJsonGenerator;
import com.atlassian.util.concurrent.Promise;

import javax.annotation.Nonnull;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 *
 * @since v2.0
 */
public class AsynchronousAuditRestClient extends AbstractAsynchronousRestClient implements AuditRestClient {

    private final URI baseUri;
    private final AuditRecordsJsonParser auditRecordsParser = new AuditRecordsJsonParser();

    protected AsynchronousAuditRestClient(final HttpClient client, final URI baseUri) {
        super(client);
        this.baseUri = baseUri;
    }

    @Override
    public Promise<AuditRecordsData> getAuditRecords(final AuditRecordSearchInput input) {
        return getAndParse(createSearchPathFromInput(
                input == null ? new AuditRecordSearchInput(null, null, null, null, null) : input), auditRecordsParser);
    }

    protected UriBuilder createPathBuilder() {
        final UriBuilder uriBuilder = UriBuilder.fromUri(baseUri);
        uriBuilder.path("auditing/record");
        return uriBuilder;
    }

    @Override
    public void addAuditRecord(@Nonnull final AuditRecordInput record) {
        post(createPathBuilder().build(), record, new AuditRecordInputJsonGenerator()).claim();
    }

    private URI createSearchPathFromInput(final AuditRecordSearchInput input) {
        final UriBuilder uriBuilder = createPathBuilder();

        if (input.getOffset() != null) {
            uriBuilder.queryParam("offset", input.getOffset());
        }

        if (input.getLimit() != null) {
            uriBuilder.queryParam(("limit"), input.getLimit());
        }

        if (input.getTextFilter() != null) {
            uriBuilder.queryParam(("filter"), input.getTextFilter());
        }

        if (input.getFrom() != null) {
            uriBuilder.queryParam(("from"), Dates.asISODateTimeString(input.getFrom()));
        }

        if (input.getTo() != null) {
            uriBuilder.queryParam(("to"), Dates.asISODateTimeString(input.getTo()));
        }

        return uriBuilder.build();
    }
}
