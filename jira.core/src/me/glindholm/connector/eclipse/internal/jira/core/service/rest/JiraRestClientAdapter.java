/*******************************************************************************

 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core.service.rest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.osgi.util.NLS;

import com.atlassian.httpclient.api.factory.HttpClientOptions;

import me.glindholm.connector.eclipse.internal.jira.core.JiraAttribute;
import me.glindholm.connector.eclipse.internal.jira.core.JiraCorePlugin;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraAction;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraComponent;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraCustomField;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssue;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueField;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueType;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraNamedFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraPriority;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraResolution;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraSecurityLevel;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraServerInfo;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraStatus;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraVersion;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraAuthenticationException;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClientCache;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraServiceUnavailableException;
import me.glindholm.jira.rest.client.api.IssueRestClient;
import me.glindholm.jira.rest.client.api.JiraRestClient;
import me.glindholm.jira.rest.client.api.RestClientException;
import me.glindholm.jira.rest.client.api.domain.BasicPriority;
import me.glindholm.jira.rest.client.api.domain.BasicProject;
import me.glindholm.jira.rest.client.api.domain.Comment;
import me.glindholm.jira.rest.client.api.domain.Field;
import me.glindholm.jira.rest.client.api.domain.Issue;
import me.glindholm.jira.rest.client.api.domain.Project;
import me.glindholm.jira.rest.client.api.domain.SecurityLevel;
import me.glindholm.jira.rest.client.api.domain.Session;
import me.glindholm.jira.rest.client.api.domain.User;
import me.glindholm.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import me.glindholm.jira.rest.client.api.domain.input.FieldInput;
import me.glindholm.jira.rest.client.api.domain.input.IssueInput;
import me.glindholm.jira.rest.client.api.domain.input.IssueInputBuilder;
import me.glindholm.jira.rest.client.api.domain.input.TransitionInput;
import me.glindholm.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

/**
 * @author Jacek Jaroczynski
 */
public class JiraRestClientAdapter {

    private static final Integer TIMEOUT_CONNECTION_IN_MS = 60 * 1000; // one minute

    private static final Integer TIMEOUT_READ_IN_MS = 10 * 60 * 1000; // ten minutes

    private static final String CONNECT_TIMEOUT_EXCEPTION = "org.apache.commons.httpclient.ConnectTimeoutException"; //$NON-NLS-1$

    private static final String SOCKET_TIMEOUT_EXCEPTION = "java.net.SocketTimeoutException"; //$NON-NLS-1$

    private static final SimpleDateFormat REST_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$

    private static final SimpleDateFormat REST_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ"); //$NON-NLS-1$

    private static final String HTTP_401 = "Client response status: 401"; //$NON-NLS-1$

    private static final String HTTP_403 = "Client response status: 403"; //$NON-NLS-1$

    public static final String HTTP_404 = "Client response status: 404"; //$NON-NLS-1$

    private static final String HTTP_302 = "Client response status: 302"; //$NON-NLS-1$

    private static final String CONNECTION_REFUSED = "java.net.ConnectException: Connection refused: connect"; //$NON-NLS-1$

    private static final String UNKNOWN_HOST_EXCEPTION = "java.net.UnknownHostException:"; //$NON-NLS-1$

    private static final String ILLEGAL_ARGUMENT_EXCEPTION = "java.lang.IllegalArgumentException:"; //$NON-NLS-1$

    private static final String NULL_POINTER_EXCEPTION = "java.lang.NullPointerException"; //$NON-NLS-1$

    private JiraRestClient restClient;

    private final JiraClientCache cache;

    private final String url;

    private final boolean followRedirects;

    public JiraRestClientAdapter(final String url, final JiraClientCache cache, final boolean followRedirects) {
        this.url = url;
        this.cache = cache;
        this.followRedirects = followRedirects;
    }

