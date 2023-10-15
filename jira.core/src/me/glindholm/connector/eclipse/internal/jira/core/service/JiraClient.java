/*******************************************************************************
 * Copyright (c) 2004, 2009 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *     Jacek Jaroczynski - fixes for bug 233757
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core.service;

import java.io.InputStream;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraAction;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraAttachment;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssue;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueField;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueType;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraNamedFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraPriority;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProjectRole;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraResolution;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraSecurityLevel;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraServerInfo;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraStatus;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.IssueCollector;
import me.glindholm.connector.eclipse.internal.jira.core.service.rest.JiraRestClientAdapter;
import me.glindholm.jira.rest.client.api.RestClientException;
import me.glindholm.jira.rest.client.api.domain.BasicUser;
import me.glindholm.jira.rest.client.api.domain.Field;
import me.glindholm.jira.rest.client.api.domain.Session;
import me.glindholm.jira.rest.client.api.domain.User;

/**
 * JIRA server implementation that caches information that is unlikely to change during the session.
 * This server uses a {@link JiraClientData} object to persist the repository configuration. It has
 * life-cycle methods to allow data in the cache to be reloaded. This interface exposes the full set
 * of services available from a Jira installation. It provides a unified interface for the SOAP and
 * Web/RSS services available.
 *
 * @author Brock Janiczak
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 * @author Jacek Jaroczynski
 */
public class JiraClient {

    private static final String URL_REGEXP_HTTP = "http.*"; //$NON-NLS-1$

    private static final String URL_REGEXP_HTTPS = "https.*"; //$NON-NLS-1$

    private static final String PROXY_TYPE_HTTP = "HTTP"; //$NON-NLS-1$

    private static final String PROXY_TYPE_HTTPS = "HTTPS"; //$NON-NLS-1$

    /**
     * Leave the assignee field as is (this does not apply when performing an assign to action)
     */
    public final static int ASSIGNEE_CURRENT = 2;

    /**
     * Assign to the default user
     */
    public final static int ASSIGNEE_DEFAULT = 1;

    /**
     * Assign to nobody
     */
    public final static int ASSIGNEE_NONE = 3;

    /**
     * Assign to the current user
     */
    public final static int ASSIGNEE_SELF = 5;

    /**
     * Assign to a specific user. To get the name of the assignee call {@link #getAssignee()}
     */
    public final static int ASSIGNEE_USER = 4;

    public final static String DEFAULT_CHARSET = "UTF-8"; //$NON-NLS-1$

    // private boolean attemptedToDetermineCharacterEncoding;

    private final String baseUrl;

    private final JiraClientCache cache;

    // private final JiraRssClient rssClient;

    // private final JiraWebClient webClient;

    private final AbstractWebLocation location;

    private JiraLocalConfiguration localConfiguration;

    // private final JiraWebSession webSession;

    private JiraRestClientAdapter restClient = null;

    public JiraClient(final AbstractWebLocation location, final JiraLocalConfiguration configuration, final JiraRestClientAdapter restClient) {
        Assert.isNotNull(location);
        Assert.isNotNull(configuration);
        baseUrl = location.getUrl();
        this.location = location;
        localConfiguration = configuration;

        cache = new JiraClientCache(this);
        // this.webSession = new JiraWebSession(this);
        // this.webClient = new JiraWebClient(this, webSession);
        // this.rssClient = new JiraRssClient(this, webSession);
        this.restClient = restClient;

    }

    public JiraClient(final AbstractWebLocation location, final JiraLocalConfiguration configuration) {
        this(location, configuration, null);
        // lazy initialization of REST client so creating JIRA client does not trigger
        // SecureStorage
        // restClient = createRestClient(location, cache);
    }

    /**
     * Only for Tests purposes
     *
     * @param location
     */
    public JiraClient(final AbstractWebLocation location) {
        this(location, new JiraLocalConfiguration());
    }

