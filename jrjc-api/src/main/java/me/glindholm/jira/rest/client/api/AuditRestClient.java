package me.glindholm.jira.rest.client.api;

import java.net.URISyntaxException;

import org.eclipse.jdt.annotation.NonNull;

import io.atlassian.util.concurrent.Promise;
import me.glindholm.jira.rest.client.api.domain.AuditRecordInput;
import me.glindholm.jira.rest.client.api.domain.AuditRecordsData;
import me.glindholm.jira.rest.client.api.domain.input.AuditRecordSearchInput;

/**
 * The me.glindholm.jira.rest.client.api handling audit record resources
 *
 * @since v2.0
 */
public interface AuditRestClient {

    Promise<AuditRecordsData> getAuditRecords(AuditRecordSearchInput input) throws URISyntaxException;

    void addAuditRecord(@NonNull AuditRecordInput record) throws URISyntaxException;

}
