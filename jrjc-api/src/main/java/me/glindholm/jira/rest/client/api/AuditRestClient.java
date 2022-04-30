package me.glindholm.jira.rest.client.api;

import io.atlassian.util.concurrent.Promise;
import me.glindholm.jira.rest.client.api.domain.AuditRecordInput;
import me.glindholm.jira.rest.client.api.domain.AuditRecordsData;
import me.glindholm.jira.rest.client.api.domain.input.AuditRecordSearchInput;

import javax.annotation.Nonnull;

/**
 * The me.glindholm.jira.rest.client.api handling audit record resources
 *
 * @since v2.0
 */
public interface AuditRestClient {

    Promise<AuditRecordsData> getAuditRecords(AuditRecordSearchInput input);

    void addAuditRecord(@Nonnull AuditRecordInput record);

}