    private JiraRestClientAdapter getRestClient() {
        if (restClient == null) {
            restClient = createRestClient(location, cache);
            try {
                final User currentUser = restClient.getCurrentUser();
                final int i = 0;
            } catch (final JiraException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return restClient;
    }

    private JiraRestClientAdapter createRestClient(final AbstractWebLocation location, final JiraClientCache cache) {
        Proxy proxy = null;
        final String baseUrl = location.getUrl();
        if (baseUrl.matches(URL_REGEXP_HTTPS)) {
            proxy = location.getProxyForHost(baseUrl, PROXY_TYPE_HTTPS);
        } else if (baseUrl.matches(URL_REGEXP_HTTP)) {
            proxy = location.getProxyForHost(baseUrl, PROXY_TYPE_HTTP);
        }

        final AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);

        String username = ""; //$NON-NLS-1$
        String password = null;

        if (credentials != null) {
            username = credentials.getUserName();
            password = credentials.getPassword();
        }

        return new JiraRestClientAdapter(baseUrl, username, password, proxy, cache, localConfiguration.getFollowRedirects());

    }

    // public void addCommentToIssue(String issueKey, Comment comment,
    // IProgressMonitor monitor) throws JiraException {
    // JiraCorePlugin.getMonitoring().logJob("addCommentToIssue", null);
    // //$NON-NLS-1$
    // soapClient.addComment(issueKey, comment, monitor);
    // }

    public void addCommentToIssue(final String issueKey, final String comment, final IProgressMonitor monitor) throws JiraException {

        try {
            getRestClient().addComment(issueKey, comment);
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }

        // Comment cmnt = new Comment();
        // cmnt.setComment(comment);
        // addCommentToIssue(issueKey, cmnt, monitor);
    }

    public void advanceIssueWorkflow(final JiraIssue issue, final String actionKey, final String comment, final IProgressMonitor monitor) throws JiraException {
        try {
            final List<JiraIssueField> fields = getActionFields(issue.getKey(), actionKey, monitor);

            getRestClient().transitionIssue(issue, actionKey, comment, fields);

        } catch (final RestClientException e) {
            throw new JiraException(e);
        }

        // soapClient.progressWorkflowAction(issue, actionKey, fields, monitor);
        //
        // if (!StringUtils.isEmpty(comment)) {
        // addCommentToIssue(issue.getKey(), comment, monitor);
        // }
    }

    public void assignIssueTo(final JiraIssue issue, final String user, final String comment, final IProgressMonitor monitor) throws JiraException {
        /*
         * PLE-1188 soapClient.assignIssueTo(issue.getKey(), getAssigneeParam(issue, assigneeType, user),
         * monitor);
         */

        // webClient.assignIssueTo(issue, assigneeType, user, comment, monitor);

        try {
            getRestClient().assignIssue(issue.getKey(), user, comment);
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }

        if (!StringUtils.isEmpty(comment)) {
            addCommentToIssue(issue.getKey(), comment, monitor);
        }

        // TODO rest: https://studio.atlassian.com/browse/JRJC-85 (single operation)
    }

    public void addAttachment(final JiraIssue jiraIssue, final String comment, final String filename, final byte[] content, final IProgressMonitor monitor)
            throws JiraException {
        if (content.length == 0) {
            throw new JiraException("Cannot attach empty file");
        }

        try {
            getRestClient().addAttachment(jiraIssue.getKey(), content, filename);
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }

        // String[] encodedContents = new String[] { new String(new
        // Base64().encode(content)) };
        // String[] names = new String[] { filename };
        //
        // try {
        // soapClient.addAttachmentsBase64EncodedToIssue(issue.getKey(), names,
        // encodedContents, monitor);
        // } catch (Throwable e) {
        // if (e.toString().contains("java.lang.OutOfMemoryError")) { //$NON-NLS-1$
        // StatusHandler.log(new Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN,
        // e.getMessage()));
        // throw new JiraException(Messages.JiraClient_attachment_too_large, e);
        // }
        // throw new JiraException(e.toString(), e);
        // }

        if (!StringUtils.isEmpty(comment)) {
            addCommentToIssue(jiraIssue.getKey(), comment, monitor);
        }
    }

    /**
     * Creates an issue with the details specified in <code>issue</code>. The following fields are
     * mandatory:
     * <ul>
     * <li>Project</li>
     * <li>Issue Type</li>
     * <li>Summary</li>
     * </ul>
     * The following fields are optional:
     * <ul>
     * <li>Priority</li>
     * <li>Components</li>
     * <li>Affects Version</li>
     * <li>Fix Version</li>
     * <li>Environment</li>
     * <li>Description</li>
     * <li>Assignee (If sufficient permissions)</li>
     * <li>Reporter (If sufficient permissions)</li>
     * </ul>
     * All other fields other fields are not settable at this time
     *
     * @param issue Prototype issue used to create the new issue
     * @return A fully populated
     *         {@link me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssue} containing the
     *         details of the new issue
     */
    public JiraIssue createIssue(final JiraIssue issue, final IProgressMonitor monitor) throws JiraException {
        if (issue.getProject().getKey() == null) {
            final JiraProject project = cache.getProjectById(issue.getProject().getId(), monitor);
            if (project != null) {
                issue.getProject().setKey(project.getKey());
            }
        }

        try {

            final String issueKey = getRestClient().createIssue(issue);

            // String issueKey = soapClient.createIssue(issue, monitor);
            // String issueKey = webClient.createIssue(issue, monitor);
            return getIssueByKey(issueKey, monitor);

        } catch (final RestClientException e) {
            throw new JiraException(e);
        }
    }

    /**
     * See {@link #createIssue(JiraIssue)} for mandatory attributes of <code>issue</code>. Additionally
     * the <code>parentIssueId</code> must be set.
     */
    // public JiraIssue createSubTask(JiraIssue issue, IProgressMonitor monitor)
    // throws JiraException {
    // JiraCorePlugin.getMonitoring().logJob("createSubTask", null); //$NON-NLS-1$
    // String issueKey = webClient.createSubTask(issue, monitor);
    // return getIssueByKey(issueKey, monitor);
    // }

    // only for tests purposes
    public void deleteIssue(final JiraIssue issue, final IProgressMonitor monitor) throws JiraException {
        // soapClient.deleteIssue(issue.getKey(), monitor);
        restClient.deleteIssue(issue.getKey(), monitor);

        // TODO rest: https://studio.atlassian.com/browse/JRJC-86
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof JiraClient) {
            return getBaseUrl().equals(((JiraClient) obj).getBaseUrl());
        }
        return false;
    }

