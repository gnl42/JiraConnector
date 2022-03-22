package me.glindholm.connector.commons.jira.beans;

import java.util.List;

/**
 * User: kalamon
 * Date: 19.11.12
 * Time: 09:49
 */
public interface JiraFilter extends JIRAQueryFragment {
    String getJql();
    String getOldStyleQueryString();
    List<JIRAQueryFragment> getQueryFragments();
}
