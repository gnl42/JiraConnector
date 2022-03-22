package me.glindholm.connector.commons.jira;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.connector.commons.jira.beans.JIRAAttachment;
import me.glindholm.connector.commons.jira.beans.JIRAComment;
import me.glindholm.connector.commons.jira.beans.JIRAComponentBean;
import me.glindholm.connector.commons.jira.beans.JIRAConstant;
import me.glindholm.connector.commons.jira.beans.JIRAPriorityBean;
import me.glindholm.connector.commons.jira.beans.JIRAProject;
import me.glindholm.connector.commons.jira.beans.JIRAQueryFragment;
import me.glindholm.connector.commons.jira.beans.JIRAResolutionBean;
import me.glindholm.connector.commons.jira.beans.JIRASavedFilter;
import me.glindholm.connector.commons.jira.beans.JIRASecurityLevelBean;
import me.glindholm.connector.commons.jira.beans.JIRAUserBean;
import me.glindholm.connector.commons.jira.beans.JIRAVersionBean;
import me.glindholm.connector.commons.jira.beans.JiraFilter;
import me.glindholm.connector.commons.jira.rest.JiraRestSessionImpl;
import me.glindholm.connector.commons.jira.rss.JIRAException;
import me.glindholm.theplugin.commons.ServerType;
import me.glindholm.theplugin.commons.configuration.ConfigurationFactory;
import me.glindholm.theplugin.commons.jira.JiraServerData;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;
import me.glindholm.theplugin.commons.util.HttpConfigurableAdapter;
import me.glindholm.theplugin.commons.util.Logger;

import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * User: kalamon
 * Date: 14.11.12
 * Time: 15:32
 */
public class JiraRESTFacade2Impl implements JIRAServerFacade2, JiraRESTSupportTester {
    private static Logger logger;

    private final Map<ConnectionCfg, JiraRestSessionImpl> sessions = new HashMap<ConnectionCfg, JiraRestSessionImpl>();

    public void reset() {
        sessions.clear();
    }

    public boolean usesRest(JiraServerData jiraServerData) {
        try {
            return get(jiraServerData).supportsRest();
        } catch (JIRAException e) {
            return false;
        }
    }

    public static void setLogger(Logger logger) {
        JiraRESTFacade2Impl.logger = logger;
    }

    public boolean supportsRest(ConnectionCfg server) throws JIRAException {
        JiraRestSessionImpl session = get(server);
        return session.supportsRest();
    }

    public List<JIRAIssue> getIssues(ConnectionCfg server, JiraFilter filter, String sort, String sortOrder, int start, int size) throws JIRAException {
        JiraRestSessionImpl session = get(server);
        return session.getIssues(filter, sort, sortOrder, start, size);
    }

    public List<JIRAIssue> getIssues(JiraServerData server, String query, String sort, String sortOrder, int start, int size) throws JIRAException {
        JiraRestSessionImpl session = get(server);
        return session.getIssues(query, sort, sortOrder, start, size);
    }

    public List<JIRAIssue> getSavedFilterIssues(ConnectionCfg server, JIRASavedFilter filter, String sort, String sortOrder, int start, int size) throws JIRAException {
        JiraRestSessionImpl session = get(server);
        return session.getSavedFilterIssues(filter, sort, sortOrder, start, size);
    }

    public List<JIRAProject> getProjects(final ConnectionCfg server) throws JIRAException {
        return withJiraException(new Callable<List<JIRAProject>>() {
            public List<JIRAProject> call() throws Exception {
                return get(server).getProjects();
            }
        });
    }

    public List<JIRAProject> getProjectsForIssueCreation(final ConnectionCfg server) throws JIRAException {
        return withJiraException(new Callable<List<JIRAProject>>() {
            public List<JIRAProject> call() throws Exception {
                return get(server).getProjectsForIssueCreation();
            }
        });
    }

    public List<JIRAConstant> getStatuses(final ConnectionCfg server) throws JIRAException {
        return withJiraException(new Callable<List<JIRAConstant>>() {
            public List<JIRAConstant> call() throws Exception {
                return get(server).getStatuses();
            }
        });
    }

    public List<JIRAConstant> getIssueTypes(final ConnectionCfg server) throws JIRAException {
        return withJiraException(new Callable<List<JIRAConstant>>() {
            public List<JIRAConstant> call() throws Exception {
                return get(server).getIssueTypes();
            }
        });
    }