    // public void executeNamedFilter(NamedFilter filter, IssueCollector collector,
    // IProgressMonitor monitor)
    // throws JiraException {
    // JiraCorePlugin.getMonitoring().logJob("executeNamedFilter", null);
    // //$NON-NLS-1$
    // rssClient.executeNamedFilter(filter, collector, monitor);
    // }

    public void findIssues(final FilterDefinition filterDefinition, final IssueCollector collector, final IProgressMonitor monitor) throws JiraException {
        final FilterDefinitionConverter filterConverter = new FilterDefinitionConverter(DEFAULT_CHARSET, getLocalConfiguration().getDateTimeFormat());

        final String jql = filterConverter.getJqlString(filterDefinition);

        findIssues(jql, collector, monitor);

        // rssClient.findIssues(filterDefinition, collector, monitor);
    }

    private void findIssues(final String jql, final IssueCollector collector, final IProgressMonitor monitor) throws JiraException {
        try {
            final List<JiraIssue> issues = getRestClient().getIssues(jql, localConfiguration.getMaxSearchResults(), monitor);

            if (!collector.isCancelled()) {
                collector.start();

                for (final JiraIssue issue : issues) {
                    collector.collectIssue(issue);
                }

                collector.done();
            }
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }
    }

    /**
     * Returns fields for given action id
     *
     * @param issueKey Unique key of the issue to find
     * @param actionId Unique id for action to get fields for
     * @return array of field ids for given actionId
     */
    public List<JiraIssueField> getActionFields(final String issueKey, final String actionId, final IProgressMonitor monitor) throws JiraException {
        final List<JiraAction> actions = getAvailableActions(issueKey, monitor);

        for (final JiraAction action : actions) {
            if (action.getId().equals(actionId)) {
                return action.getFields();
            }
        }

        return Collections.emptyList();

        // return soapClient.getActionFields(issueKey, actionId, monitor);
    }

    /**
     * Returns available operations for <code>issueKey</code>
     *
     * @param issueKey Unique key of the issue to find
     * @return corresponding array of <code>RepositoryOperation</code> objects or <code>null</code>.
     */
    public List<JiraAction> getAvailableActions(final String issueKey, final IProgressMonitor monitor) throws JiraException {
        try {
            return getRestClient().getTransitions(issueKey);
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }

        // return soapClient.getAvailableActions(issueKey, monitor);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public JiraClientCache getCache() {
        return cache;
    }

    public String getCharacterEncoding(final IProgressMonitor monitor) throws JiraException {
        if (localConfiguration.getCharacterEncoding() == null) {
            // String serverEncoding =
            // getCache().getServerInfo(monitor).getCharacterEncoding();
            // if (serverEncoding != null) {
            // return serverEncoding;
            // } else if (!attemptedToDetermineCharacterEncoding) {
            // getCache().refreshServerInfo(monitor);
            // serverEncoding = getCache().getServerInfo(monitor).getCharacterEncoding();
            // if (serverEncoding != null) {
            // return serverEncoding;
            // }
            // }
            // fallback
            return DEFAULT_CHARSET;
        }
        return localConfiguration.getCharacterEncoding();
    }

