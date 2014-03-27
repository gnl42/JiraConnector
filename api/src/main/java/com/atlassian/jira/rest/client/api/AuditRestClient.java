package com.atlassian.jira.rest.client.api;

import com.atlassian.jira.rest.client.api.domain.AuditRecord;
import com.atlassian.jira.rest.client.api.domain.input.AuditRecordSearchInput;
import com.atlassian.util.concurrent.Promise;

/**
 * The com.atlassian.jira.rest.client.api handling audit record resources
 *
 * @since v2.0
 */
public interface AuditRestClient {

    Promise<Iterable<AuditRecord>> getAuditRecords(AuditRecordSearchInput input);

}
