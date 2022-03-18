package com.atlassian.connector.commons.jira;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.jira.rss.JIRAException;

/**
 * User: kalamon
 * Date: 14.11.12
 * Time: 15:36
 */
public interface JiraRESTSupportTester {
    boolean supportsRest(ConnectionCfg server) throws JIRAException;
}