    // public Component[] getComponents(String projectKey, IProgressMonitor monitor)
    // throws JiraException {
    // JiraCorePlugin.getMonitoring().logJob("getComponents", null); //$NON-NLS-1$
    //
    // return restClient.getComponents(projectKey);
    //
    //// return soapClient.getComponents(projectKey, monitor);
    // }

    public synchronized JiraLocalConfiguration getLocalConfiguration() {
        return localConfiguration;
    }

    /**
     * Returns editable attributes for <code>issueKey</code>
     *
     * @param issueKey Unique key of the issue to find
     * @return corresponding array of <code>RepositoryTaskAttribute</code> objects or <code>null</code>.
     */
    // public IssueField[] getEditableAttributes(final String issueKey,
    // IProgressMonitor monitor) throws JiraException {
    // JiraCorePlugin.getMonitoring().logJob("getEditableAttributes", null);
    // //$NON-NLS-1$
    // return soapClient.getEditableAttributes(issueKey, monitor);
    // }

    /**
     * Retrieve an issue using its unique key
     *
     * @param issueKey Unique key of the issue to find
     * @return Matching issue or <code>null</code> if no matching issue could be found
     */
    public JiraIssue getIssueByKey(final String issueKey, final IProgressMonitor monitor) throws JiraException {
        try {
            return getRestClient().getIssueByKeyOrId(issueKey, monitor);
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }

        // SingleIssueCollector collector = new SingleIssueCollector();
        // rssClient.getIssueByKey(issueKey, collector, monitor);
        // if (collector.getIssue() != null && collector.getIssue().getProject() ==
        // null) {
        // throw new JiraException("Repository returned an unknown project for issue '"
        // //$NON-NLS-1$
        // + collector.getIssue().getKey() + "'"); //$NON-NLS-1$
        // }
        // return collector.getIssue();
    }

    public JiraIssue getIssueById(final String issueId, final IProgressMonitor monitor) throws JiraException {
        try {
            return getRestClient().getIssueByKeyOrId(issueId, monitor);
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }
    }

    public JiraIssueType[] getIssueTypes(final IProgressMonitor monitor) throws JiraException {
        try {
            return getRestClient().getIssueTypes();
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }

        // return soapClient.getIssueTypes(monitor);
    }

    // public IssueType[] getIssueTypes(String projectKey, IProgressMonitor monitor)
    // throws JiraException {
    // JiraCorePlugin.getMonitoring().logJob("getIssueTypesForProject", null);
    // //$NON-NLS-1$
    //
    // return restClient.getIssueTypes(projectKey);
    //
    //// return soapClient.getIssueTypes(projectKey, monitor);
    // }

    /**
     * Returns the corresponding key for <code>issueId</code>.
     *
     * @param issueId unique id of the issue
     * @return corresponding key or <code>null</code> if the id was not found
     */
    // public String getKeyFromId(final String issueId, IProgressMonitor monitor)
    // throws JiraException {
    // JiraCorePlugin.getMonitoring().logJob("getKeyFromId", null); //$NON-NLS-1$
    // // it is used only in tests
    // return soapClient.getKeyFromId(issueId, monitor);
    // }

    public AbstractWebLocation getLocation() {
        return location;
    }

    /**
     * Return field metadata
     *
     * @param monitor
     * @return
     * @throws JiraException
     */
    public List<Field> getMetadata(final IProgressMonitor monitor) throws JiraException {
        try {
            return getRestClient().getMetadata();
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }

    }

    /**
     * Retrieves all filters that are stored and run on the server. The client will never be aware of
     * the definition for the filter, only its name and description
     *
     * @return List of all filters taht are stored and executed on the server
     */
    public JiraNamedFilter[] getNamedFilters(final IProgressMonitor monitor) throws JiraException {
        try {
            return getRestClient().getFavouriteFilters();
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }

        // return soapClient.getNamedFilters(monitor);
    }

    public JiraPriority[] getPriorities(final IProgressMonitor monitor) throws JiraException {
        try {
            return getRestClient().getPriorities();
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }

        // return soapClient.getPriorities(monitor);
    }

    public JiraProject[] getProjects(final IProgressMonitor monitor) throws JiraException {
        try {
            return getRestClient().getProjects();
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }

        // return soapClient.getProjects(monitor);
    }