    public List<JIRAConstant> getIssueTypesForProject(
            final ConnectionCfg server, final long projectId, final String projectKey) throws JIRAException {
        return withJiraException(new Callable<List<JIRAConstant>>() {
            public List<JIRAConstant> call() throws Exception {
                return get(server).getIssueTypesForProject(projectId, projectKey);
            }
        });
    }

    public List<JIRAConstant> getSubtaskIssueTypes(final ConnectionCfg server) throws JIRAException {
        return withJiraException(new Callable<List<JIRAConstant>>() {
            public List<JIRAConstant> call() throws Exception {
                return get(server).getSubtaskIssueTypes();
            }
        });
    }

    public List<JIRAConstant> getSubtaskIssueTypesForProject(
            final ConnectionCfg server, final long projectId, final String projectKey) throws JIRAException {
        return withJiraException(new Callable<List<JIRAConstant>>() {
            public List<JIRAConstant> call() throws Exception {
                return get(server).getSubtaskIssueTypesForProject(projectId, projectKey);
            }
        });
    }

    public List<JIRAQueryFragment> getSavedFilters(final ConnectionCfg server) throws JIRAException {
        return withJiraException(new Callable<List<JIRAQueryFragment>>() {
            public List<JIRAQueryFragment> call() throws Exception {
                return get(server).getSavedFilters();
            }
        });
    }

    public List<JIRAComponentBean> getComponents(final ConnectionCfg server, final String projectKey) throws JIRAException {
        return withJiraException(new Callable<List<JIRAComponentBean>>() {
            public List<JIRAComponentBean> call() throws Exception {
                return get(server).getComponents(projectKey);
            }
        });
    }

    public List<JIRAVersionBean> getVersions(final ConnectionCfg server, final String projectKey) throws JIRAException {
        return withJiraException(new Callable<List<JIRAVersionBean>>() {
            public List<JIRAVersionBean> call() throws Exception {
                return get(server).getVersions(projectKey);
            }
        });
    }

    public List<JIRAPriorityBean> getPriorities(final ConnectionCfg server) throws JIRAException {
        return withJiraException(new Callable<List<JIRAPriorityBean>>() {
            public List<JIRAPriorityBean> call() throws Exception {
                return get(server).getPriorities();
            }
        });
    }

    public List<JIRAResolutionBean> getResolutions(final ConnectionCfg server) throws JIRAException {
        return withJiraException(new Callable<List<JIRAResolutionBean>>() {
            public List<JIRAResolutionBean> call() throws Exception {
                return get(server).getResolutions();
            }
        });
    }

    public List<JIRAAction> getAvailableActions(final ConnectionCfg server, final JIRAIssue issue) throws JIRAException {
        return withJiraException(new Callable<List<JIRAAction>>() {
            public List<JIRAAction> call() throws Exception {
                return get(server).getAvailableActions(issue);
            }
        });
    }

    public List<JIRAActionField> getFieldsForAction(final ConnectionCfg server, final JIRAIssue issue, final JIRAAction action) throws JIRAException {
        return withJiraException(new Callable<List<JIRAActionField>>() {
            public List<JIRAActionField> call() throws Exception {
                return get(server).getFieldsForAction(issue, action);
            }
        });
    }

    public void progressWorkflowAction(final ConnectionCfg server, final JIRAIssue issue, final JIRAAction action) throws JIRAException {
        withJiraException(new Callable<Object>() {
            public Object call() throws Exception {
                get(server).progressWorkflowAction(issue, action, null);
                return null;
            }
        });
    }

    public void progressWorkflowAction(final ConnectionCfg server, final JIRAIssue issue, final JIRAAction action, final List<JIRAActionField> fields) throws JIRAException {
        withJiraException(new Callable<Object>() {
            public Object call() throws Exception {
                get(server).progressWorkflowAction(issue, action, fields);
                return null;
            }
        });
    }

    public void addComment(final ConnectionCfg server, final String issueKey, final String comment) throws JIRAException {
        withJiraException(new Callable<Object>() {
            public Object call() throws Exception {
                get(server).addComment(issueKey, comment);
                return null;
            }
        });
    }

    public void addAttachment(final ConnectionCfg server, final String issueKey, final String name, final byte[] content) throws JIRAException {
        withJiraException(new Callable<Object>() {
            public Object call() throws Exception {
                get(server).addAttachment(issueKey, name, content);
                return null;
            }
        });
    }

    public JIRAIssue createIssue(final ConnectionCfg server, final JIRAIssue issue) throws JIRAException {
        return withJiraException(new Callable<JIRAIssue>() {
            public JIRAIssue call() throws Exception {
                return get(server).createIssue(issue);
            }
        });
    }

