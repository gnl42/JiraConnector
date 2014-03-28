package com.atlassian.jira.rest.client.api;

import com.atlassian.jira.rest.client.api.domain.AuditRecord;
import com.atlassian.jira.rest.client.api.domain.AuditRecordInput;
import com.atlassian.jira.rest.client.api.domain.input.AuditRecordSearchInput;
import com.atlassian.util.concurrent.Promise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The com.atlassian.jira.rest.client.api handling audit record resources
 *
 * @since v2.0
 */
public interface AuditRestClient {

    Promise<Iterable<AuditRecord>> getAuditRecords(@Nullable AuditRecordSearchInput input);

    void addAuditRecord(@Nonnull AuditRecordInput record);

}
