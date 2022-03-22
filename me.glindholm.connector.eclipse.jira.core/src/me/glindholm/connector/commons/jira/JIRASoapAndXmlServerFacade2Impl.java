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
import me.glindholm.connector.commons.jira.rss.JIRAException;
import me.glindholm.connector.commons.jira.rss.JIRARssClient;
import me.glindholm.connector.commons.jira.rss.JiraRssAutoRenewClient;
import me.glindholm.connector.commons.jira.soap.AxisSessionCallback;
import me.glindholm.connector.commons.jira.soap.JIRASoapSessionImpl;
import me.glindholm.theplugin.commons.ServerType;
import me.glindholm.theplugin.commons.jira.JiraServerData;
import me.glindholm.theplugin.commons.remoteapi.CaptchaRequiredException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiLoginException;
import me.glindholm.theplugin.commons.remoteapi.jira.JiraCaptchaRequiredException;
import me.glindholm.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import me.glindholm.theplugin.commons.util.Logger;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JIRASoapAndXmlServerFacade2Impl implements JIRAServerFacade2 {

    private final HttpSessionCallback callback;
    private final AxisSessionCallback axisCallback;
    private static Logger logger;

    private final Map<String, JiraRssAutoRenewClient> rssSessions = new HashMap<String, JiraRssAutoRenewClient>();
    private final Map<String, JIRASessionPartOne> soapSessions = new HashMap<String, JIRASessionPartOne>();

    private static final long ONE_MINUTE = 1 * 60 * 1000;

    private String getSessionKey(ConnectionCfg httpConnectionCfg) {
        return httpConnectionCfg.getUrl() + "_" + httpConnectionCfg.getUsername() + "_" + httpConnectionCfg.getPassword();
    }

    public JIRASoapAndXmlServerFacade2Impl(HttpSessionCallback callback, AxisSessionCallback axisCallback) {
        this.callback = callback;
        this.axisCallback = axisCallback;
    }

    public void reset() {
        rssSessions.clear();
        soapSessions.clear();
    }

    public boolean usesRest(JiraServerData jiraServerData) {
        return false;
    }

    public static void setLogger(Logger logger) {
        JIRASoapAndXmlServerFacade2Impl.logger = logger;
    }

    private synchronized JIRASessionPartOne getSoapSession(ConnectionCfg connectionCfg) throws RemoteApiException {
        String key = getSessionKey(connectionCfg);

        JIRASessionPartOne session = soapSessions.get(key);
        if (session == null
                || (((JIRASoapSessionImpl) session).getLastUsed().getTime() < new Date().getTime() - ONE_MINUTE)) {
            if (session != null) {
                soapSessions.remove(key);
            }

            try {
                session = new JIRASoapSessionImpl(logger, connectionCfg, axisCallback);
            } catch (MalformedURLException e) {
                throw new RemoteApiException(e);
            } catch (ServiceException e) {
                throw new RemoteApiException(e);
            }

            session.login(connectionCfg.getUsername(), connectionCfg.getPassword());
            soapSessions.put(key, session);
        }
        return session;
    }

    private synchronized JiraRssAutoRenewClient getRssSession(ConnectionCfg server) throws RemoteApiException {
        String key = getSessionKey(server);

        JiraRssAutoRenewClient session = rssSessions.get(key);

        // sessions should time out after 5 mins to avoid silent session drop on JIRA side

        if (session == null || (session.getLastUsed().getTime() < new Date().getTime() - ONE_MINUTE)) {


            JIRARssClient client = new JIRARssClient(server, callback);

            try {
                callback.disposeClient(server);
                client.login();
            } catch (JiraCaptchaRequiredException e) {
                throw new CaptchaRequiredException(e);
            } catch (JIRAException e) {
                throw new RemoteApiException(e);
            }

            session = new JiraRssAutoRenewClient(client);
            rssSessions.put(key, session);
        }
        session.setLastUsed(new Date());
        return session;
    }

    private synchronized void removeRssSession(ConnectionCfg server) {
        rssSessions.remove(getSessionKey(server));
    }

    public void testServerConnection(ConnectionCfg httpConnectionCfg)
            throws RemoteApiException {
        JIRASessionPartOne session;
        try {
            session = new JIRASoapSessionImpl(logger, httpConnectionCfg, axisCallback);
        } catch (MalformedURLException e) {
            throw new RemoteApiException(e);
        } catch (ServiceException e) {
            throw new RemoteApiLoginException(e.getMessage(), e);
        }
        session.login(httpConnectionCfg.getUsername(), httpConnectionCfg.getPassword());
    }

    public ServerType getServerType() {
        return ServerType.JIRA_SERVER;
    }

    public List<JIRAIssue> getIssues(ConnectionCfg httpConnectionCfg, JiraFilter filter, String sort, String sortOrder, int start, int size) throws JIRAException {
        try {
            JiraRssAutoRenewClient rss = getRssSession(httpConnectionCfg);
            return rss.getIssues(filter, sort, sortOrder, start, size);
        } catch (CaptchaRequiredException e) {
            removeRssSession(httpConnectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            removeRssSession(httpConnectionCfg);
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAIssue> getIssues(JiraServerData server, String query, String sort, String sortOrder, int start, int size) throws JIRAException {
        throw new JIRAException("Not implemented");
    }

    //    public List<JIRAIssue> getIssues(ConnectionCfg httpConnectionCfg, String queryString, String sort,
//                                     String sortOrder, int start, int size) throws JIRAException {
//        try {
//            JiraRssAutoRenewClient rss = getRssSession(httpConnectionCfg);
//            return rss.getIssues(queryString, sort, sortOrder, start, size);
//        } catch (CaptchaRequiredException e) {
//            removeRssSession(httpConnectionCfg);
//            throw new JiraCaptchaRequiredException(e.getMessage());
//        } catch (RemoteApiException e) {
//            removeRssSession(httpConnectionCfg);
//            throw new JIRAException(e.getMessage(), e);
//        }
//    }

//    public List<JIRAIssue> getIssues(ConnectionCfg httpConnectionCfg,
//                                     List<JIRAQueryFragment> query,
//                                     String sort,
//                                     String sortOrder,
//                                     int start,
//                                     int size) throws JIRAException {
//        try {
//            JiraRssAutoRenewClient rss = getRssSession(httpConnectionCfg);
//            return rss.getIssues(query, sort, sortOrder, start, size);
//        } catch (CaptchaRequiredException e) {
//            removeRssSession(httpConnectionCfg);
//            throw new JiraCaptchaRequiredException(e.getMessage());
//        } catch (RemoteApiException e) {
//            removeRssSession(httpConnectionCfg);
//            throw new JIRAException(e.getMessage(), e);
//        }
//    }

    public List<JIRAIssue> getSavedFilterIssues(
            ConnectionCfg httpConnectionCfg, JIRASavedFilter filter, String sort, String sortOrder, int start, int size) throws JIRAException {

//    public List<JIRAIssue> getSavedFilterIssues(ConnectionCfg httpConnectionCfg,
//                                                List<JIRAQueryFragment> query,
//                                                String sort,
//                                                String sortOrder,
//                                                int start,
//                                                int size) throws JIRAException {
        try {
            JiraRssAutoRenewClient rss = getRssSession(httpConnectionCfg);
//            if (query.size() != 1) {
//                throw new JIRAException("Only one saved filter could be used for query");
//            } else {
//                return rss.getSavedFilterIssues(query.get(0), sort, sortOrder, start, size);
//            }
//            if (query.size() != 1) {
//                throw new JIRAException("Only one saved filter could be used for query");
//            } else {
                return rss.getSavedFilterIssues(filter, sort, sortOrder, start, size);
//            }
        } catch (CaptchaRequiredException e) {
            removeRssSession(httpConnectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            removeRssSession(httpConnectionCfg);
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public JIRAIssue getIssue(ConnectionCfg httpConnectionCfg, String key) throws JIRAException {
        try {
            JiraRssAutoRenewClient rss = getRssSession(httpConnectionCfg);
            return rss.getIssue(key);
        } catch (CaptchaRequiredException e) {
            removeRssSession(httpConnectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            removeRssSession(httpConnectionCfg);
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAProject> getProjects(ConnectionCfg server) throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(server);
            return soap.getProjects();
        } catch (CaptchaRequiredException e) {
            removeRssSession(server);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(server));
            try {
                if (e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = null;
                    soap = getSoapSession(server);
                    return soap.getProjects();
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAProject> getProjectsForIssueCreation(ConnectionCfg httpConnectionCfg) throws JIRAException {
        // that's faking it. SOAP does not really support this
        return getProjects(httpConnectionCfg);
    }

    public List<JIRAConstant> getIssueTypes(ConnectionCfg httpConnectionCfg) throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(httpConnectionCfg);
            return soap.getIssueTypes();
        } catch (CaptchaRequiredException e) {
            removeRssSession(httpConnectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(httpConnectionCfg));
            try {
                if (e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(httpConnectionCfg);
                    return soap.getIssueTypes();
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAConstant> getIssueTypesForProject(ConnectionCfg httpConnectionCfg, long projectId, String projectKey)
            throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(httpConnectionCfg);
            return soap.getIssueTypesForProject(projectId, projectKey);
        } catch (CaptchaRequiredException e) {
            removeRssSession(httpConnectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(httpConnectionCfg));
            try {
                if (e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(httpConnectionCfg);
                    return soap.getIssueTypesForProject(projectId, projectKey);
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAConstant> getSubtaskIssueTypes(ConnectionCfg httpConnectionCfg) throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(httpConnectionCfg);
            return soap.getSubtaskIssueTypes();
        } catch (CaptchaRequiredException e) {
            removeRssSession(httpConnectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(httpConnectionCfg));
            try {
                if (e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(httpConnectionCfg);
                    return soap.getSubtaskIssueTypes();
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAConstant> getSubtaskIssueTypesForProject(ConnectionCfg connectionCfg, long projectId, String projectKey)
            throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            return soap.getSubtaskIssueTypesForProject(projectId, projectKey);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                    return soap.getSubtaskIssueTypesForProject(projectId, projectKey);
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAConstant> getStatuses(ConnectionCfg connection) throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connection);
            return soap.getStatuses();
        } catch (CaptchaRequiredException e) {
            removeRssSession(connection);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connection));
            try {
                if (e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connection);
                    return soap.getStatuses();
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public void addComment(ConnectionCfg connectionCfg, String issueKey, String comment) throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            soap.addComment(issueKey, comment);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                    soap.addComment(issueKey, comment);
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public void addAttachment(ConnectionCfg connectionCfg, String issueKey, String name, byte[] content)
            throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            soap.addAttachment(issueKey, name, content);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                    soap.addAttachment(issueKey, name, content);
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public JIRAIssue createIssue(ConnectionCfg connectionCfg, JIRAIssue issue) throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            JIRAIssue i = soap.createIssue(issue);
            return getIssue(connectionCfg, i.getKey());
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                    JIRAIssue i = soap.createIssue(issue);
                    return getIssue(connectionCfg, i.getKey());
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public JIRAIssue createSubtask(JiraServerData jiraServerData, JIRAIssue parent, JIRAIssue issue) throws JIRAException {
        throw new JIRAException("SOAP JIRA API does not support issue subtask creation");
    }

    public void logWork(ConnectionCfg connectionCfg, JIRAIssue issue, String timeSpent, Calendar startDate,
                        String comment, boolean updateEstimate, String newEstimate)
            throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            soap.logWork(issue, timeSpent, startDate, comment, updateEstimate, newEstimate);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                    soap.logWork(issue, timeSpent, startDate, comment, updateEstimate, newEstimate);
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAComponentBean> getComponents(ConnectionCfg connectionCfg, String projectKey) throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            return soap.getComponents(projectKey);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                    return soap.getComponents(projectKey);
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAVersionBean> getVersions(ConnectionCfg connectionCfg, String projectKey) throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            return soap.getVersions(projectKey);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            if (e == null) {
                logger.warn("PL-1710: e is null");
            } else if (e.getMessage() == null) {
                logger.warn("PL-1710: e.getMessage() is null");
            }
            try {
                if (e != null && e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                    return soap.getVersions(projectKey);
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAPriorityBean> getPriorities(ConnectionCfg connectionCfg) throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            return soap.getPriorities();
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e != null && e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                    return soap.getPriorities();
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAResolutionBean> getResolutions(ConnectionCfg connectionCfg) throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            return soap.getResolutions();
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e != null && e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                    return soap.getResolutions();
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAQueryFragment> getSavedFilters(ConnectionCfg connectionCfg) throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            return soap.getSavedFilters();
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e != null && e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                    return soap.getSavedFilters();
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAAction> getAvailableActions(ConnectionCfg connectionCfg, JIRAIssue issue) throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            return soap.getAvailableActions(issue);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e != null && e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                    return soap.getAvailableActions(issue);
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRAActionField> getFieldsForAction(ConnectionCfg connectionCfg, JIRAIssue issue, JIRAAction action)
            throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            return soap.getFieldsForAction(issue, action);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e != null && e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                    return soap.getFieldsForAction(issue, action);
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public void setField(ConnectionCfg connectionCfg, JIRAIssue issue, String fieldId, String value)
            throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            soap.setField(issue, fieldId, value);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e != null && e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                     soap.setField(issue, fieldId, value);
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public void setField(ConnectionCfg connectionCfg, JIRAIssue issue, String fieldId, String[] values)
            throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            soap.setField(issue, fieldId, values);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e != null && e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                     soap.setField(issue, fieldId, values);
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public void setFields(ConnectionCfg connectionCfg, JIRAIssue issue, List<JIRAActionField> fields) throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            soap.setFields(issue, fields);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e != null && e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                     soap.setFields(issue, fields);
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }


    public List<JIRAComment> getComments(ConnectionCfg connectionCfg, JIRAIssue issue) throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            return soap.getComments(issue);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e != null && e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                     return soap.getComments(issue);
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public Collection<JIRAAttachment> getIssueAttachements(ConnectionCfg connectionCfg, JIRAIssue issue)
            throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            return soap.getIssueAttachements(issue);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e != null && e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                    return soap.getIssueAttachements(issue);
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public void progressWorkflowAction(ConnectionCfg connectionCfg, JIRAIssue issue, JIRAAction action)
            throws JIRAException {
        progressWorkflowAction(connectionCfg, issue, action, null);
    }

    public void progressWorkflowAction(ConnectionCfg connectionCfg, JIRAIssue issue,
                                       JIRAAction action, List<JIRAActionField> fields) throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            soap.progressWorkflowAction(issue, action, fields);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e != null && e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                    soap.progressWorkflowAction(issue, action, fields);
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public JIRAIssue getIssueDetails(ConnectionCfg connectionCfg, JIRAIssue issue) throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            return soap.getIssueDetails(issue);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e != null && e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                    return soap.getIssueDetails(issue);
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public JIRAUserBean getUser(ConnectionCfg connectionCfg, String loginName)
            throws JIRAException, JiraUserNotFoundException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            return soap.getUser(loginName);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e != null && e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                    return soap.getUser(loginName);
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }

    public List<JIRASecurityLevelBean> getSecurityLevels(ConnectionCfg connectionCfg, String projectKey) throws JIRAException {
        try {
            JIRASessionPartOne soap = getSoapSession(connectionCfg);
            return soap.getSecurityLevels(projectKey);
        } catch (CaptchaRequiredException e) {
            removeRssSession(connectionCfg);
            throw new JiraCaptchaRequiredException(e.getMessage());
        } catch (RemoteApiException e) {
            soapSessions.remove(getSessionKey(connectionCfg));
            try {
                if (e != null && e.getMessage().contains("User not authenticated yet, or session timed out.")) {
                    JIRASessionPartOne soap = getSoapSession(connectionCfg);
                    return soap.getSecurityLevels(projectKey);
                }
            } catch (RemoteApiException e1) {
                throw new JIRAException(e.getMessage(), e);
            }
            throw new JIRAException(e.getMessage(), e);
        }
    }
}
