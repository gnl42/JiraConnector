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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.osgi.util.NLS;

import com.atlassian.httpclient.api.factory.HttpClientOptions;

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
import me.glindholm.jira.rest.client.api.GetCreateIssueMetadataOptions;
import me.glindholm.jira.rest.client.api.GetCreateIssueMetadataOptionsBuilder;
import me.glindholm.jira.rest.client.api.IssueRestClient;
import me.glindholm.jira.rest.client.api.JiraRestClient;
import me.glindholm.jira.rest.client.api.RestClientException;
import me.glindholm.jira.rest.client.api.domain.BasicPriority;
import me.glindholm.jira.rest.client.api.domain.BasicProject;
import me.glindholm.jira.rest.client.api.domain.BasicUser;
import me.glindholm.jira.rest.client.api.domain.BasicWatchers;
import me.glindholm.jira.rest.client.api.domain.CimFieldInfo;
import me.glindholm.jira.rest.client.api.domain.CimIssueType;
import me.glindholm.jira.rest.client.api.domain.CimProject;
import me.glindholm.jira.rest.client.api.domain.Comment;
import me.glindholm.jira.rest.client.api.domain.Field;
import me.glindholm.jira.rest.client.api.domain.Issue;
import me.glindholm.jira.rest.client.api.domain.Project;
import me.glindholm.jira.rest.client.api.domain.Session;
import me.glindholm.jira.rest.client.api.domain.Watchers;
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

    public JiraRestClientAdapter(String url, JiraClientCache cache, boolean followRedirects) {
        this.url = url;
        this.cache = cache;
        this.followRedirects = followRedirects;
    }

    public JiraRestClientAdapter(String url, String userName, String password, final Proxy proxy, JiraClientCache cache, final boolean followRedirects) {
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
            HttpClientOptions httpOptions = new HttpClientOptions();
            httpOptions.setUserAgent("JiraConnector for Eclipse");

            restClient = new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(new URI(url), userName, password);

            // restClient = new JerseyJiraRestClientBuilder().header("User-Agent",
            // "JiraConnector for Eclipse") //$NON-NLS-1$ //$NON-NLS-2$
            // .queryParam("requestSource", "eclipse-ide-connector")
            // //$NON-NLS-1$//$NON-NLS-2$
            // .create(new URI(url), new BasicHttpAuthenticationHandler(userName, password)
            // {
            // @Override
            // public void configure(ApacheHttpClientConfig config) {
            // super.configure(config);
            // if (proxy != null) {
            // InetSocketAddress address = (InetSocketAddress) proxy.address();
            // if (proxy instanceof AuthenticatedProxy) {
            // AuthenticatedProxy authProxy = (AuthenticatedProxy) proxy;
            //
            // config.getState().setProxyCredentials(AuthScope.ANY_REALM,
            // address.getHostName(),
            // address.getPort(), authProxy.getUserName(), authProxy.getPassword());
            // }
            //
            // }
            //
            // // timeout
            // config.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT,
            // TIMEOUT_CONNECTION_IN_MS);
            // config.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT,
            // TIMEOUT_READ_IN_MS);
            //
            //// SSLContext context;
            //// try {
            //// context = SSLContext.getInstance("SSL");
            //// context.init(null, trustAll, new SecureRandom());
            ////
            //// config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
            //// new HTTPSProperties(new HostnameVerifier() {
            //// public boolean verify(String s, SSLSession sslSession) {
            //// return false;
            //// }
            ////
            //// }, context));
            ////
            //// } catch (NoSuchAlgorithmException e1) {
            //// // TODO Auto-generated catch block
            //// e1.printStackTrace();
            //// } catch (KeyManagementException e) {
            //// // TODO Auto-generated catch block
            //// e.printStackTrace();
            //// }
            //
            // }
            // }, followRedirects);
            //
            // if (proxy != null) {
            // final InetSocketAddress address = (InetSocketAddress) proxy.address();
            // restClient.getTransportClient()
            // .getProperties()
            // .put(ApacheHttpClientConfig.PROPERTY_PROXY_URI,
            // "http://" + address.getHostName() + ":" + address.getPort()); //$NON-NLS-1$
            // //$NON-NLS-2$
            // }

            // HttpClient httpClient =
            // restClient.getTransportClient().getClientHandler().getHttpClient();
            // X509HostnameVerifier hostnameVerifier = new AllowAllHostnameVerifier();
            // SSLSocketFactory sslSf = new SSLSocketFactory(trustStrategy,
            // hostnameVerifier);
            // Scheme https = new Scheme("https", 443, sslSf);
            // SchemeRegistry schemeRegistry = new SchemeRegistry();
            // schemeRegistry.register(https);

            // httpClient.getHttpConnectionManager().getParams();
            // restClient.getTransportClient()
            // .getProperties()
            // .put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(new
            // HostnameVerifier() {
            // public boolean verify(String s, SSLSession sslSession) {
            // return false;
            // }
            //
            // }, context));

        } catch (URISyntaxException e) {
            // we should never get here as Mylyn constructs URI first and fails if it is
            // incorrect
            StatusHandler.log(new Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, e.getMessage()));
        }
    }

    public void addComment(final String issueKey, final String comment) throws JiraException {

        call(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                restClient.getIssueClient().addComment(getIssue(issueKey).getCommentsUri(), Comment.valueOf(comment)).claim();

                return null;
            }
        });
    }

    private Issue getIssue(final String issueKeyOrId) throws JiraException {
        return call(new Callable<Issue>() {
            @Override
            public Issue call() throws JiraException {
                try {
                    Issue issue = restClient.getIssueClient().getIssue(issueKeyOrId, List.of(IssueRestClient.Expandos.SCHEMA)).get();
                    final BasicWatchers watched = issue.getWatched();
                    final Watchers watchers = restClient.getIssueClient().getWatchers(watched.getSelf()).get();
                    issue.setWatchers(watchers);
                    return issue;
                } catch (InterruptedException | ExecutionException | URISyntaxException e) {
                    throw new JiraException(e);
                }
            }
        });
    }

    public void addAttachment(String issueKey, byte[] content, String filename) throws JiraException {
        restClient.getIssueClient().addAttachment(getIssue(issueKey).getAttachmentsUri(), new ByteArrayInputStream(content), filename).claim();
    }

    public InputStream getAttachment(URI attachmentUri) throws JiraException {
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

        return call(new Callable<JiraNamedFilter[]>() {

            @Override
            public JiraNamedFilter[] call() throws Exception {
                return JiraRestConverter.convertNamedFilters(restClient.getSearchClient().getFavouriteFilters().get());
            }
        });
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
            List<Field> metadata = restClient.getMetadataClient().getFields().get();
            return metadata;
        } catch (InterruptedException | ExecutionException | URISyntaxException e) {
            throw new JiraException(e);
        }
    }

    public JiraIssue getIssueByKeyOrId(String issueKeyOrId, IProgressMonitor monitor) throws JiraException {
        return JiraRestConverter.convertIssue(getIssue(issueKeyOrId), cache, url, monitor);
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

    public JiraIssueType[] getIssueTypes(String projectKey) throws JiraException {
        try {
            return JiraRestConverter.convertIssueTypes(restClient.getProjectClient().getProject(projectKey).get().getIssueTypes());
        } catch (InterruptedException | ExecutionException | URISyntaxException e) {
            throw new JiraException(e);
        }
    }

    public List<JiraIssue> getIssues(final String jql, final int maxSearchResult, final IProgressMonitor monitor) throws JiraException {

        return call(new Callable<List<JiraIssue>>() {

            @Override
            public List<JiraIssue> call() throws Exception {
                List<JiraIssue> issues = JiraRestConverter
                        .convertIssues(restClient.getSearchClient().searchJql(jql, maxSearchResult, 0, null).get().getIssues()); // FIXME 4th param

                List<JiraIssue> fullIssues = new ArrayList<>();

                for (JiraIssue issue : issues) {
                    fullIssues.add(JiraRestConverter.convertIssue(getIssue(issue.getKey()), cache, url, monitor));
                }

                return fullIssues;
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

    public void getProjectDetails(JiraProject project) throws JiraException {

        try {
            Project projectWithDetails = restClient.getProjectClient().getProject(project.getKey()).get();

            GetCreateIssueMetadataOptions builder = new GetCreateIssueMetadataOptionsBuilder()
                    .withProjectIds(Long.valueOf(project.getId()))
                    .withExpandedIssueTypesFields()
                    .build();
            CimProject cimProjectWithDetails = restClient.getIssueClient().getCreateIssueMetadata(builder).get().iterator().next();

            Map<Long, Map<String, CimFieldInfo>> projectMetadata = new HashMap<>();
            for (CimIssueType issueType : cimProjectWithDetails.getIssueTypes()) {
                projectMetadata.put(issueType.getId(), issueType.getFields());
            }
            project.setfieldMetadata(projectMetadata);

            project.setComponents(JiraRestConverter.convertComponents(projectWithDetails.getComponents()));
            project.setVersions(JiraRestConverter.convertVersions(projectWithDetails.getVersions()));
            project.setIssueTypes(JiraRestConverter.convertIssueTypes(projectWithDetails.getIssueTypes()));
        } catch (InterruptedException | ExecutionException | URISyntaxException e) {
            throw new JiraException(e);
        }
    }

    public void addWorklog(String issueKey, JiraWorkLog jiraWorklog) throws JiraException {
        Issue issue = getIssue(issueKey);
        try {
            restClient.getIssueClient().addWorklog(issue.getWorklogUri(), JiraRestConverter.convert(jiraWorklog, issue.getSelf())).claim();
        } catch (URISyntaxException e) {
            throw new JiraException(e);
        }
    }

    public JiraServerInfo getServerInfo() throws JiraException {
        return call(new Callable<JiraServerInfo>() {
            @Override
            public JiraServerInfo call() throws JiraException {
                try {
                    return JiraRestConverter.convert(restClient.getMetadataClient().getServerInfo().get());
                } catch (InterruptedException | ExecutionException | URISyntaxException e) {
                    throw new JiraException(e);
                }
            }
        });
    }

    public Session getSession() throws JiraException {
        return call(new Callable<Session>() {
            @Override
            public Session call() throws JiraException {
                try {
                    return restClient.getSessionClient().getCurrentSession().get();
                } catch (RestClientException | InterruptedException | ExecutionException | URISyntaxException e) {
                    throw new JiraException(e);
                }
            }
        });
    }

    public List<JiraAction> getTransitions(String issueKey) throws JiraException {


        try {
            URI transitionUri = new URIBuilder(url)
                    .appendPath("/rest/api/latest") //
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

    public void transitionIssue(JiraIssue issue, String transitionKey, String comment, List<JiraIssueField> transitionFields) throws JiraException {

        Comment outComment = StringUtils.isEmpty(comment) ? null : Comment.valueOf(comment);

        List<FieldInput> fields = new ArrayList<>();
        for (JiraIssueField transitionField : transitionFields) {

            if (transitionField.isRequired()) {

                String[] values = issue.getFieldValues(transitionField.getName());

                if (values != null && values.length > 0) {
                    if (transitionField.getName().equals(JiraRestFields.SUMMARY) || transitionField.getName().equals(JiraRestFields.DESCRIPTION)
                            || transitionField.getName().equals(JiraRestFields.ENVIRONMENT)) {

                        fields.add(new FieldInput(transitionField.getName(), values[0]));

                    } else if (transitionField.getName().equals(JiraRestFields.DUEDATE)) {

                        String date = DateTimeFormatter.ofPattern(JiraRestFields.DATE_FORMAT).format(issue.getDue());
                        if (values[0] == null) {
                            date = null;
                        }
                        fields.add(new FieldInput(JiraRestFields.DUEDATE, date));

                    } else if (transitionField.getName().equals(JiraRestFields.LABELS)) {

                        fields.add(new FieldInput(transitionField.getName(), Arrays.asList(values)));

                    } else if (transitionField.getName().equals(JiraRestFields.RESOLUTION) || transitionField.getName().equals(JiraRestFields.ISSUETYPE)
                            || transitionField.getName().equals(JiraRestFields.PRIORITY) || transitionField.getName().equals(JiraRestFields.SECURITY)) {

                        fields.add(new FieldInput(transitionField.getId(), ComplexIssueInputFieldValue.with(JiraRestFields.ID, values[0])));

                    } else if (transitionField.getType() != null && transitionField.getType().equals("array") //$NON-NLS-1$
                            && !transitionField.getName().startsWith("customfield_")) { //$NON-NLS-1$

                        List<ComplexIssueInputFieldValue> array = new ArrayList<>();

                        for (String value : values) {
                            array.add(ComplexIssueInputFieldValue.with(JiraRestFields.ID, value));
                        }

                        fields.add(new FieldInput(transitionField.getId(), array));

                    } else if (transitionField.getName().startsWith("customfield_")) { //$NON-NLS-1$

                        JiraCustomField customField = issue.getCustomFieldById(transitionField.getId());

                        FieldInput field = JiraRestConverter.convert(customField);
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
                        JiraCustomField customField = issue.getCustomFieldById(transitionField.getId());
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

        TransitionInput transitionInput = new TransitionInput(Integer.parseInt(transitionKey), fields, outComment);

        try {
            restClient.getIssueClient().transition(getIssue(issue.getKey()), transitionInput).claim();
        } catch (URISyntaxException | JiraException e) {
            throw new JiraException(e);
        }

    }

    public void assignIssue(String issueKey, String user, String comment) throws JiraException {
        Issue issue = getIssue(issueKey);

        IssueInput fields = IssueInput.createWithFields(new FieldInput(JiraRestFields.ASSIGNEE, ComplexIssueInputFieldValue.with(JiraRestFields.NAME, user)));
        try {
            restClient.getIssueClient().updateIssue(issue.getKey(), fields).claim();
        } catch (URISyntaxException e) {
            throw new JiraException(e);
        }

    }

    /**
     * @param issue
     * @return issue key
     * @throws JiraException
     */
    public String createIssue(JiraIssue issue) throws JiraException {

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
        } catch (NumberFormatException e) {
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
        if (issue.getAssignee() != null && !issue.getAssignee().equals("-1")) { //$NON-NLS-1$
            issueInputBuilder.setAssignee(new BasicUser(null, issue.getAssignee(), null));
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
            Map<String, Object> map = Map.ofEntries(Map.entry(JiraRestFields.ORIGINAL_ESTIMATE, String.valueOf(issue.getEstimate() / 60) + "m"),
                    Map.entry(JiraRestFields.REMAINING_ESTIMATE, String.valueOf(issue.getEstimate() / 60) + "m"));

            // TODO Remove
            //                    ImmutableMap.<String, Object>builder()
            //                    .put(JiraRestFields.ORIGINAL_ESTIMATE, String.valueOf(issue.getEstimate() / 60) + "m") //$NON-NLS-1$
            //                    .put(JiraRestFields.REMAINING_ESTIMATE, String.valueOf(issue.getEstimate() / 60) + "m") //$NON-NLS-1$
            //                    .build();
            issueInputBuilder.setFieldInput(new FieldInput(JiraRestFields.TIMETRACKING, new ComplexIssueInputFieldValue(map)));
        }

        if (issue.getSecurityLevel() != null) {
            issueInputBuilder.setFieldValue(JiraRestFields.SECURITY, ComplexIssueInputFieldValue.with(JiraRestFields.ID, issue.getSecurityLevel().getId()));
        }

        if (!StringUtils.isEmpty(issue.getParentKey())) {
            issueInputBuilder.setFieldInput(new FieldInput(JiraRestFields.PARENT, ComplexIssueInputFieldValue.with(JiraRestFields.KEY, issue.getParentKey())));
        } else if (!StringUtils.isEmpty(issue.getParentId())) {
            issueInputBuilder.setFieldInput(new FieldInput(JiraRestFields.PARENT, ComplexIssueInputFieldValue.with(JiraRestFields.ID, issue.getParentId())));
        }

        return call(new Callable<String>() {

            @Override
            public String call() throws Exception {
                return restClient.getIssueClient().createIssue(issueInputBuilder.build()).get().getKey();
            }
        });
    }

    public void updateIssue(JiraIssue changedIssue, boolean updateEstimate) throws JiraException {
        final JiraIssue fullIssue = getIssueByKeyOrId(changedIssue.getKey(), new org.eclipse.core.runtime.NullProgressMonitor());

        final Issue issue = fullIssue.getRawIssue();

        Collection<JiraIssueField> editableFields = Arrays.asList(fullIssue.getEditableFields());

        final List<FieldInput> updateFields = new ArrayList<>();

        updateFields.add(new FieldInput(JiraRestFields.ISSUETYPE, ComplexIssueInputFieldValue.with(JiraRestFields.ID, changedIssue.getType().getId())));
        if (editableFields.contains(new JiraIssueField(JiraRestFields.PRIORITY, null)) && changedIssue.getPriority() != null) {
            updateFields.add(new FieldInput(JiraRestFields.PRIORITY, ComplexIssueInputFieldValue.with(JiraRestFields.ID, changedIssue.getPriority().getId())));
        }

        if (editableFields.contains(new JiraIssueField(JiraRestFields.DUEDATE, null))) {
            String date = DateTimeFormatter.ofPattern(JiraRestFields.DATE_FORMAT).withZone(ZoneId.systemDefault()).format(changedIssue.getDue());
            if (changedIssue.getDue() == null) {
                date = null;
            }
            updateFields.add(new FieldInput(JiraRestFields.DUEDATE, date));
        }

        // if time tracking is enabled and estimate changed
        if (issue.getTimeTracking() != null && updateEstimate) {

            Long currentEstimateInSeconds = changedIssue.getEstimate();
            Integer previousEstimateInMinutes = issue.getTimeTracking().getRemainingEstimateMinutes();

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

                Map<String, Object> map = Map.ofEntries(Map.entry(JiraRestFields.ORIGINAL_ESTIMATE, outputOriginalEstimateInMinutes + "m"),
                        Map.entry(JiraRestFields.REMAINING_ESTIMATE, outputRemainingEstimateInMinutes + "m"));

                // TODO Remove
                //                        ImmutableMap.<String, Object>builder().put(JiraRestFields.ORIGINAL_ESTIMATE, outputOriginalEstimateInMinutes + "m") //$NON-NLS-1$
                //                        .put(JiraRestFields.REMAINING_ESTIMATE, outputRemainingEstimateInMinutes + "m") //$NON-NLS-1$
                //                        .build();

                updateFields.add(new FieldInput(JiraRestFields.TIMETRACKING, new ComplexIssueInputFieldValue(map)));
            }
        }

        if (editableFields.contains(new JiraIssueField(JiraRestFields.VERSIONS, null))) {
            List<ComplexIssueInputFieldValue> reportedVersions = new ArrayList<>();
            for (JiraVersion version : changedIssue.getReportedVersions()) {
                reportedVersions.add(ComplexIssueInputFieldValue.with(JiraRestFields.ID, version.getId()));
            }
            updateFields.add(new FieldInput(JiraRestFields.VERSIONS, reportedVersions));
        }

        if (editableFields.contains(new JiraIssueField(JiraRestFields.FIX_VERSIONS, null))) {
            List<ComplexIssueInputFieldValue> fixVersions = new ArrayList<>();
            for (JiraVersion version : changedIssue.getFixVersions()) {
                fixVersions.add(ComplexIssueInputFieldValue.with(JiraRestFields.ID, version.getId()));
            }
            updateFields.add(new FieldInput(JiraRestFields.FIX_VERSIONS, fixVersions));
        }

        if (editableFields.contains(new JiraIssueField(JiraRestFields.COMPONENTS, null))) {
            List<ComplexIssueInputFieldValue> components = new ArrayList<>();
            for (JiraComponent component : changedIssue.getComponents()) {
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
            String assigne = "-1".equals(changedIssue.getAssignee()) ? "" : changedIssue.getAssignee(); //$NON-NLS-1$//$NON-NLS-2$
            String prevAssigne = issue.getAssignee() != null ? issue.getAssignee().getName() : ""; //$NON-NLS-1$

            if (!assigne.equals(prevAssigne)) {
                updateFields.add(new FieldInput(JiraRestFields.ASSIGNEE, ComplexIssueInputFieldValue.with(JiraRestFields.NAME, assigne)));
            }
        }

        if (editableFields.contains(new JiraIssueField(JiraRestFields.LABELS, null))) {
            updateFields.add(new FieldInput(JiraRestFields.LABELS, Arrays.asList(changedIssue.getLabels())));
        }

        for (JiraCustomField customField : changedIssue.getCustomFields()) {
            FieldInput field = JiraRestConverter.convert(customField);
            if (field != null) {
                updateFields.add(field);
            }
        }

        call(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                IssueInput issueInput = IssueInput.createWithFields(updateFields.toArray(new FieldInput[0]));
                restClient.getIssueClient().updateIssue(issue.getKey(), issueInput).claim();
                return null;
            }
        });

    }

    private <V> V call(Callable<V> callable) throws JiraException {

        try {
            return callable.call();
        } catch (RestClientException e) {
            if (e.getMessage().contains(HTTP_401)) {
                throw new JiraAuthenticationException(HTTP_401);
            } else if (e.getMessage().contains(HTTP_403)) {
                throw new JiraException(HTTP_403 + ". Captcha might be required. Please try to log in via browser."); //$NON-NLS-1$
            } else if (e.getMessage().contains(CONNECTION_REFUSED)) {
                throw new JiraException(CONNECTION_REFUSED, e);
            } else if (e.getMessage().contains(UNKNOWN_HOST_EXCEPTION)) {
                int index = e.getMessage().indexOf(UNKNOWN_HOST_EXCEPTION);
                throw new JiraServiceUnavailableException(e.getMessage().substring(index));
            } else if (e.getMessage().contains(ILLEGAL_ARGUMENT_EXCEPTION)) {
                int index = e.getMessage().indexOf(ILLEGAL_ARGUMENT_EXCEPTION);
                throw new JiraException(e.getMessage().substring(index), e);
            } else if (e.getMessage().contains(HTTP_302)) {
                int index = e.getMessage().indexOf(HTTP_302);
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
                int index = e.getMessage().indexOf(SOCKET_TIMEOUT_EXCEPTION);
                throw new JiraException(e.getMessage().substring(index), e);
            } else if (e.getMessage().contains(CONNECT_TIMEOUT_EXCEPTION)) {
                int index = e.getMessage().indexOf(CONNECT_TIMEOUT_EXCEPTION);
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
        } catch (Exception e) {
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

    public JiraSecurityLevel[] getSecurityLevels(String projectKey) throws JiraException {

        GetCreateIssueMetadataOptionsBuilder builder = new GetCreateIssueMetadataOptionsBuilder();

        List<CimProject> createIssueMetadata;
        try {
            createIssueMetadata = restClient.getIssueClient().getCreateIssueMetadata(builder.withExpandedIssueTypesFields().withProjectKeys(projectKey).build())
                    .get();
        } catch (InterruptedException | ExecutionException | URISyntaxException e) {
            throw new JiraException(e);
        }

        if (createIssueMetadata.iterator().hasNext()) {
            CimProject cimProject = createIssueMetadata.iterator().next();

            // get first issue type (security level is the same for all issue types in the
            // project)
            if (cimProject.getIssueTypes().iterator().hasNext()) {
                CimIssueType cimIssueType = cimProject.getIssueTypes().iterator().next();

                CimFieldInfo cimFieldSecurity = cimIssueType.getFields().get(JiraRestFields.SECURITY);

                if (cimFieldSecurity != null) {
                    List<Object> allowedValues = cimFieldSecurity.getAllowedValues();

                    List<JiraSecurityLevel> securityLevels = new ArrayList<>();

                    for (Object allowedValue : allowedValues) {
                        if (allowedValue instanceof JiraSecurityLevel) {
                            JiraSecurityLevel securityLevel = (JiraSecurityLevel) allowedValue;

                            securityLevels.add(new JiraSecurityLevel(securityLevel.getId().toString(), securityLevel.getName()));
                        }
                    }

                    return securityLevels.toArray(new JiraSecurityLevel[securityLevels.size()]);
                } else {
                    // (security might not exist if not defined for project)
                    return new JiraSecurityLevel[0];
                }
            }
        }

        return new JiraSecurityLevel[0];
    }

    public void deleteIssue(final String key, IProgressMonitor monitor) throws JiraException {
        call(new Callable<Void>() {
            @Override
            public Void call() throws JiraException {
                try {
                    restClient.getIssueClient().deleteIssue(key, true).claim();
                } catch (URISyntaxException e) {
                    throw new JiraException(e);
                }
                return null;
            }
        });
    }

}
