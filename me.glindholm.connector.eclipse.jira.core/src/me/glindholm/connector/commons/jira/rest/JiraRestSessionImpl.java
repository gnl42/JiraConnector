package me.glindholm.connector.commons.jira.rest;

import me.glindholmjira.rest.client.GetCreateIssueMetadataOptionsBuilder;
import me.glindholmjira.rest.client.IssueRestClient;
import me.glindholmjira.rest.client.JiraRestClient;
import me.glindholmjira.rest.client.NullProgressMonitor;
import me.glindholmjira.rest.client.OptionalIterable;
import me.glindholmjira.rest.client.RestClientException;
import me.glindholmjira.rest.client.auth.AnonymousAuthenticationHandler;
import me.glindholmjira.rest.client.auth.BasicHttpAuthenticationHandler;
import me.glindholmjira.rest.client.domain.Attachment;
import me.glindholmjira.rest.client.domain.Authentication;
import me.glindholmjira.rest.client.domain.BasicComponent;
import me.glindholmjira.rest.client.domain.BasicIssue;
import me.glindholmjira.rest.client.domain.BasicProject;
import me.glindholmjira.rest.client.domain.CimFieldInfo;
import me.glindholmjira.rest.client.domain.CimIssueType;
import me.glindholmjira.rest.client.domain.CimProject;
import me.glindholmjira.rest.client.domain.Comment;
import me.glindholmjira.rest.client.domain.FavouriteFilter;
import me.glindholmjira.rest.client.domain.Issue;
import me.glindholmjira.rest.client.domain.IssueFieldId;
import me.glindholmjira.rest.client.domain.IssueType;
import me.glindholmjira.rest.client.domain.Priority;
import me.glindholmjira.rest.client.domain.Resolution;
import me.glindholmjira.rest.client.domain.SearchResult;
import me.glindholmjira.rest.client.domain.SecurityLevel;
import me.glindholmjira.rest.client.domain.SessionCookie;
import me.glindholmjira.rest.client.domain.Status;
import me.glindholmjira.rest.client.domain.Transition;
import me.glindholmjira.rest.client.domain.User;
import me.glindholmjira.rest.client.domain.Version;
import me.glindholmjira.rest.client.domain.input.ComplexIssueInputFieldValue;
import me.glindholmjira.rest.client.domain.input.FieldInput;
import me.glindholmjira.rest.client.domain.input.IssueInputBuilder;
import me.glindholmjira.rest.client.domain.input.TransitionInput;
import me.glindholmjira.rest.client.domain.input.WorklogInputBuilder;
import me.glindholmjira.rest.client.internal.ServerVersionConstants;
import me.glindholmjira.rest.client.internal.jersey.JerseyJiraRestClientBuilder;
import me.glindholmjira.rest.client.internal.json.JsonParseUtil;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.filter.Filterable;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.connector.commons.jira.JIRAAction;
import me.glindholm.connector.commons.jira.JIRAActionBean;
import me.glindholm.connector.commons.jira.JIRAActionField;
import me.glindholm.connector.commons.jira.JIRAActionFieldBean;
import me.glindholm.connector.commons.jira.JIRAIssue;
import me.glindholm.connector.commons.jira.JIRAIssueBean;
import me.glindholm.connector.commons.jira.JIRASessionPartOne;
import me.glindholm.connector.commons.jira.JIRASessionPartTwo;
import me.glindholm.connector.commons.jira.JiraUserNotFoundException;
import me.glindholm.connector.commons.jira.beans.JIRAAttachment;
import me.glindholm.connector.commons.jira.beans.JIRAComment;
import me.glindholm.connector.commons.jira.beans.JIRAComponentBean;
import me.glindholm.connector.commons.jira.beans.JIRAConstant;
import me.glindholm.connector.commons.jira.beans.JIRAIssueTypeBean;
import me.glindholm.connector.commons.jira.beans.JIRAPriorityBean;
import me.glindholm.connector.commons.jira.beans.JIRAProject;
import me.glindholm.connector.commons.jira.beans.JIRAProjectBean;
import me.glindholm.connector.commons.jira.beans.JIRAQueryFragment;
import me.glindholm.connector.commons.jira.beans.JIRAResolutionBean;
import me.glindholm.connector.commons.jira.beans.JIRASavedFilter;
import me.glindholm.connector.commons.jira.beans.JIRASavedFilterBean;
import me.glindholm.connector.commons.jira.beans.JIRASecurityLevelBean;
import me.glindholm.connector.commons.jira.beans.JIRAStatusBean;
import me.glindholm.connector.commons.jira.beans.JIRAUserBean;
import me.glindholm.connector.commons.jira.beans.JIRAVersionBean;
import me.glindholm.connector.commons.jira.beans.JiraFilter;
import me.glindholm.connector.commons.jira.rss.JIRAException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;
import me.glindholm.theplugin.commons.remoteapi.ServerData;
import me.glindholm.theplugin.commons.remoteapi.jira.JiraCaptchaRequiredException;
import me.glindholm.theplugin.commons.util.HttpConfigurableAdapter;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * User: kalamon
 * Date: 14.11.12
 * Time: 16:29
 */
