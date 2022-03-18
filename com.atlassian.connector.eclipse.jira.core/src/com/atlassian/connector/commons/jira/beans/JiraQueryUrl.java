/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.connector.commons.jira.beans;

import com.atlassian.connector.commons.jira.cache.CacheConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pmaruszak
 */

public final class JiraQueryUrl {
    private static final int NOT_INITIALIZED = -10;
    private static final String ISSUE_NAVIGATOR =
            "/secure/IssueNavigator.jspa?refreshFilter=false&reset=update&show=View+%3E%3E";
    private static final String ISSUE_RSS = "/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?";
    private static final String ISSUES_SAVED_FILTER = "/sr/jira.issueviews:searchrequest-xml/";
    private String serverUrl = null;
    private List<JIRAQueryFragment> queryFragments = null;
    private String sortBy = null;
    private String sortOrder = null;
    private int start = NOT_INITIALIZED;
    private int max = NOT_INITIALIZED;
    private String userName = null;
    private String password;

    public static class Builder {
        private List<JIRAQueryFragment> queryFragments = null;
        private String sortBy = null;
        private String sortOrder = null;
        private int start = -1;
        private int max = -1;
        private String userName = null;
        private String serverUrl;
        private String password;


        public Builder serverUrl(String server) {
            this.serverUrl = server;
            return this;
        }

        public Builder queryFragments(List<JIRAQueryFragment> fragmentList) {
            this.queryFragments = fragmentList;
            return this;
        }

        public Builder sortBy(String sort) {
            this.sortBy = sort;
            return this;
        }

        public Builder sortOrder(String sortOdr) {
            this.sortOrder = sortOdr;
            return this;
        }

        public Builder start(int strt) {
            this.start = strt;
            return this;
        }

        public Builder max(int mx) {
            this.max = mx;
            return this;
        }

        public Builder userName(String username) {
            this.userName = username;
            return this;
        }

        public Builder password(String passw) {
            this.password = passw;
            return this;
        }

        public JiraQueryUrl build() {
            return new JiraQueryUrl(this);
        }

        public Builder queryFragment(JIRAQueryFragment fragment) {
            if (queryFragments == null) {
                queryFragments = new ArrayList<JIRAQueryFragment>();
            }
            queryFragments.add(fragment);
            return this;
        }
    }

    public String buildRssSearchUrl() {
        StringBuffer sb = new StringBuffer();
        sb.append(buildQueryFragment());
        sb.append("&").append(buildOptions());

        if (serverUrl != null) {
               sb.insert(0, ISSUE_RSS);
               sb.insert(0, serverUrl);
        }

        return sb.toString();
    }

    public String buildIssueNavigatorUrl() {
       StringBuffer sb = new StringBuffer();
        sb.append(buildQueryFragment());
        sb.append("&").append(buildOptions());

        if (serverUrl != null) {
               sb.insert(0, ISSUE_NAVIGATOR);
               sb.insert(0, serverUrl);
        }

        return sb.toString();
    }


    public String buildSavedFilterUrl() {
        JIRAQueryFragment query = queryFragments != null && queryFragments.size() > 0 ? queryFragments.get(0) : null;

        StringBuffer sb = new StringBuffer();
         if (serverUrl != null) {
             sb.append(serverUrl);
             sb.append(ISSUES_SAVED_FILTER);
        }

        if (query != null) {
			sb.append(query.getQueryStringFragment())
			  .append("/SearchRequest-")
			  .append(query.getQueryStringFragment())
			  .append(".xml");
		}
        sb.append("?");
        sb.append(buildOptions());

        return sb.toString();
    }
    private String buildQueryFragment() {
        StringBuilder sb = new StringBuilder();

        List<JIRAQueryFragment> fragmentsWithoutAnys = new ArrayList<JIRAQueryFragment>();
        for (JIRAQueryFragment jiraQueryFragment : queryFragments) {
            if (jiraQueryFragment.getId() != CacheConstants.ANY_ID) {
                fragmentsWithoutAnys.add(jiraQueryFragment);
            }
        }

        for (JIRAQueryFragment fragment : fragmentsWithoutAnys) {
            if (fragment.getQueryStringFragment() != null) {
                sb.append("&");
                sb.append(fragment.getQueryStringFragment());
            }
        }


        return sb.toString();
    }

    private String buildOptions() {
        StringBuffer sb = new StringBuffer();

        if (sortBy != null) {
            sb.append("&sorter/field=").append(sortBy);
        }
        if (sortOrder != null) {
            sb.append("&sorter/order=").append(sortOrder);
        }
        if (start != NOT_INITIALIZED) {
            sb.append("&pager/start=").append(start);
        }

        if (max != NOT_INITIALIZED) {
            sb.append("&tempMax=").append(max);
        }

        if (userName != null) {
            sb.append(appendAuthentication(false, userName, password));
        }

        if (sb.length() > 0) {
            sb.deleteCharAt(0); //remove &
        }
        return sb.toString();
    }


    private static String appendAuthentication(boolean firstItem, String userName, String password) {
//        if (userName != null) {
//            //return (firstItem ? "?" : "&") + "os_username=" + password + ;
//        }
        return "";
    }

    private JiraQueryUrl(Builder builder) {
        this.queryFragments = builder.queryFragments;
        this.sortBy = builder.sortBy;
        this.sortOrder = builder.sortOrder;
        this.start = builder.start;
        this.max = builder.max;
        this.userName = builder.userName;
        this.serverUrl = builder.serverUrl;
        this.password = builder.password;
    }
}