    public JiraRestClientAdapter(final String url, final String userName, final String password, final Proxy proxy, final JiraClientCache cache,
            final boolean followRedirects) {
        this(url, cache, followRedirects);

        // final TrustManager[] trustAll = new TrustManager[] { new X509TrustManager() {
        // public X509Certificate[] getAcceptedIssuers() {
        // return null;
        // }
        //
        // public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws
        // CertificateException {
        // }
        //
        // public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws
        // CertificateException {
        // }
        // } };

        try {

            // HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
            //
            // HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            // public boolean verify(String s, javax.net.ssl.SSLSession sslSession) {
            // return true;
            // }
            // });
            final HttpClientOptions httpOptions = new HttpClientOptions();
            httpOptions.setUserAgent("Mylyn Tasks Connector: Jira");
            httpOptions.setConnectionTimeout(TIMEOUT_CONNECTION_IN_MS, TimeUnit.MILLISECONDS);
            httpOptions.setSocketTimeout(TIMEOUT_READ_IN_MS, TimeUnit.MILLISECONDS);
            if (userName.isEmpty()) {
                restClient = new AsynchronousJiraRestClientFactory().createWithBearerHttpAuthentication(new URI(url), password, httpOptions);
            } else {
                restClient = new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(new URI(url), userName, password, httpOptions);
            }
        } catch (final URISyntaxException e) {
            // we should never get here as Mylyn constructs URI first and fails if it is
            // incorrect
            StatusHandler.log(new Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, e.getMessage()));
        }
    }

    public void addComment(final String issueKey, final String comment) throws JiraException {

        call(() -> {
            restClient.getIssueClient().addComment(getIssue(issueKey).getCommentsUri(), Comment.valueOf(comment)).claim();

            return null;
        });
    }

    private Issue getIssue(final String issueKeyOrId) throws JiraException {
        return call(() -> {
            try {
                final Issue issue = restClient.getIssueClient()
                        .getIssue(issueKeyOrId, List.of(IssueRestClient.Expandos.SCHEMA, IssueRestClient.Expandos.EDITMETA)).get();
                return issue;
            } catch (InterruptedException | ExecutionException | URISyntaxException e) {
                throw new JiraException(e);
            }
        });
    }

    public void addAttachment(final String issueKey, final byte[] content, final String filename) throws JiraException {
        restClient.getIssueClient().addAttachment(getIssue(issueKey).getAttachmentsUri(), new ByteArrayInputStream(content), filename).claim();
    }

    public InputStream getAttachment(final URI attachmentUri) throws JiraException {
        try {
            return restClient.getIssueClient().getAttachment(attachmentUri).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new JiraException(e);
        }
    }

    public JiraProject[] getProjects() throws JiraException {
        List<BasicProject> allProjects;
        try {
            allProjects = restClient.getProjectClient().getAllProjects().get();
        } catch (InterruptedException | ExecutionException | URISyntaxException e) {
            throw new JiraException(e);
        }

        return JiraRestConverter.convertProjects(allProjects);
    }

    public JiraNamedFilter[] getFavouriteFilters() throws JiraException {

        return call(() -> JiraRestConverter.convertNamedFilters(restClient.getSearchClient().getFavouriteFilters().get()));
    }

    public JiraResolution[] getResolutions() throws JiraException {
        try {
            return JiraRestConverter.convertResolutions(restClient.getMetadataClient().getResolutions().get());
        } catch (InterruptedException | ExecutionException | URISyntaxException e) {
            throw new JiraException(e);
        }
    }

    public JiraPriority[] getPriorities() throws JiraException {
        try {
            return JiraRestConverter.convertPriorities(restClient.getMetadataClient().getPriorities().get());
        } catch (InterruptedException | ExecutionException | URISyntaxException e) {
            throw new JiraException(e);
        }
    }

    public List<Field> getMetadata() throws JiraException {
        try {
            final List<Field> metadata = restClient.getMetadataClient().getFields().get();
            return metadata;
        } catch (InterruptedException | ExecutionException | URISyntaxException e) {
            throw new JiraException(e);
        }
    }

    public JiraIssue getIssueByKeyOrId(final String issueKeyOrId, final IProgressMonitor monitor) throws JiraException {
        return JiraRestConverter.convertIssue(restClient, getIssue(issueKeyOrId), cache, url, monitor);
    }

    public JiraStatus[] getStatuses() throws JiraException {
        try {
            return JiraRestConverter.convertStatuses(restClient.getMetadataClient().getStatuses().get());
        } catch (InterruptedException | ExecutionException | URISyntaxException e) {
            throw new JiraException(e);
        }
    }

    public JiraIssueType[] getIssueTypes() throws JiraException {
        try {
            return JiraRestConverter.convertIssueTypes(restClient.getMetadataClient().getIssueTypes().get());
        } catch (InterruptedException | ExecutionException | URISyntaxException e) {
            throw new JiraException(e);
        }
    }

    public JiraIssueType[] getIssueTypes(final String projectKey) throws JiraException {
        try {
            return JiraRestConverter.convertIssueTypes(restClient.getProjectClient().getProject(projectKey).get().getIssueTypes());
        } catch (InterruptedException | ExecutionException | URISyntaxException e) {
            throw new JiraException(e);
        }
    }

    public List<JiraIssue> getIssues(final String jql, final int maxSearchResult, final IProgressMonitor monitor) throws JiraException {

        return call(() -> {
            try {
                final Set<String> fields = new TreeSet<>();
                fields.add("*all");
                monitor.subTask("Process issues");
                final SubMonitor progress = SubMonitor.convert(monitor, 100);
                progress.split(0);
                progress.setTaskName("Query server");
                final List<Issue> issuesFromServer = restClient.getSearchClient().searchJql(jql, maxSearchResult, 0, fields).get().getIssues();
                progress.split(20).setWorkRemaining(issuesFromServer.size());

                final List<JiraIssue> fullIssues = new ArrayList<>();
                for (final Issue issue : issuesFromServer) {
                    fullIssues.add(JiraRestConverter.convertIssue(restClient, issue, cache, url, progress));
                    progress.split(1);
                }
                progress.done();
                return fullIssues;
            } catch (final Exception e) {
                final IStatus[] msgs = { new org.eclipse.core.runtime.Status(IStatus.INFO, //
                        JiraCorePlugin.ID_PLUGIN, //
                        NLS.bind("using jql [{0}].", //$NON-NLS-1$
                                new Object[] { jql })),
                        new org.eclipse.core.runtime.Status(IStatus.INFO, //
                                JiraCorePlugin.ID_PLUGIN, //
                                NLS.bind("Server: [{0}].", //$NON-NLS-1$
                                        new Object[] { cache.getServerInfo().getVersion() })) };

                final MultiStatus multiMsgs = new MultiStatus(JiraCorePlugin.ID_PLUGIN, IStatus.ERROR, msgs, "Error finding issues", e);
                StatusHandler.log(multiMsgs);

                throw e;
            }
        });

    }

    // public Component[] getComponents(String projectKey) {
    // return JiraRestConverter.convertComponents(restClient.getProjectClient()
    // .getProject(projectKey, new NullProgressMonitor())
    // .getComponents());
    // }
    //
    // public Version[] getVersions(String projectKey) {
    // return JiraRestConverter.convertVersions(restClient.getProjectClient()
    // .getProject(projectKey, new NullProgressMonitor())
    // .getVersions());
    // }

    public void getProjectDetails(final JiraProject project) throws JiraException {

        try {
            final Project projectWithDetails = restClient.getProjectClient().getProject(project.getKey()).get();

            project.setComponents(JiraRestConverter.convertComponents(projectWithDetails.getComponents()));
            project.setVersions(JiraRestConverter.convertVersions(projectWithDetails.getVersions()));
            project.setIssueTypes(JiraRestConverter.convertIssueTypes(projectWithDetails.getIssueTypes()));
        } catch (InterruptedException | ExecutionException | URISyntaxException e) {
            throw new JiraException(e);
        }
    }

    public void addWorklog(final String issueKey, final JiraWorkLog jiraWorklog) throws JiraException {
        final Issue issue = getIssue(issueKey);
        try {
            restClient.getIssueClient().addWorklog(issue.getWorklogUri(), JiraRestConverter.convert(jiraWorklog, issue.getSelf())).claim();
        } catch (final URISyntaxException e) {
            throw new JiraException(e);
        }
    }

    public JiraServerInfo getServerInfo() throws JiraException {
        return call(() -> {
            try {
                return JiraRestConverter.convert(restClient.getMetadataClient().getServerInfo().get());
            } catch (InterruptedException | ExecutionException | URISyntaxException e) {
                throw new JiraException(e);
            }
        });
    }

    public Session getSession() throws JiraException {
        return call(() -> {
            try {
                return restClient.getSessionClient().getCurrentSession().get();
            } catch (RestClientException | InterruptedException | ExecutionException | URISyntaxException e) {
                throw new JiraException(e);
            }
        });
    }

    public User getCurrentUser() throws JiraException {
        return call(() -> {
            try {
                return restClient.getUserClient().getCurrentUser().get();
            } catch (RestClientException | InterruptedException | ExecutionException | URISyntaxException e) {
                throw new JiraException(e);
            }
        });
    }

    public List<JiraAction> getTransitions(final String issueKey) throws JiraException {

        try {
            final URI transitionUri = new URIBuilder(url).appendPath("/rest/api/latest") //
                    .appendPath("issue") //
                    .appendPath(issueKey) //
                    .appendPath("transitions") //$NON-NLS-1$
                    .addParameter("expand", "transitions.fields") //$NON-NLS-1$ //$NON-NLS-2$
                    .build();
            return JiraRestConverter.convertTransitions(restClient.getIssueClient().getTransitions(transitionUri).get());
        } catch (InterruptedException | ExecutionException | URISyntaxException e) {
            throw new JiraException(e);
        }
    }

    public void transitionIssue(final JiraIssue issue, final String transitionKey, final String comment, final List<JiraIssueField> transitionFields)
            throws JiraException {

        final Comment outComment = StringUtils.isEmpty(comment) ? null : Comment.valueOf(comment);

        final List<FieldInput> fields = new ArrayList<>();
        for (final JiraIssueField transitionField : transitionFields) {

            if (transitionField.isRequired()) {

                final String[] values = issue.getFieldValues(transitionField.getName());

                if (values != null && values.length > 0) {
                    if (JiraRestFields.SUMMARY.equals(transitionField.getName()) || JiraRestFields.DESCRIPTION.equals(transitionField.getName())
                            || JiraRestFields.ENVIRONMENT.equals(transitionField.getName())) {

                        fields.add(new FieldInput(transitionField.getName(), values[0]));

                    } else if (JiraRestFields.DUEDATE.equals(transitionField.getName())) {

                        String date = DateTimeFormatter.ofPattern(JiraRestFields.DATE_FORMAT).format(issue.getDue());
                        if (values[0] == null) {
                            date = null;
                        }
                        fields.add(new FieldInput(JiraRestFields.DUEDATE, date));

                    } else if (JiraRestFields.LABELS.equals(transitionField.getName())) {

                        fields.add(new FieldInput(transitionField.getName(), Arrays.asList(values)));

                    } else if (JiraRestFields.RESOLUTION.equals(transitionField.getName()) || JiraRestFields.ISSUETYPE.equals(transitionField.getName())
                            || JiraRestFields.PRIORITY.equals(transitionField.getName()) || JiraRestFields.SECURITY.equals(transitionField.getName())) {

                        fields.add(new FieldInput(transitionField.getId(), ComplexIssueInputFieldValue.with(JiraRestFields.ID, values[0])));

                    } else if (transitionField.getType() != null && "array".equals(transitionField.getType()) //$NON-NLS-1$
                            && !transitionField.getName().startsWith("customfield_")) { //$NON-NLS-1$

                        final List<ComplexIssueInputFieldValue> array = new ArrayList<>();

                        for (final String value : values) {
                            array.add(ComplexIssueInputFieldValue.with(JiraRestFields.ID, value));
                        }

                        fields.add(new FieldInput(transitionField.getId(), array));

                    } else if (transitionField.getName().startsWith("customfield_")) { //$NON-NLS-1$

                        final JiraCustomField customField = issue.getCustomFieldById(transitionField.getId());

                        final FieldInput field = JiraRestConverter.convert(customField);
                        if (field != null) {
                            fields.add(field);
                        }
                    } else {
                        fields.add(new FieldInput(transitionField.getName(), ComplexIssueInputFieldValue.with(JiraRestFields.NAME, values[0])));
                    }
                } else {
                    String name = transitionField.getName();
                    String message = "Field \"{0}\" is required for transition id \"{1}\""; //$NON-NLS-1$

                    if (name.startsWith("customfield_")) { //$NON-NLS-1$
                        final JiraCustomField customField = issue.getCustomFieldById(transitionField.getId());
                        if (customField != null) {
                            name = customField.getName();
                        } else {
                            message += " but is not present on the Issue Edit screen."; //$NON-NLS-1$
                        }
                    }
                    // TODO retrieve transition name
                    throw new JiraException(NLS.bind(message, name, transitionKey));
                }
            }
        }

        // fields.add(new FieldInput("resolution",
        // ComplexIssueInputFieldValue.with("name", "Duplicate")));
        // fields.add(new FieldInput("resolution", new Resolution(null, "Duplicate",
        // null)));

        final TransitionInput transitionInput = new TransitionInput(Integer.parseInt(transitionKey), fields, outComment);

        try {
            restClient.getIssueClient().transition(getIssue(issue.getKey()), transitionInput).claim();
        } catch (URISyntaxException | JiraException e) {
            throw new JiraException(e);
        }

    }

    public void assignIssue(final String issueKey, final String user, final String comment) throws JiraException {
        final Issue issue = getIssue(issueKey);

        final IssueInput fields = IssueInput
                .createWithFields(new FieldInput(JiraRestFields.ASSIGNEE, ComplexIssueInputFieldValue.with(cache.getServerInfo().getAccountTag(), user)));
        try {
            restClient.getIssueClient().updateIssue(issue.getKey(), fields).claim();
        } catch (final URISyntaxException e) {
            throw new JiraException(e);
        }

    }

    /**
     * @param issue
     * @return issue key
     * @throws JiraException
     */
    public String createIssue(final JiraIssue issue) throws JiraException {

        // GetCreateIssueMetadataOptionsBuilder builder = new
        // GetCreateIssueMetadataOptionsBuilder();
        // builder.withExpandedIssueTypesFields().withProjectKeys("TEST");
        //
        // List<CimProject> createIssueMetadata =
        // restClient.getIssueClient().getCreateIssueMetadata(builder.build(),
        // new NullProgressMonitor());
        if (issue.getProject() == null || issue.getProject().getKey() == null || StringUtils.isEmpty(issue.getProject().getKey())) {
            throw new JiraException("Project must be set."); //$NON-NLS-1$
        } else if (issue.getSummary() == null || StringUtils.isEmpty(issue.getSummary())) {
            throw new JiraException("Summary must be set."); //$NON-NLS-1$
        } else if (issue.getType() == null || issue.getType().getId() == null || StringUtils.isEmpty(issue.getType().getId())) {
            throw new JiraException("Issue type must be set."); //$NON-NLS-1$
        }

        long issueTypeId;

        try {
            issueTypeId = Long.parseLong(issue.getType().getId());
        } catch (final NumberFormatException e) {
            throw new JiraException("Incorrect issue type.", e); //$NON-NLS-1$
        }

        final IssueInputBuilder issueInputBuilder = new IssueInputBuilder(issue.getProject().getKey(), issueTypeId, issue.getSummary());

        if (issue.getComponents() != null && issue.getComponents().length > 0) {
            issueInputBuilder.setComponents(JiraRestConverter.convert(issue.getComponents()));
        }

        if (!StringUtils.isEmpty(issue.getDescription())) {
            issueInputBuilder.setDescription(issue.getDescription());
        }

        // Mylyn sets -1 as a value of empty assignee
        if (issue.getAssignee() != null && !"-1".equals(issue.getAssignee())) { //$NON-NLS-1$
            issueInputBuilder.setAssignee(issue.getAssignee());
        }

        if (issue.getDue() != null) {
            issueInputBuilder.setDueDate(OffsetDateTime.ofInstant(issue.getDue(), ZoneId.systemDefault()));
        }

        if (issue.getReportedVersions() != null && issue.getReportedVersions().length > 0) {
            issueInputBuilder.setAffectedVersions(JiraRestConverter.convert(issue.getReportedVersions()));
        }

        if (issue.getFixVersions() != null && issue.getFixVersions().length > 0) {
            issueInputBuilder.setFixVersions(JiraRestConverter.convert(issue.getFixVersions()));
        }

        if (issue.getPriority() == null || StringUtils.isEmpty(issue.getPriority().getId())) {
            throw new JiraException("Priority not set");
        }
        issueInputBuilder.setPriority(new BasicPriority(null, Long.valueOf(issue.getPriority().getId()), issue.getPriority().getName()));

        if (!StringUtils.isEmpty(issue.getEnvironment())) {
            issueInputBuilder.setFieldInput(new FieldInput(JiraRestFields.ENVIRONMENT, issue.getEnvironment()));
        }

        if (issue.getEstimate() != null) {
            final Map<String, Object> map = Map.ofEntries(Map.entry(JiraRestFields.ORIGINAL_ESTIMATE, String.valueOf(issue.getEstimate() / 60) + "m"),
                    Map.entry(JiraRestFields.REMAINING_ESTIMATE, String.valueOf(issue.getEstimate() / 60) + "m"));

            // TODO Remove
            // ImmutableMap.<String, Object>builder()
            // .put(JiraRestFields.ORIGINAL_ESTIMATE, String.valueOf(issue.getEstimate() /
            // 60) + "m") //$NON-NLS-1$
            // .put(JiraRestFields.REMAINING_ESTIMATE, String.valueOf(issue.getEstimate() /
            // 60) + "m") //$NON-NLS-1$
            // .build();
            issueInputBuilder.setFieldInput(new FieldInput(JiraRestFields.TIMETRACKING, new ComplexIssueInputFieldValue(map)));
        }

        if (issue.getSecurityLevel() != null && !JiraAttribute.SECURITY_LEVEL.isReadOnly()) {
            issueInputBuilder.setFieldValue(JiraRestFields.SECURITY, ComplexIssueInputFieldValue.with(JiraRestFields.ID, issue.getSecurityLevel().getId()));
        }

        if (!StringUtils.isEmpty(issue.getParentKey())) {
            issueInputBuilder.setFieldInput(new FieldInput(JiraRestFields.PARENT, ComplexIssueInputFieldValue.with(JiraRestFields.KEY, issue.getParentKey())));
        } else if (!StringUtils.isEmpty(issue.getParentId())) {
            issueInputBuilder.setFieldInput(new FieldInput(JiraRestFields.PARENT, ComplexIssueInputFieldValue.with(JiraRestFields.ID, issue.getParentId())));
        }

        return call(() -> restClient.getIssueClient().createIssue(issueInputBuilder.build()).get().getKey());
    }

    public void updateIssue(final JiraIssue changedIssue, final boolean updateEstimate) throws JiraException {
        final JiraIssue fullIssue = getIssueByKeyOrId(changedIssue.getKey(), new org.eclipse.core.runtime.NullProgressMonitor());

        final Issue issue = fullIssue.getRawIssue();

        final Collection<JiraIssueField> editableFields = Arrays.asList(fullIssue.getEditableFields());

        final List<FieldInput> updateFields = new ArrayList<>();

        updateFields.add(new FieldInput(JiraRestFields.ISSUETYPE, ComplexIssueInputFieldValue.with(JiraRestFields.ID, changedIssue.getType().getId())));
        if (editableFields.contains(new JiraIssueField(JiraRestFields.PRIORITY, null)) && changedIssue.getPriority() != null) {
            updateFields.add(new FieldInput(JiraRestFields.PRIORITY, ComplexIssueInputFieldValue.with(JiraRestFields.ID, changedIssue.getPriority().getId())));
        }

        if (editableFields.contains(new JiraIssueField(JiraRestFields.DUEDATE, null))) {
            final String date;
            if (changedIssue.getDue() == null) {
                date = null;
            } else {
                date = DateTimeFormatter.ofPattern(JiraRestFields.DATE_FORMAT).withZone(ZoneId.systemDefault()).format(changedIssue.getDue());
            }
            updateFields.add(new FieldInput(JiraRestFields.DUEDATE, date));
        }

        // if time tracking is enabled and estimate changed
        if (issue.getTimeTracking() != null && updateEstimate) {

            final Long currentEstimateInSeconds = changedIssue.getEstimate();
            final Integer previousEstimateInMinutes = issue.getTimeTracking().getRemainingEstimateMinutes();

            String outputOriginalEstimateInMinutes = null;
            String outputRemainingEstimateInMinutes = null;

            // estimate has been cleaned
            if (currentEstimateInSeconds == null && previousEstimateInMinutes != null) {

                // set estimate (remaining estimate) to original estimate (the same way as JIRA
                // UI does)
                if (issue.getTimeTracking().getOriginalEstimateMinutes() != null) {
                    outputRemainingEstimateInMinutes = outputOriginalEstimateInMinutes = String.valueOf(issue.getTimeTracking().getOriginalEstimateMinutes());
                } else {
                    StatusHandler.log(new Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN, "Remaining Estimate is set but Original Estimate is null?")); //$NON-NLS-1$
                }

            } // estimate has been set up or changed
            else if (currentEstimateInSeconds != null && previousEstimateInMinutes == null
                    || currentEstimateInSeconds != null && previousEstimateInMinutes != null && currentEstimateInSeconds / 60 != previousEstimateInMinutes) {

                outputRemainingEstimateInMinutes = String.valueOf(currentEstimateInSeconds / 60);

                // we must set original estimate explicitly otherwise it is overwritten by
                // remaining estimate (REST bug)
                outputOriginalEstimateInMinutes = outputRemainingEstimateInMinutes;
                if (issue.getTimeTracking().getOriginalEstimateMinutes() != null && issue.getTimeTracking().getOriginalEstimateMinutes() != 0) {
                    // preserve original estimate
                    outputOriginalEstimateInMinutes = String.valueOf(issue.getTimeTracking().getOriginalEstimateMinutes());
                }
            }

            if (outputOriginalEstimateInMinutes != null && outputRemainingEstimateInMinutes != null) {

                final Map<String, Object> map = Map.ofEntries(Map.entry(JiraRestFields.ORIGINAL_ESTIMATE, outputOriginalEstimateInMinutes + "m"),
                        Map.entry(JiraRestFields.REMAINING_ESTIMATE, outputRemainingEstimateInMinutes + "m"));

                // TODO Remove
                // ImmutableMap.<String, Object>builder().put(JiraRestFields.ORIGINAL_ESTIMATE,
                // outputOriginalEstimateInMinutes + "m") //$NON-NLS-1$
                // .put(JiraRestFields.REMAINING_ESTIMATE, outputRemainingEstimateInMinutes +
                // "m") //$NON-NLS-1$
                // .build();

                updateFields.add(new FieldInput(JiraRestFields.TIMETRACKING, new ComplexIssueInputFieldValue(map)));
            }
        }

        if (editableFields.contains(new JiraIssueField(JiraRestFields.VERSIONS, null))) {
            final List<ComplexIssueInputFieldValue> reportedVersions = new ArrayList<>();
            for (final JiraVersion version : changedIssue.getReportedVersions()) {
                reportedVersions.add(ComplexIssueInputFieldValue.with(JiraRestFields.ID, version.getId()));
            }
            updateFields.add(new FieldInput(JiraRestFields.VERSIONS, reportedVersions));
        }

        if (editableFields.contains(new JiraIssueField(JiraRestFields.FIX_VERSIONS, null))) {
            final List<ComplexIssueInputFieldValue> fixVersions = new ArrayList<>();
            for (final JiraVersion version : changedIssue.getFixVersions()) {
                fixVersions.add(ComplexIssueInputFieldValue.with(JiraRestFields.ID, version.getId()));
            }
            updateFields.add(new FieldInput(JiraRestFields.FIX_VERSIONS, fixVersions));
        }

        if (editableFields.contains(new JiraIssueField(JiraRestFields.COMPONENTS, null))) {
            final List<ComplexIssueInputFieldValue> components = new ArrayList<>();
            for (final JiraComponent component : changedIssue.getComponents()) {
                components.add(ComplexIssueInputFieldValue.with(JiraRestFields.ID, component.getId()));
            }
            updateFields.add(new FieldInput(JiraRestFields.COMPONENTS, components));
        }

        if (changedIssue.getSecurityLevel() != null) {
            // security level value "-1" clears security level
            updateFields
                    .add(new FieldInput(JiraRestFields.SECURITY, ComplexIssueInputFieldValue.with(JiraRestFields.ID, changedIssue.getSecurityLevel().getId())));
        } else {
            // do not clear security level as it might be not available on the screen
        }

        if (editableFields.contains(new JiraIssueField(JiraRestFields.ENVIRONMENT, null))) {
            updateFields.add(new FieldInput(JiraRestFields.ENVIRONMENT, changedIssue.getEnvironment()));
        }

        if (editableFields.contains(new JiraIssueField(JiraRestFields.SUMMARY, null))) {
            updateFields.add(new FieldInput(JiraRestFields.SUMMARY, changedIssue.getSummary()));
        }

        if (editableFields.contains(new JiraIssueField(JiraRestFields.DESCRIPTION, null))) {
            updateFields.add(new FieldInput(JiraRestFields.DESCRIPTION, changedIssue.getDescription() != null ? changedIssue.getDescription() : "")); //$NON-NLS-1$
        }

        if (editableFields.contains(new JiraIssueField(JiraRestFields.ASSIGNEE, null))) {
            final String assigne = "-1".equals(changedIssue.getAssignee()) ? "" : changedIssue.getAssignee().getId(); //$NON-NLS-1$//$NON-NLS-2$
            final String prevAssigne = issue.getAssignee() != null ? issue.getAssignee().getId() : ""; //$NON-NLS-1$

            if (!assigne.equals(prevAssigne)) {
                updateFields.add(new FieldInput(JiraRestFields.ASSIGNEE, ComplexIssueInputFieldValue.with(cache.getServerInfo().getAccountTag(), assigne)));
            }
        }

        if (editableFields.contains(new JiraIssueField(JiraRestFields.LABELS, null))) {
            updateFields.add(new FieldInput(JiraRestFields.LABELS, Arrays.asList(changedIssue.getLabels())));
        }

        for (final JiraCustomField customField : changedIssue.getCustomFields()) {
            final FieldInput field = JiraRestConverter.convert(customField);
            if (field != null) {
                updateFields.add(field);
            }
        }

        call(() -> {
            final IssueInput issueInput = IssueInput.createWithFields(updateFields.toArray(new FieldInput[0]));
            restClient.getIssueClient().updateIssue(issue.getKey(), issueInput).claim();
            return null;
        });

    }

    private <V> V call(final Callable<V> callable) throws JiraException {

        try {
            return callable.call();
        } catch (final RestClientException e) {
            if (e.getMessage().contains(HTTP_401)) {
                throw new JiraAuthenticationException(HTTP_401);
            } else if (e.getMessage().contains(HTTP_403)) {
                throw new JiraException(HTTP_403 + ". Captcha might be required. Please try to log in via browser."); //$NON-NLS-1$
            } else if (e.getMessage().contains(CONNECTION_REFUSED)) {
                throw new JiraException(CONNECTION_REFUSED, e);
            } else if (e.getMessage().contains(UNKNOWN_HOST_EXCEPTION)) {
                final int index = e.getMessage().indexOf(UNKNOWN_HOST_EXCEPTION);
                throw new JiraServiceUnavailableException(e.getMessage().substring(index));
            } else if (e.getMessage().contains(ILLEGAL_ARGUMENT_EXCEPTION)) {
                final int index = e.getMessage().indexOf(ILLEGAL_ARGUMENT_EXCEPTION);
                throw new JiraException(e.getMessage().substring(index), e);
            } else if (e.getMessage().contains(HTTP_302)) {
                final int index = e.getMessage().indexOf(HTTP_302);
                throw new JiraException(e.getMessage().substring(index) + ". Https might be required.", e); //$NON-NLS-1$
            } else if (e.getMessage().contains(HTTP_404)) {
                throw new JiraServiceUnavailableException(e);
            } else if (e.getMessage().contains("is not supported in Legacy Mode")) { //$NON-NLS-1$
                throw new JiraException(e.getMessage() + " Please disable time tracking \"Legacy Mode\" in JIRA.", e); //$NON-NLS-1$
            } else if (e.getMessage().contains(NULL_POINTER_EXCEPTION)) {
                throw new RuntimeException(e);
            } else if (e.getMessage().contains("Client response status: 301") && !followRedirects) { //$NON-NLS-1$
                throw new JiraException("Client response status: 301. Please enable 'Follow redirects' checkbox for task repository.", e); //$NON-NLS-1$
            } else if (e.getMessage().contains(SOCKET_TIMEOUT_EXCEPTION)) {
                final int index = e.getMessage().indexOf(SOCKET_TIMEOUT_EXCEPTION);
                throw new JiraException(e.getMessage().substring(index), e);
            } else if (e.getMessage().contains(CONNECT_TIMEOUT_EXCEPTION)) {
                final int index = e.getMessage().indexOf(CONNECT_TIMEOUT_EXCEPTION);
                throw new JiraException(e.getMessage().substring(index), e);
            } else if (e.getMessage().contains("unable to find valid certification path")) { //$NON-NLS-1$
                throw new JiraException(
                        "Connection failed. JIRA self-signed certificates are not supported.\nFor workaround see https://ecosystem.atlassian.net/browse/PLE-1430", //$NON-NLS-1$
                        e);
            } else {
                // use "e.getMessage()" as an argument instead of "e" so it fits error window
                // (mainly TaskRepository dialog)
                throw new JiraException(e.getMessage(), e);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new JiraException(e);
        }

    }

    public static SimpleDateFormat getOffsetDateTimeFormat() {
        return REST_DATETIME_FORMAT;
    }

    public static SimpleDateFormat getDateFormat() {
        return REST_DATE_FORMAT;
    }

    public JiraSecurityLevel[] getSecurityLevels(final String projectKey) throws JiraException {
        try {
            final SecurityLevel securityLevel = restClient.getProjectClient().getSecurityLevel(projectKey).get();

            return new JiraSecurityLevel[] { JiraSecurityLevel.convert(securityLevel) };
        } catch (InterruptedException | ExecutionException | URISyntaxException e) {
            return new JiraSecurityLevel[0]; // FIXME Not available on JiraServer. Add better check
        }
    }

    public void deleteIssue(final String key, final IProgressMonitor monitor) throws JiraException {
        call(() -> {
            try {
                restClient.getIssueClient().deleteIssue(key, true).claim();
            } catch (final URISyntaxException e) {
                throw new JiraException(e);
            }
            return null;
        });
    }

    public List<User> getProjectAssignables(final String projectKey) throws JiraException {
        try {
            return restClient.getUserClient().findAssignableUsersForProject(projectKey, null, 1000, true, false).get();
        } catch (InterruptedException | ExecutionException | URISyntaxException e) {
            throw new JiraException(e);
        }
    }
}