    public JIRAIssue createSubtask(final JiraServerData server, final JIRAIssue parent, final JIRAIssue issue) throws JIRAException {
        return withJiraException(new Callable<JIRAIssue>() {
            public JIRAIssue call() throws Exception {
                return get(server).createSubtask(parent, issue);
            }
        });
    }

    public JIRAIssue getIssue(final ConnectionCfg server, final String key) throws JIRAException {
        return get(server).getIssue(key);
    }

    public JIRAIssue getIssueDetails(final ConnectionCfg server, final JIRAIssue issue) throws JIRAException {
        return withJiraException(new Callable<JIRAIssue>() {
            public JIRAIssue call() throws Exception {
                return get(server).getIssueDetails(issue);
            }
        });
    }

    public void logWork(
            final ConnectionCfg server, final JIRAIssue issue, final String timeSpent, final Calendar startDate,
            final String comment, final boolean updateEstimate, final String newEstimate) throws JIRAException {
        withJiraException(new Callable<Object>() {
            public Object call() throws Exception {
                get(server).logWork(issue, timeSpent, startDate, comment, updateEstimate, newEstimate);
                return null;
            }
        });
    }

    public void setField(final ConnectionCfg server, final JIRAIssue issue, final String fieldId, final String value) throws JIRAException {
        withJiraException(new Callable<Object>() {
            public Object call() throws Exception {
                get(server).setField(issue, fieldId, value);
                return null;
            }
        });
    }

    public void setField(final ConnectionCfg server, final JIRAIssue issue, final String fieldId, final String[] values) throws JIRAException {
        withJiraException(new Callable<Object>() {
            public Object call() throws Exception {
                get(server).setField(issue, fieldId, values);
                return null;
            }
        });
    }

    public void setFields(final ConnectionCfg server, final JIRAIssue issue, final List<JIRAActionField> fields) throws JIRAException {
        withJiraException(new Callable<Object>() {
            public Object call() throws Exception {
                get(server).setFields(issue, fields);
                return null;
            }
        });
    }

    public JIRAUserBean getUser(final ConnectionCfg server, final String loginName) throws JIRAException, JiraUserNotFoundException {
        return withJiraException(new Callable<JIRAUserBean>() {
            public JIRAUserBean call() throws Exception {
                return get(server).getUser(loginName);
            }
        });
    }

    public List<JIRAComment> getComments(final ConnectionCfg server, final JIRAIssue issue) throws JIRAException {
        return withJiraException(new Callable<List<JIRAComment>>() {
            public List<JIRAComment> call() throws Exception {
                return get(server).getComments(issue);
            }
        });
    }

    public Collection<JIRAAttachment> getIssueAttachements(final ConnectionCfg server, final JIRAIssue issue) throws JIRAException {
        return withJiraException(new Callable<Collection<JIRAAttachment>>() {
            public Collection<JIRAAttachment> call() throws Exception {
                return get(server).getIssueAttachements(issue);
            }
        });
    }

    public List<JIRASecurityLevelBean> getSecurityLevels(final ConnectionCfg server, final String projectKey) throws JIRAException {
        return withJiraException(new Callable<List<JIRASecurityLevelBean>>() {
            public List<JIRASecurityLevelBean> call() throws Exception {
                return get(server).getSecurityLevels(projectKey);
            }
        });
    }

    public void testServerConnection(final ConnectionCfg server) throws RemoteApiException {
        try {
            get(server).testConnection();
        } catch (JIRAException e) {
            throw new RemoteApiException(e);
        }
    }

    public ServerType getServerType() {
        return ServerType.JIRA_SERVER;
    }

    private JiraRestSessionImpl get(ConnectionCfg connectionCfg) throws JIRAException {

        JiraRestSessionImpl session = sessions.get(connectionCfg);
        if (session == null) {
            boolean useIdeaProxySettings =
                    ConfigurationFactory.getConfiguration().getGeneralConfigurationData().getUseIdeaProxySettings();
            HttpConfigurableAdapter proxyInfo = ConfigurationFactory.getConfiguration().transientGetHttpConfigurable();

            try {
                session = new JiraRestSessionImpl(connectionCfg, useIdeaProxySettings ? proxyInfo : null);
            } catch (URISyntaxException e) {
                throw new JIRAException(e.getMessage());
            }
            sessions.put(connectionCfg, session);
        }
        return session;
    }

    private <T> T withJiraException(Callable<T> callable) throws JIRAException {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new JIRAException(e.getMessage(), e);
        }
    }
}