    public JiraResolution[] getResolutions(final IProgressMonitor monitor) throws JiraException {
        try {
            return getRestClient().getResolutions();
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }

        // return soapClient.getResolutions(monitor);
    }

    public Session getSession(final IProgressMonitor monitor) throws JiraException {

        // TODO add some aspect logging here
        try {
            return getRestClient().getSession();
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }
    }

    public JiraServerInfo getServerInfo(final IProgressMonitor monitor) throws JiraException {
        // get server information through SOAP
        // ServerInfo serverInfo = soapClient.getServerInfo(monitor);
        try {
            final JiraServerInfo serverInfo = getRestClient().getServerInfo();

            // get character encoding through web
            // WebServerInfo webServerInfo = webClient.getWebServerInfo(monitor);
            // serverInfo.setCharacterEncoding(webServerInfo.getCharacterEncoding());
            serverInfo.setCharacterEncoding(DEFAULT_CHARSET);
            // serverInfo.setWebBaseUrl(webServerInfo.getBaseUrl());

            // serverInfo.getStatistics().getStatus().addAll(webServerInfo.getStatistics().getStatus());

            return serverInfo;
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }
    }

    public JiraStatus[] getStatuses(final IProgressMonitor monitor) throws JiraException {
        try {
            return getRestClient().getStatuses();
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }

        // return soapClient.getStatuses(monitor);
    }

    // public IssueType[] getSubTaskIssueTypes(IProgressMonitor monitor) throws
    // JiraException {
    // JiraCorePlugin.getMonitoring().logJob("getSubTaskIssueTypes", null);
    // //$NON-NLS-1$
    // return soapClient.getSubTaskIssueTypes(monitor);
    // }

    // public IssueType[] getSubTaskIssueTypes(String projectId, IProgressMonitor
    // monitor) throws JiraException {
    // JiraCorePlugin.getMonitoring().logJob("getSubTaskIssueTypesForProject",
    // null); //$NON-NLS-1$
    // return soapClient.getSubTaskIssueTypes(projectId, monitor);
    // }

    public void getProjectDetails(final JiraProject project) throws JiraException {
        try {
            getRestClient().getProjectDetails(project);
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }
    }

    public String getUserName() {
        final AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
        return credentials != null ? credentials.getUserName() : ""; //$NON-NLS-1$
    }

    /**
     * Returns a sorted list of versions for the specified project in descended order.
     *
     * @param key the project key
     */
    // public Version[] getVersions(String key, IProgressMonitor monitor) throws
    // JiraException {
    // JiraCorePlugin.getMonitoring().logJob("getVersions", null); //$NON-NLS-1$
    //// List<Version> versions = Arrays.asList(soapClient.getVersions(key,
    // monitor));
    // List<Version> versions = Arrays.asList(restClient.getVersions(key));
    // Collections.sort(versions, new Comparator<Version>() {
    // public int compare(Version o1, Version o2) {
    // return o1.getSequence() > o2.getSequence() ? -1 : (o1.getSequence() ==
    // o2.getSequence() ? 0 : 1);
    // }
    // });
    // return versions.toArray(new Version[0]);
    // }

    @Override
    public int hashCode() {
        return getBaseUrl().hashCode();
    }

    /**
     * Force a login to the remote repository. There is no need to call this method as all services
     * should automatically login when the session is about to expire. If you need to check if the
     * credentials are valid, call
     * {@link me.glindholm.connector.eclipse.internal.jira.core.JiraClientManager#testConnection(String, String, String)}
     */
    // public void login(IProgressMonitor monitor) throws JiraException {
    // JiraCorePlugin.getMonitoring().logJob("login", null); //$NON-NLS-1$
    // soapClient.login(monitor);
    // }

    /**
     * Force the current session to be closed. This method should only be called during application
     * shutdown and then only out of courtesy to the server. Jira will automatically expire sessions
     * after a set amount of time.
     */
    public void logout(final IProgressMonitor monitor) throws JiraException {
        // JiraCorePlugin.getMonitoring().logJob("logout", null); //$NON-NLS-1$
        // soapClient.logout(monitor);

        // Nothing to do here as our REST client is stateless
    }

    // public void quickSearch(String searchString, IssueCollector collector,
    // IProgressMonitor monitor)
    // throws JiraException {
    // JiraCorePlugin.getMonitoring().logJob("quickSearch", null); //$NON-NLS-1$
    // rssClient.quickSearch(searchString, collector, monitor);
    //
    // }