public class JiraRestSessionImpl implements JIRASessionPartOne, JIRASessionPartTwo {
    private final ConnectionCfg server;
    private final HttpConfigurableAdapter proxyInfo;
    private final JiraRestClient restClient;
    final NullProgressMonitor pm = new NullProgressMonitor();
    private Authentication authentication = null;
    private ApacheHttpClientConfig apacheClientConfig = null;

    public JiraRestSessionImpl(ConnectionCfg server, final HttpConfigurableAdapter proxyInfo) throws URISyntaxException {
        this.server = server;
        this.proxyInfo = proxyInfo;

        if (((ServerData) server).isUseSessionCookies()) {
            restClient = new JerseyJiraRestClientBuilder()
                .header("User-Agent", "Atlassian IntelliJ IDEA Connector")
                .queryParam("requestSource", "intellij-ide-connector")
                .create(new URI(server.getUrl()), new AnonymousAuthenticationHandler() {
                    @Override
                    public void configure(ApacheHttpClientConfig config) {
                        super.configure(config);
                        setupApacheClient(config);
                    }

                    @Override
                    public void configure(Filterable filterable, Client client) {
                        super.configure(filterable, client);
                        setupApacheClient(apacheClientConfig);
                    }
                });
        } else {
            restClient = new JerseyJiraRestClientBuilder()
                .header("User-Agent", "Atlassian IntelliJ IDEA Connector")
                .queryParam("requestSource", "intellij-ide-connector")
                .create(new URI(server.getUrl()), new BasicHttpAuthenticationHandler(server.getUsername(), server.getPassword()) {
                    @Override
                    public void configure(ApacheHttpClientConfig config) {
                        config.getProperties().put(ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES, true);
                        config.getState().getHttpState().setCookiePolicy(CookiePolicy.COMPATIBILITY);
                        super.configure(config);
                        config.getProperties().put(ApacheHttpClientConfig.PROPERTY_PREEMPTIVE_AUTHENTICATION, false);

                        setupApacheClient(config);

                    }

                    @Override
                    public void configure(Filterable filterable, Client client) {
                        super.configure(filterable, client);
                        setupApacheClient(apacheClientConfig);
                    }
                });
        }

        if (proxyInfo != null && proxyInfo.isUseHttpProxy()) {
            restClient.getTransportClient().getProperties().put(
                ApacheHttpClientConfig.PROPERTY_PROXY_URI, "http://" + proxyInfo.getProxyHost() + ":" + proxyInfo.getProxyPort());
        }
        if (((ServerData) server).isUseSessionCookies()) {
            restClient.getTransportClient().getProperties().put(ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES, true);
            restClient.getTransportClient().getClientHandler().getHttpClient().getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        }
    }

    public boolean supportsRest() throws JIRAException {
        try {
            return restClient.getMetadataClient().getServerInfo(pm).getBuildNumber() >= ServerVersionConstants.BN_JIRA_5;
        } catch (Exception e) {
            return false;
        }
    }

    public void login(final String userName, final String password) throws RemoteApiException {
        wrapWithRemoteApiException(new Callable<Void>() {
            public Void call() throws Exception {
                authentication = restClient.getSessionClient().login(userName, password, pm);
                return null;
            }
        });
    }

    public void logout() {
        authentication = null;
    }

