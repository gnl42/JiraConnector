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

/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 13/03/2004
 * Time: 23:19:19
 */
package com.atlassian.connector.commons.jira.rss;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.JIRAIssueBean;
import com.atlassian.connector.commons.jira.JIRASessionPartTwo;
import com.atlassian.connector.commons.jira.beans.JIRASavedFilter;
import com.atlassian.connector.commons.jira.beans.JiraFilter;
import com.atlassian.connector.commons.jira.cache.CachedIconLoader;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.remoteapi.CaptchaRequiredException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.remoteapi.ServiceUnavailableException;
import com.atlassian.theplugin.commons.remoteapi.jira.JiraCaptchaRequiredException;
import com.atlassian.theplugin.commons.remoteapi.jira.JiraServiceUnavailableException;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.util.StringUtil;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JIRARssClient extends AbstractHttpSession implements JIRASessionPartTwo {

    private final ConnectionCfg httpConnectionCfg;
    private boolean login = false;
    private boolean jira4x = true;

    public JIRARssClient(final ConnectionCfg httpConnectionCfg, final HttpSessionCallback callback)
            throws RemoteApiMalformedUrlException {
        super(httpConnectionCfg, callback);
        this.httpConnectionCfg = httpConnectionCfg;
    }

    @Override
    protected void adjustHttpHeader(HttpMethod method) {
        if (httpConnectionCfg instanceof ServerData && ((ServerData) httpConnectionCfg).isUseBasicUser()) {
            method.addRequestHeader(new Header("Authorization", getAuthBasicHeaderValue()));
        }
    }

    @Override
    protected void preprocessResult(Document doc) throws JDOMException, RemoteApiSessionExpiredException {
    }

    @Override
    protected void preprocessMethodResult(HttpMethod method)
            throws RemoteApiException, ServiceUnavailableException {
        try {
            if (login && method != null && method.getStatusLine() != null) {
                if (method.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                    jira4x = false;
                } else if (method.getResponseHeader("Content-Type") != null
                        && method.getResponseHeader("Content-Type").getValue().startsWith("application/json")) {
                    // we're talking to JIRA 4.x
                    String json = "";
                    if (method instanceof PostMethod) {
                        json = new String(((PostMethod) method).getResponseBody(1024));
                    } else {
                        json = method.getResponseBodyAsString();
                    }
                    if (json != null && json.contains("\"captchaFailure\":true")) {
                        throw new CaptchaRequiredException(null);
                    }
                    if (json != null && json.contains("\"loginFailedByPermissions\":true")) {
                        throw new JiraServiceUnavailableException("You don't have permission to login");
                    }
                }
            }
        } catch (IOException e) {
            throw new RemoteApiException("Cannot parse method result.", e);

        }
    }

    private String getAuthBasicHeaderValue() {
        UserCfg basicUser = ((ServerData) httpConnectionCfg).getBasicUser();

        if (basicUser != null && basicUser.getUsername() != null && basicUser.getPassword() != null) {
            return "Basic " + StringUtil.encode(basicUser.getUsername() + ":" + basicUser.getPassword());
        }

        return "";
    }

    private Locale getLocale(Element channel) {
        Locale locale = Locale.US;
        Element language = channel.getChild("language");
        if (language != null) {
            String[] parsedLocale = language.getText().split("-");
            if (parsedLocale != null && parsedLocale.length > 1) {
                locale = new Locale(parsedLocale[0], parsedLocale[1]);
            }
        }
        return locale;
    }

    public List<JIRAIssue> getIssues(JiraFilter filter, String sortBy, String sortOrder, int start, int max) throws JIRAException {

//    public List<JIRAIssue> getIssues(String queryString, String sortBy, String sortOrder, int start, int max)
//            throws JIRAException {

        StringBuilder url =
                new StringBuilder(getBaseUrl() + "/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?");

        url.append(filter.getOldStyleQueryString());

        url.append("&sorter/field=").append(sortBy);
        url.append("&sorter/order=").append(sortOrder);
        url.append("&pager/start=").append(start);
        url.append("&tempMax=").append(max);
//        url.append(appendAuthentication(false));

        try {
            Document doc = retrieveGetResponse(url.toString());
            Element root = doc.getRootElement();
            Element channel = root.getChild("channel");

            if (channel != null && !channel.getChildren("item").isEmpty()) {
                return makeIssues(channel.getChildren("item"), getLocale(channel));
            }
            return Collections.emptyList();
        } catch (AuthenticationException e) {
            throw new JIRAException("Authentication error", e);
        } catch (IOException e) {
            throw new JIRAException("Connection error: " + e.getMessage(), e);
        } catch (JDOMException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (RemoteApiSessionExpiredException e) {
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAIssue> getIssues(String jql, String sort, String sortOrder, int start, int size) throws JIRAException {
        throw new JIRAException("Not implemented");
    }

    //    public List<JIRAIssue> getIssues(List<JIRAQueryFragment> fragments, String sortBy,
//                                     String sortOrder, int start, int max) throws JIRAException {
//
//        StringBuilder query = new StringBuilder();
//
//        List<JIRAQueryFragment> fragmentsWithoutAnys = new ArrayList<JIRAQueryFragment>();
//        for (JIRAQueryFragment jiraQueryFragment : fragments) {
//            if (jiraQueryFragment.getId() != CacheConstants.ANY_ID) {
//                fragmentsWithoutAnys.add(jiraQueryFragment);
//            }
//        }
//
//        for (JIRAQueryFragment fragment : fragmentsWithoutAnys) {
//            if (fragment.getQueryStringFragment() != null) {
//                query.append("&").append(fragment.getQueryStringFragment());
//            }
//        }
//
//        return getIssues(query.toString(), sortBy, sortOrder, start, max);
//    }

//    public List<JIRAIssue> getAssignedIssues(String assignee) throws JIRAException {
//        String url = getBaseUrl() + "/sr/jira.issueviews:searchrequest-xml"
//                + "/temp/SearchRequest.xml?resolution=-1&assignee=" + encodeUrl(assignee)
//                + "&sorter/field=updated&sorter/order=DESC&tempMax=100";
////                + appendAuthentication(false);
//
//        try {
//            Document doc = retrieveGetResponse(url);
//            Element root = doc.getRootElement();
//            Element channel = root.getChild("channel");
//            if (channel != null && !channel.getChildren("item").isEmpty()) {
//                return makeIssues(channel.getChildren("item"), getLocale(channel));
//            }
//
//
//            return Collections.emptyList();
//        } catch (IOException e) {
//            throw new JIRAException(e.getMessage(), e);
//        } catch (JDOMException e) {
//            throw new JIRAException(e.getMessage(), e);
//        } catch (RemoteApiSessionExpiredException e) {
//            throw new JIRAException(e.getMessage(), e);
//        }
//    }

    public List<JIRAIssue> getSavedFilterIssues(JIRASavedFilter filter, String sortBy, String sortOrder, int start, int max) throws JIRAException {

//    public List<JIRAIssue> getSavedFilterIssues(JIRAQueryFragment fragment,
//                                                String sortBy,
//                                                String sortOrder,
//                                                int start,
//                                                int max) throws JIRAException {
//
        StringBuilder url = new StringBuilder(getBaseUrl() + "/sr/jira.issueviews:searchrequest-xml/");

        if (filter.getOldStyleQueryString() != null) {
            url.append(filter.getQueryStringFragment())
                    .append("/SearchRequest-")
                    .append(filter.getOldStyleQueryString())
                    .append(".xml");
        }

        url.append("?sorter/field=").append(sortBy);
        url.append("&sorter/order=").append(sortOrder);
        url.append("&pager/start=").append(start);
        url.append("&tempMax=").append(max);

//        url.append(appendAuthentication(false));

        try {
            Document doc = retrieveGetResponse(url.toString());
            Element root = doc.getRootElement();
            Element channel = root.getChild("channel");
            if (channel != null && !channel.getChildren("item").isEmpty()) {
                return makeIssues(channel.getChildren("item"), getLocale(channel));
            }
            return Collections.emptyList();
        } catch (IOException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (RemoteApiSessionExpiredException e) {
            throw new JIRAException(e.getMessage(), e);
        }

    }

    public JIRAIssue getIssue(String issueKey) throws JIRAException {

        StringBuffer url = new StringBuffer(getBaseUrl() + "/si/jira.issueviews:issue-xml/");
        url.append(issueKey).append('/').append(issueKey).append(".xml");

//        url.append(appendAuthentication(true));

        try {
            Document doc = retrieveGetResponse(url.toString());
            Element root = doc.getRootElement();
            Element channel = root.getChild("channel");
            if (channel != null) {
                @SuppressWarnings("unchecked")
                final List<Element> items = channel.getChildren("item");
                if (!items.isEmpty()) {

                    return makeIssues(items, getLocale(channel)).get(0);
                }
            }
            throw new JIRAException("Cannot parse response from JIRA: " + doc.toString());
        } catch (IOException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (RemoteApiSessionExpiredException e) {
            throw new JIRAException(e.getMessage(), e);
        }
    }

    private List<JIRAIssue> makeIssues(@NotNull List<Element> issueElements, Locale locale) {
        List<JIRAIssue> result = new ArrayList<JIRAIssue>(issueElements.size());
        for (final Element issueElement : issueElements) {
            JIRAIssueBean jiraIssue = new JIRAIssueBean(httpConnectionCfg.getUrl(), issueElement, locale);
            CachedIconLoader.loadIcon(jiraIssue.getTypeIconUrl());
            CachedIconLoader.loadIcon(jiraIssue.getPriorityIconUrl());
            CachedIconLoader.loadIcon(jiraIssue.getStatusTypeUrl());
            result.add(jiraIssue);
        }
        return result;
    }

    public void testConnection() throws RemoteApiException {
        try {
            login();
        } catch (JIRAException e) {
            throw new RemoteApiException(e);
        }
    }

    public void login() throws JIRAException, JiraCaptchaRequiredException {
        final String restLogin = "/rest/gadget/1.0/login";
        // JIRA 4.x has additional endpoint for login that tells if CAPTCHA limit was hit
        final String loginAction = "/secure/Dashboard.jspa";

        try {
            login = true;
            Map<String, String> loginParams = new HashMap<String, String>();
            loginParams.put("os_username", httpConnectionCfg.getUsername());
            loginParams.put("os_password", httpConnectionCfg.getPassword());
            loginParams.put("os_destination", "/success");

            if (jira4x) {
                super.retrievePostResponseWithForm(httpConnectionCfg.getUrl() + restLogin, loginParams, false);
            }
            if (!jira4x) {
                super.retrievePostResponseWithForm(httpConnectionCfg.getUrl() + loginAction, loginParams, false);
            }

        } catch (JDOMException e) {
            throw new JIRAException(e.getMessage());
        } catch (CaptchaRequiredException e) {
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            throw new JIRAException(e.getMessage());
        } finally {
            login = false;
        }
    }

    public boolean isLoggedIn(ConnectionCfg server) {
        Cookie[] cookies = callback.getCookiesHeaders(server);
        return cookies != null && cookies.length > 0;
    }
}