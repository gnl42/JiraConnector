package me.glindholm.connector.commons.jira;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.connector.commons.jira.rss.JIRAException;

/**
 * User: kalamon
 * Date: 14.11.12
 * Time: 15:36
 */
public interface JiraRESTSupportTester {
    boolean supportsRest(ConnectionCfg server) throws JIRAException;
}