    public List<JIRAProject> getProjects() throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAProject>>() {
            public List<JIRAProject> call() throws Exception {
                Iterable<BasicProject> projects = restClient.getProjectClient().getAllProjects(pm);
                List<JIRAProject> result = Lists.newArrayList();
                for (BasicProject project : projects) {
                    Long id = project.getId();
                    result.add(new JIRAProjectBean(id != null ? id : -1, project.getKey(), project.getName()));
                }
                return result;
            }
        });
    }

    public List<JIRAConstant> getIssueTypes() throws RemoteApiException {
        return getIssueTypes(false);
    }

    public List<JIRAConstant> getSubtaskIssueTypes() throws RemoteApiException {
        return getIssueTypes(true);
    }

    private List<JIRAConstant> getIssueTypes(final boolean subtasks) throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAConstant>>() {
            public List<JIRAConstant> call() throws Exception {
                Iterable<IssueType> issueTypes = restClient.getMetadataClient().getIssueTypes(pm);
                List<JIRAConstant> result = Lists.newArrayList();
                for (IssueType type : issueTypes) {
                    Long id = type.getId();
                    if (type.isSubtask() != subtasks || id == null) {
                        continue;
                    }
                    result.add(new JIRAIssueTypeBean(id, type.getName(), type.getIconUri().toURL()));
                }
                return result;
            }
        });
    }

    public List<JIRAConstant> getIssueTypesForProject(long projectId, String projectKey) throws RemoteApiException {
        return getIssueTypesForProject(projectKey, false);
    }

    public List<JIRAConstant> getSubtaskIssueTypesForProject(long projectId, String projectKey) throws RemoteApiException {
        return getIssueTypesForProject(projectKey, true);
    }

    private List<JIRAConstant> getIssueTypesForProject(final String projectKey, final boolean subtasks) throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAConstant>>() {
            public List<JIRAConstant> call() throws Exception {
                OptionalIterable<IssueType> issueTypes = restClient.getProjectClient().getProject(projectKey, pm).getIssueTypes();
                List<JIRAConstant> result = Lists.newArrayList();
                for (IssueType issueType : issueTypes) {
                    if (subtasks != issueType.isSubtask()) {
						continue;
					}
                    Long id = issueType.getId();
                    result.add(new JIRAIssueTypeBean(id != null ? id : -1, issueType.getName(), issueType.getIconUri().toURL()));
                }
                return result;
            }
        });
    }

    public List<JIRAConstant> getStatuses() throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAConstant>>() {
            public List<JIRAConstant> call() throws Exception {
                Iterable<Status> statuses = restClient.getMetadataClient().getStatuses(pm);
                List<JIRAConstant> result = Lists.newArrayList();
                for (Status status : statuses) {
                    Long id = status.getId();
                    result.add(new JIRAStatusBean(id != null ? id : -1, status.getName(), status.getIconUrl().toURL()));
                }
                return result;
            }
        });
    }

    public List<JIRAComponentBean> getComponents(final String projectKey) throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAComponentBean>>() {
            public List<JIRAComponentBean> call() throws Exception {
                Iterable<BasicComponent> components = restClient.getProjectClient().getProject(projectKey, pm).getComponents();
                List<JIRAComponentBean> result = Lists.newArrayList();
                for (BasicComponent component : components) {
                    Long id = component.getId();
                    result.add(new JIRAComponentBean(id != null ? id : -1, component.getName()));
                }
                return result;
            }
        });
    }

    public List<JIRAVersionBean> getVersions(final String projectKey) throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAVersionBean>>() {
            public List<JIRAVersionBean> call() throws Exception {
                Iterable<Version> versions = restClient.getProjectClient().getProject(projectKey, pm).getVersions();
                List<JIRAVersionBean> result = Lists.newArrayList();
                for (Version version : versions) {
                    Long id = version.getId();
                    result.add(new JIRAVersionBean(id != null ? id : -1, version.getName(), version.isReleased()));
                }
                return result;
            }
        });
    }

    public List<JIRAPriorityBean> getPriorities() throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAPriorityBean>>() {
            public List<JIRAPriorityBean> call() throws Exception {
                Iterable<Priority> priorities = restClient.getMetadataClient().getPriorities(pm);
                List<JIRAPriorityBean> result = Lists.newArrayList();
                int order = 0;
                for (Priority priority : priorities) {
                    Long id = priority.getId();
                    result.add(new JIRAPriorityBean(id != null ? id : -1, order++, priority.getName(), priority.getIconUri().toURL()));
                }
                return result;
            }
        });
    }

    public List<JIRAResolutionBean> getResolutions() throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAResolutionBean>>() {
            public List<JIRAResolutionBean> call() throws Exception {
                Iterable<Resolution> resolutions = restClient.getMetadataClient().getResolutions(pm);
                List<JIRAResolutionBean> result = Lists.newArrayList();
                for (Resolution status : resolutions) {
                    Long id = status.getId();
                    result.add(new JIRAResolutionBean(id != null ? id : -1, status.getName()));
                }
                return result;
            }
        });
    }

    public List<JIRAQueryFragment> getSavedFilters() throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAQueryFragment>>() {
            public List<JIRAQueryFragment> call() throws Exception {
                Iterable<FavouriteFilter> filters = restClient.getSearchClient().getFavouriteFilters(pm);
                List<JIRAQueryFragment> result = Lists.newArrayList();
                for (FavouriteFilter filter : filters) {
                    Long id = filter.getId();
                    String jql = filter.getJql();
                    jql = jql != null ? jql.replace("\\\"", "\"") : "";
                    result.add(new JIRASavedFilterBean(filter.getName(), id != null ? id : -1, jql, filter.getSearchUrl(), filter.getViewUrl()));
                }
                return result;
            }
        });
    }

    public List<JIRAAction> getAvailableActions(final JIRAIssue issue) throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAAction>>() {
            public List<JIRAAction> call() throws Exception {
                List<JIRAAction> result = Lists.newArrayList();
                Iterable<Transition> transitions = restClient.getIssueClient().getTransitions((Issue) issue.getApiIssueObject(), pm);
                for (Transition transition : transitions) {
                    result.add(new JIRAActionBean(transition.getId(), transition.getName()));
                }
                return result;
            }
        });
    }

    public List<JIRAActionField> getFieldsForAction(final JIRAIssue issue, final JIRAAction action) throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAActionField>>() {
            public List<JIRAActionField> call() throws Exception {
                Iterable<Transition> transitions = restClient.getIssueClient().getTransitions((Issue) issue.getApiIssueObject(), pm);
                List<JIRAActionField> result = Lists.newArrayList();
                for (Transition transition : transitions) {
                    if (transition.getId() != action.getId()) {
                        continue;
                    }
                    for (Transition.Field field : transition.getFields()) {
                        JIRAActionFieldBean f = new JIRAActionFieldBean(field.getId(), field.getName());
                        result.add(f);
                    }
                    break;
                }
                return result;
            }
        });
    }

    public void progressWorkflowAction(final JIRAIssue issue, final JIRAAction action, final List<JIRAActionField> fields) throws RemoteApiException {
        final List<FieldInput> fieldValues = Lists.newArrayList();
        if (fields == null || fields.size() == 0) {
            wrapWithRemoteApiException(new Callable<Object>() {
                public Object call() throws Exception {
                    TransitionInput t = new TransitionInput((int) action.getId(), fieldValues);
                    restClient.getIssueClient().transition((Issue) issue.getApiIssueObject(), t, pm);
                    return null;
                }
            });
        } else {
            wrapWithRemoteApiException(new Callable<Object>() {
                public Object call() throws Exception {
                    Issue iszju = restClient.getIssueClient().getIssue(issue.getKey(), ImmutableList.of(IssueRestClient.Expandos.EDITMETA), pm);
                    fieldValues.addAll(generateFieldValues(issue, iszju, fields));
                    TransitionInput t = new TransitionInput((int) action.getId(), fieldValues);
                    restClient.getIssueClient().transition((Issue) issue.getApiIssueObject(), t, pm);
                    return null;
                }
            });
        }
    }

    public void setField(JIRAIssue issue, String fieldId, String value) throws RemoteApiException {
        JIRAActionFieldBean f = new JIRAActionFieldBean(fieldId, null);
        f.addValue(value);
        setFields(issue, ImmutableList.of((JIRAActionField) f));
    }

    public void setField(JIRAIssue issue, String fieldId, String[] values) throws RemoteApiException {
        JIRAActionFieldBean f = new JIRAActionFieldBean(fieldId, null);
        for (String value : values) {
            f.addValue(value);
        }
        setFields(issue, ImmutableList.of((JIRAActionField) f));
    }

    public void setFields(final JIRAIssue issue, final List<JIRAActionField> fields) throws RemoteApiException {
        wrapWithRemoteApiException(new Callable<Object>() {
            public Object call() throws Exception {
                Issue iszju = restClient.getIssueClient().getIssue(issue.getKey(), ImmutableList.of(IssueRestClient.Expandos.EDITMETA), pm);
                restClient.getIssueClient().update(iszju, generateFieldValues(issue, iszju, fields), pm);
                return null;
            }
        });
    }

    private Collection<FieldInput> generateFieldValues(final JIRAIssue issue, final Issue iszju, List<JIRAActionField> fieldValues) throws RemoteApiException {
        JSONObject editmeta = JsonParseUtil.getOptionalJsonObject(iszju.getRawObject(), "editmeta");
        if (editmeta == null) {
            throw new RemoteApiException("Unable to retrieve issue's editmeta information");
        }
        JSONObject fields = JsonParseUtil.getOptionalJsonObject(editmeta, "fields");
        try {
            if (fields != null) {
                List<FieldInput> result = Lists.newArrayList();
                for (JIRAActionField field : fieldValues) {
                    JSONObject fieldDef = JsonParseUtil.getOptionalJsonObject(fields, field.getFieldId());
                    FieldInput fieldInput = null;
                    if (fieldDef != null) {
                        fieldInput = field.generateFieldValue(issue, fieldDef);
                    } else if (field.getFieldId().equals("resolution")) {
                        fieldInput = new FieldInput("resolution", new ComplexIssueInputFieldValue(ImmutableMap.of("id", (Object) field.getValues().get(0))));
                    }
                    if (fieldInput != null) {
                        result.add(fieldInput);
                    }
                }
                return result;
            }
        } catch (JSONException e) {
            throw new RemoteApiException("Unable to generate field values", e);
        }
        return null;
    }

    public JIRAUserBean getUser(final String loginName) throws RemoteApiException, JiraUserNotFoundException {
        return wrapWithRemoteApiException(new Callable<JIRAUserBean>() {
            public JIRAUserBean call() throws Exception {
                User user = restClient.getUserClient().getUser(loginName, pm);
                return new JIRAUserBean(-1, user.getDisplayName(), user.getName()) {
                    @Override
					public String getQueryStringFragment() {
                        return null;
                    }

                    public JIRAQueryFragment getClone() {
                        return null;
                    }
                };
            }
        });
    }

    public List<JIRAComment> getComments(final JIRAIssue issue) throws RemoteApiException {
        if (issue.getComments() == null) {
            return Lists.newArrayList();
        }
        return issue.getComments();
    }

    public Collection<JIRAAttachment> getIssueAttachements(final JIRAIssue issue) throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<Collection<JIRAAttachment>>() {
            public Collection<JIRAAttachment> call() throws Exception {
//                Issue iszju = (Issue) issue.getApiIssueObject();
                Issue iszju = restClient.getIssueClient().getIssue(issue.getKey(), pm);
                List<JIRAAttachment> result = Lists.newArrayList();
                Iterable<Attachment> attachments = iszju.getAttachments();
                if (attachments != null) {
                    for (Attachment attachment : attachments) {
                        Long id = attachment.getId();
                        JIRAAttachment a = new JIRAAttachment(
                            id != null ? id.toString() : "-1", attachment.getAuthor().getName(), attachment.getFilename(),
                            attachment.getSize(), attachment.getMimeType(), attachment.getCreationDate().toGregorianCalendar());
                        result.add(a);
                    }
                }
                return result;
            }
        });
    }

    public List<JIRASecurityLevelBean> getSecurityLevels(final String projectKey) throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRASecurityLevelBean>>() {
            public List<JIRASecurityLevelBean> call() throws Exception {
                GetCreateIssueMetadataOptionsBuilder builder = new GetCreateIssueMetadataOptionsBuilder();
                builder.withExpandedIssueTypesFields().withProjectKeys(projectKey);
                Iterable<CimProject> metadata = restClient.getIssueClient().getCreateIssueMetadata(builder.build(), pm);
                if (metadata == null || !metadata.iterator().hasNext()) {
                    return Lists.newArrayList();
                }
                CimProject project = metadata.iterator().next();
                Map<Long, JIRASecurityLevelBean> levels = Maps.newHashMap();
                for (CimIssueType type : project.getIssueTypes()) {
                    Map<String, CimFieldInfo> fields = type.getFields();
                    CimFieldInfo security = fields.get("security");
                    if (security != null) {
                        Iterable<Object> allowedValues = security.getAllowedValues();
                        if (allowedValues == null) {
                            continue;
                        }
                        for (Object lvl : allowedValues) {
                            SecurityLevel secLevel = (SecurityLevel) lvl;
                            Long id = secLevel.getId();
                            if (!levels.containsKey(id)) {
                                levels.put(id, new JIRASecurityLevelBean(id, secLevel.getName()));
                            }
                        }
                    }
                }
                return Lists.newArrayList(levels.values());
            }
        });
    }

    public List<JIRAProject> getProjectsForIssueCreation() throws RemoteApiException {
        return wrapWithRemoteApiException(new Callable<List<JIRAProject>>() {
            public List<JIRAProject> call() throws Exception {
                GetCreateIssueMetadataOptionsBuilder builder = new GetCreateIssueMetadataOptionsBuilder();
                Iterable<CimProject> metadata = restClient.getIssueClient().getCreateIssueMetadata(builder.build(), pm);
                if (metadata == null || !metadata.iterator().hasNext()) {
                    return Lists.newArrayList();
                }
                List<JIRAProject> result = Lists.newArrayList();
                for (CimProject cimProject : metadata) {
                    Long id = cimProject.getId();
                    if (id == null) {
                        continue;
                    }
                    JIRAProject p = new JIRAProjectBean(id, cimProject.getKey(), cimProject.getName());
                    result.add(p);
                }
                return result;
            }
        });
    }

    public List<JIRAIssue> getIssues(
            JiraFilter filter, String sortBy, String sortOrder, int start, int max) throws JIRAException {
        return getIssues(filter.getJql(), sortBy, sortOrder, start, max);
    }

    public List<JIRAIssue> getSavedFilterIssues(
            JIRASavedFilter filter, String sortBy, String sortOrder, int start, int max) throws JIRAException {
        return getIssues(filter.getJql(), sortBy, sortOrder, start, max);
    }

    public List<JIRAIssue> getIssues(
            final String jql, final String sortBy, final String sortOrder, final int start, final int max)
            throws JIRAException {
        return wrapWithJiraException(new Callable<List<JIRAIssue>>() {
            public List<JIRAIssue> call() throws Exception {
                String sort =
                        jql.toLowerCase().contains("order by")
                            ? ""
                            : (StringUtils.isNotEmpty(sortBy) && StringUtils.isNotEmpty(sortOrder)
                                ? " order by " + sortBy + " " + sortOrder
                                : "");
                SearchResult result = restClient.getSearchClient().searchJqlWithFullIssues(jql + sort, max, start, pm);
                List<JIRAIssue> list = Lists.newArrayList();
                for (BasicIssue issue : result.getIssues()) {
                    JIRAIssueBean bean = new JIRAIssueBean(server.getUrl(), (Issue) issue);
                    list.add(bean);
                }
                return list;
            }
        });
    }

    public JIRAIssue getIssue(final String issueKey) throws JIRAException {
        return wrapWithJiraException(new Callable<JIRAIssue>() {
            public JIRAIssue call() throws Exception {
                Issue issue = restClient.getIssueClient().getIssue(issueKey,
                        ImmutableList.of(IssueRestClient.Expandos.RENDERED_FIELDS, IssueRestClient.Expandos.EDITMETA), pm);
                return new JIRAIssueBean(server.getUrl(), issue);
            }
        });
    }

    public JIRAIssue getIssueDetails(JIRAIssue issue) throws RemoteApiException {
        try {
            return getIssue(issue.getKey());
        } catch (JIRAException e) {
            throw new RemoteApiException(e);
        }
    }

    public void logWork(
            final JIRAIssue issue, final String timeSpent, final Calendar startDate, final String comment,
            final boolean updateEstimate, final String newEstimate) throws RemoteApiException {
        wrapWithRemoteApiException(new Callable<Object>() {
            public Object call() throws Exception {
                Issue iszju = restClient.getIssueClient().getIssue(issue.getKey(), pm);
                WorklogInputBuilder builder = new WorklogInputBuilder(iszju.getSelf());
                builder.setStartDate(new DateTime(startDate));
                builder.setTimeSpent(timeSpent);
                if (updateEstimate) {
                    if (newEstimate != null) {
                        builder.setAdjustEstimateNew(newEstimate);
                    } else {
                        builder.setAdjustEstimateAuto();
                    }
                } else {
                    builder.setAdjustEstimateLeave();
                }
                builder.setComment(comment);
                restClient.getIssueClient().addWorklog(iszju.getWorklogUri(), builder.build(), pm);
                return null;
            }
        });
    }

    public void addComment(final String issueKey, final String comment) throws RemoteApiException {
        wrapWithRemoteApiException(new Callable<Object>() {
            public Object call() throws Exception {
                Issue issue = restClient.getIssueClient().getIssue(issueKey, pm);
                restClient.getIssueClient().addComment(pm, issue.getCommentsUri(), Comment.valueOf(comment));
                return null;
            }
        });
    }

    public void addAttachment(final String issueKey, final String name, final byte[] content) throws RemoteApiException {
        wrapWithRemoteApiException(new Callable<Object>() {
            public Object call() throws Exception {
                Issue issue = restClient.getIssueClient().getIssue(issueKey, pm);
                restClient.getIssueClient().addAttachment(pm, issue.getAttachmentsUri(), new ByteArrayInputStream(content), name);
                return null;
            }
        });
    }

    public JIRAIssue createIssue(final JIRAIssue issue) throws RemoteApiException {
        return createIssueOrSubtask(null, issue);
    }

    public JIRAIssue createSubtask(JIRAIssue parent, JIRAIssue issue) throws RemoteApiException {
        return createIssueOrSubtask(parent, issue);
    }

    private JIRAIssue createIssueOrSubtask(final JIRAIssue parent, final JIRAIssue issue) throws RemoteApiException {
        final BasicIssue newIssue = wrapWithRemoteApiException(new Callable<BasicIssue>() {
            public BasicIssue call() throws Exception {
                GetCreateIssueMetadataOptionsBuilder metaBuilder =
                    new GetCreateIssueMetadataOptionsBuilder()
                        .withProjectKeys(issue.getProjectKey())
                        .withIssueTypeIds(issue.getTypeConstant().getId())
                        .withExpandedIssueTypesFields();

                Iterable<CimProject> metadata = restClient.getIssueClient().getCreateIssueMetadata(metaBuilder.build(), pm);
                String message = "Invalid issue creation metadata";
                if (metadata == null) {
                    throw new RemoteApiException(message);
                }
                Optional<CimProject> cimProjectOptional = Iterables.tryFind(metadata, new Predicate<CimProject>() {
                    public boolean apply(CimProject input) {
                        return input != null && input.getKey().equals(issue.getProjectKey());
                    }
                });
                if (!cimProjectOptional.isPresent()) {
                    throw new RemoteApiException(message);
                }
                Optional<CimIssueType> issueTypeOptional = Iterables.tryFind(cimProjectOptional.get().getIssueTypes(), new Predicate<CimIssueType>() {
                    public boolean apply(CimIssueType input) {
                        return input != null && input.getId() == issue.getTypeConstant().getId();
                    }
                });
                if (!issueTypeOptional.isPresent()) {
                    throw new RemoteApiException(message);
                }
                CimIssueType typeMeta = issueTypeOptional.get();
                final IssueInputBuilder builder = new IssueInputBuilder(issue.getProjectKey(), issue.getTypeConstant().getId(), issue.getSummary());
                List<JIRAConstant> components = issue.getComponents();
                List<JIRAConstant> affectsVersions = issue.getAffectsVersions();
                List<JIRAConstant> fixVersions = issue.getFixVersions();
                if (has(typeMeta, IssueFieldId.COMPONENTS_FIELD) && components != null && components.size() > 0) {
                    List<String> comps = Lists.newArrayList();
                    for (JIRAConstant component : components) {
                        comps.add(component.getName());
                    }
                    builder.setComponentsNames(comps);
                }
                if (has(typeMeta, IssueFieldId.AFFECTS_VERSIONS_FIELD) && affectsVersions != null && affectsVersions.size() > 0) {
                    List<String> versions = Lists.newArrayList();
                    for (JIRAConstant version : affectsVersions) {
                        versions.add(version.getName());
                    }
                    builder.setAffectedVersionsNames(versions);
                }
                if (has (typeMeta, IssueFieldId.FIX_VERSIONS_FIELD) && fixVersions != null && fixVersions.size() > 0) {
                    List<String> versions = Lists.newArrayList();
                    for (JIRAConstant version : fixVersions) {
                        versions.add(version.getName());
                    }
                    builder.setFixVersionsNames(versions);
                }
                if (has(typeMeta, IssueFieldId.PRIORITY_FIELD)) {
                    builder.setPriorityId(issue.getPriorityConstant().getId());
                }
                if (has(typeMeta, IssueFieldId.DESCRIPTION_FIELD)) {
                    builder.setDescription(issue.getDescription());
                }
                if (has(typeMeta, IssueFieldId.ASSIGNEE_FIELD) && issue.getAssigneeId() != null) {
                    builder.setAssigneeName(issue.getAssigneeId());
                }
                String originalEstimate = issue.getOriginalEstimate();
                if (has(typeMeta, IssueFieldId.TIMETRACKING_FIELD) && originalEstimate != null && originalEstimate.length() > 0) {
                    builder.setFieldValue(IssueFieldId.TIMETRACKING_FIELD.id,
                        new ComplexIssueInputFieldValue(
                            ImmutableMap.of("originalEstimate", (Object) originalEstimate)));
                }
                JIRASecurityLevelBean securityLevel = issue.getSecurityLevel();
                if (securityLevel != null && securityLevel.getId() > 0) {
                    builder.setFieldValue("security",
                        new ComplexIssueInputFieldValue(
                            ImmutableMap.of("id", (Object) Long.valueOf(securityLevel.getId()).toString())));
                }
                if (parent != null) {
                    builder.setFieldValue("parent",
                        new ComplexIssueInputFieldValue(ImmutableMap.of("key", (Object) parent.getKey())));
                }
                return restClient.getIssueClient().createIssue(builder.build(), pm);
            }
        });
        return wrapWithRemoteApiException(new Callable<JIRAIssue>() {
            public JIRAIssue call() throws Exception {
                Issue issue = restClient.getIssueClient().getIssue(newIssue.getKey(),
                        ImmutableList.of(IssueRestClient.Expandos.RENDERED_FIELDS, IssueRestClient.Expandos.EDITMETA), pm);
                return new JIRAIssueBean(server.getUrl(), issue);
            }
        });
    }

    private boolean has(CimIssueType meta, IssueFieldId id) {
        return meta.getField(id) != null;
    }

    public void login() throws JIRAException, JiraCaptchaRequiredException {
        throw new JIRAException("Not implemented");
    }

    public boolean isLoggedIn(ConnectionCfg server) {
        // is this even used anywhere?
        return false;
    }

    public void testConnection() throws RemoteApiException {
        login(server.getUsername(), server.getPassword());
    }

    public boolean isLoggedIn() {
        return isLoggedIn(server);
    }

    private <T> T wrapWithJiraException(Callable<T> c) throws JIRAException {
        try {
            return doCall(c);
        } catch (RestClientException e) {
            if (e.getCause() instanceof UniformInterfaceException &&
                ((UniformInterfaceException) e.getCause()).getResponse().getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                // renew session and retry
                try {
                    authentication = restClient.getSessionClient().login(server.getUsername(), server.getPassword(), pm);
                    return doCall(c);
                } catch (Exception e1) {
                    throw new JIRAException(getConnectionCfgString() + "\n\n" + e1.getMessage(), e1);
                }
            }
            throw new JIRAException(getConnectionCfgString() + "\n\n" + e.getMessage(), e);
        } catch (Exception e) {
            throw new JIRAException(getConnectionCfgString() + "\n\n" + e.getMessage(), e);
        }
    }

    private <T> T wrapWithRemoteApiException(Callable<T> c) throws RemoteApiException {
        try {
            return doCall(c);
        } catch (RestClientException e) {
            if (e.getCause() instanceof UniformInterfaceException &&
                ((UniformInterfaceException) e.getCause()).getResponse().getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
                // renew session and retry
                try {
                    authentication = restClient.getSessionClient().login(server.getUsername(), server.getPassword(), pm);
                    return doCall(c);
                } catch (Exception e1) {
                    throw new RemoteApiException(getConnectionCfgString() + "\n\n" + e1.getMessage(), e1);
                }
            }
            throw new RemoteApiException(getConnectionCfgString() + "\n\n" + e.getMessage(), e);
        } catch (Exception e) {
            throw new RemoteApiException(getConnectionCfgString() + "\n\n" + e.getMessage(), e);
        }
    }

    private <T> T doCall(Callable<T> c) throws Exception {
        setSessionCookies();
        T res = c.call();
        getSessionCookies();
        return res;
    }

    private void setSessionCookies() {
        if (apacheClientConfig != null) {
            if (authentication == null) {
                authentication = restClient.getSessionClient().login(server.getUsername(), server.getPassword(), pm);
            }
            synchronized (this) {
                if (authentication != null) {
                    Cookie cookie = new Cookie();
                    cookie.setName(authentication.getSession().getName());
                    cookie.setValue(authentication.getSession().getValue());
                    cookie.setPath("/");
                    int idx = server.getUrl().indexOf("//");
                    String domain = idx > 0 ? server.getUrl().substring(idx + 2) : server.getUrl();
                    cookie.setDomain(domain);
                    apacheClientConfig.getState().getHttpState().addCookie(cookie);
                } else {
                    apacheClientConfig.getState().getHttpState().clearCookies();
                }
            }
        }
    }

    private synchronized void getSessionCookies() {
        if (apacheClientConfig == null) {
            return;
        }

        Cookie[] cookies = apacheClientConfig.getState().getHttpState().getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().toLowerCase().equals("jsessionid")) {
                authentication = new Authentication(null, new SessionCookie(cookie.getName(), cookie.getValue()));
            }
        }
    }

    private void setupApacheClient(ApacheHttpClientConfig config) {
        apacheClientConfig = config;
        if (proxyInfo != null && proxyInfo.isUseHttpProxy() && proxyInfo.isProxyAuthentication()) {
            config.getState().setProxyCredentials(AuthScope.ANY_REALM, proxyInfo.getProxyHost(),
                    proxyInfo.getProxyPort(), proxyInfo.getProxyLogin(), proxyInfo.getPlainProxyPassword());
        }
    }

    private String getConnectionCfgString() {
        return notNullString(server.getUsername()) +
                (server.getPassword() != null
                    ? ":[password " + server.getPassword().length() + " chars]"
                    : "<null>"
                )
                + "@" + server.getUrl();
    }

    private static String notNullString(String str) {
        if (str == null) {
            return "<null>";
        }
        return str;
    }
}
