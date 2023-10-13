package me.glindholm.jira.rest.client.internal.async;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hc.core5.net.URIBuilder;
import org.eclipse.jdt.annotation.NonNull;

import com.atlassian.httpclient.api.HttpClient;

import io.atlassian.util.concurrent.Promise;
import me.glindholm.jira.rest.client.api.AuditRestClient;
import me.glindholm.jira.rest.client.api.domain.AuditRecordInput;
import me.glindholm.jira.rest.client.api.domain.AuditRecordsData;
import me.glindholm.jira.rest.client.api.domain.input.AuditRecordSearchInput;
import me.glindholm.jira.rest.client.internal.json.AuditRecordsJsonParser;
import me.glindholm.jira.rest.client.internal.json.JsonParseUtil;
import me.glindholm.jira.rest.client.internal.json.gen.AuditRecordInputJsonGenerator;

/**
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
    public Promise<AuditRecordsData> getAuditRecords(final AuditRecordSearchInput input) throws URISyntaxException {
        return getAndParse(createSearchPathFromInput(input == null ? new AuditRecordSearchInput(null, null, null, null, null) : input), auditRecordsParser);
    }

    protected URIBuilder createPathBuilder() {
        final URIBuilder uriBuilder = new URIBuilder(baseUri);
        uriBuilder.appendPath("auditing/record");
        return uriBuilder;
    }

    @Override
    public void addAuditRecord(@NonNull final AuditRecordInput record) throws URISyntaxException {
        post(createPathBuilder().build(), record, new AuditRecordInputJsonGenerator()).claim();
    }

    private URI createSearchPathFromInput(final AuditRecordSearchInput input) throws URISyntaxException {
        final URIBuilder uriBuilder = createPathBuilder();
        if (input.getOffset() != null) {
            uriBuilder.addParameter("offset", String.valueOf(input.getOffset()));
        }

        if (input.getLimit() != null) {
            uriBuilder.addParameter("limit", String.valueOf(input.getLimit()));
        }

        if (input.getTextFilter() != null) {
            uriBuilder.addParameter("filter", input.getTextFilter());
        }

        if (input.getFrom() != null) {
            final String fromIsoString = input.getFrom().format(JsonParseUtil.JIRA_DATE_TIME_FORMATTER);
            uriBuilder.addParameter("from", fromIsoString);
        }

        if (input.getTo() != null) {
            final String toIsoString = input.getTo().format(JsonParseUtil.JIRA_DATE_TIME_FORMATTER);
            uriBuilder.addParameter("to", toIsoString);
        }

        try {
            return uriBuilder.build();
        } catch (final RuntimeException x) {
            throw x;
        }
    }
}
