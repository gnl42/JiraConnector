package com.atlassian.jira.rest.client.api;

import com.atlassian.jira.rest.client.api.domain.Permissions;
import com.atlassian.util.concurrent.Promise;

public interface MyPermissionsRestClient {
    Promise<Permissions> getMyPermissions(Object ...issueOrProjectKeyOrIds);
}
