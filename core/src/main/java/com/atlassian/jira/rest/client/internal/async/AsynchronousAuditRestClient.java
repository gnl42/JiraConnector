package com.atlassian.jira.rest.client.internal.async;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.AuditRestClient;
import com.atlassian.jira.rest.client.api.domain.AuditRecord;
import com.atlassian.jira.rest.client.api.domain.input.AuditRecordSearchInput;
import com.atlassian.jira.rest.client.internal.json.AuditRecordsJsonParser;
import com.atlassian.util.concurrent.Promise;

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
    public Promise<Iterable<AuditRecord>> getAuditRecords(final AuditRecordSearchInput input) {

        return getAndParse(createSearchPathFromInput(input), auditRecordsParser);
    }

    private URI createSearchPathFromInput( final AuditRecordSearchInput input) {
        final UriBuilder uriBuilder = UriBuilder.fromUri(baseUri);
        uriBuilder.path("auditing/record");

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
            uriBuilder.queryParam(("from"), input.getFrom());
        }

        if (input.getTo() != null) {
            uriBuilder.queryParam(("to"), input.getTo());
        }

        return uriBuilder.build();
    }
}