    public InputStream getAttachment(final JiraIssue jiraIssue, final JiraAttachment attachment, final IProgressMonitor monitor) throws JiraException {
        try {
            return getRestClient().getAttachment(attachment.getContent());
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }

        // webClient.retrieveFile(jiraIssue, attachment, out, monitor);
    }

    /**
     * @param query     Query to be executed
     * @param collector Reciever for the matching issues
     */
    public void search(final JiraFilter query, final IssueCollector collector, final IProgressMonitor monitor) throws JiraException {
        // if (query instanceof TextFilter) {
        // quickSearch(((TextFilter) query).getKeywords(), collector, monitor);
        // }

        if (query instanceof FilterDefinition) {
            findIssues((FilterDefinition) query, collector, monitor);
        } else if (query instanceof JiraNamedFilter) {
            findIssues(((JiraNamedFilter) query).getJql(), collector, monitor);
            // executeNamedFilter((NamedFilter) query, collector, monitor);
        } else {
            throw new IllegalArgumentException("Unknown query type: " + query.getClass()); //$NON-NLS-1$
        }
    }

    @Override
    public String toString() {
        return getBaseUrl();
    }

    public void updateIssue(final JiraIssue issue, final String comment, final boolean updateEstimate, final IProgressMonitor monitor) throws JiraException {
        // soapClient.updateIssue(issue, monitor);

        try {
            getRestClient().updateIssue(issue, updateEstimate);
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }

        if (!StringUtils.isEmpty(comment)) {
            addCommentToIssue(issue.getKey(), comment, monitor);
        }
    }

    // public JiraWorkLog[] getWorklogs(String issueKey, IProgressMonitor monitor)
    // throws JiraException {
    // JiraCorePlugin.getMonitoring().logJob("getWorklogs", null); //$NON-NLS-1$
    // return soapClient.getWorkLogs(issueKey, monitor);
    // }

    public JiraSecurityLevel[] getAvailableSecurityLevels(final String projectKey, final IProgressMonitor monitor) throws JiraException {
        return getRestClient().getSecurityLevels(projectKey);
    }

    public void addWorkLog(final String issueKey, final JiraWorkLog log, final IProgressMonitor monitor) throws JiraException {
        try {
            getRestClient().addWorklog(issueKey, log);
        } catch (final RestClientException e) {
            throw new JiraException(e);
        }

        // return soapClient.addWorkLog(issueKey, log, monitor);
    }

    public JiraProjectRole[] getProjectRoles(final IProgressMonitor monitor) throws JiraException {
        // return soapClient.getProjectRoles(monitor);

        // not used
        // restClient.getProjectRolesRestClient().getRoles(projectUri, progressMonitor)
        return new JiraProjectRole[0];

        // TODO rest https://studio.atlassian.com/browse/JRJC-108
    }

    // public JiraConfiguration getConfiguration(IProgressMonitor monitor) throws
    // JiraException {
    // JiraCorePlugin.getMonitoring().logJob("getConfiguration", null);
    // //$NON-NLS-1$
    // return soapClient.getConfiguration(monitor);
    // }

    public synchronized void setLocalConfiguration(final JiraLocalConfiguration configuration) {
        Assert.isNotNull(configuration);
        localConfiguration = configuration;
    }

    public boolean isCompressionEnabled() {
        return getLocalConfiguration().isCompressionEnabled();
    }

    public synchronized void purgeSession() {
        // webSession.purgeSession();
        // soapClient.purgeSession();
        restClient = createRestClient(location, cache);
    }

    public String getAssigneeParam(final JiraIssue issue, final int assigneeType, final String user) {
        return switch (assigneeType) {
        case JiraClient.ASSIGNEE_CURRENT -> issue.getAssignee().getDisplayName();
        case JiraClient.ASSIGNEE_DEFAULT -> "-1"; //$NON-NLS-1$
        case JiraClient.ASSIGNEE_NONE -> ""; //$NON-NLS-1$
        case JiraClient.ASSIGNEE_SELF -> getUserName();
        case JiraClient.ASSIGNEE_USER -> user;
        default -> user;
        };
    }

    public SimpleDateFormat getDateTimeFormat() {
        return JiraRestClientAdapter.getOffsetDateTimeFormat();
    }

    public SimpleDateFormat getDateFormat() {
        return JiraRestClientAdapter.getDateFormat();
    }

    public List<User> getProjectAssignables(final String issueKey) throws JiraException {
        return getRestClient().getProjectAssignables(issueKey);
    }

    public BasicUser getCurrentUser() throws JiraException {
        return getRestClient().getCurrentUser();
    }
}
