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
import me.glindholm.connector.commons.jira.soap.AxisSessionCallback;
import me.glindholm.theplugin.commons.ServerType;
import me.glindholm.theplugin.commons.jira.JiraServerData;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;
import me.glindholm.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import me.glindholm.theplugin.commons.util.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class JIRAServerFacade2Impl implements JIRAServerFacade2 {
    private static Logger logger;

    private JIRASoapAndXmlServerFacade2Impl soapAndXmlFacade;
    private JiraRESTFacade2Impl restFacade;

    private final Set<ConnectionCfg> restCapable = new HashSet<ConnectionCfg>();
    private final Set<ConnectionCfg> notRestCapable = new HashSet<ConnectionCfg>();

    JIRAServerFacade2 worker;

    public JIRAServerFacade2Impl(HttpSessionCallback callback, AxisSessionCallback axisCallback) {
        soapAndXmlFacade = new JIRASoapAndXmlServerFacade2Impl(callback, axisCallback);
        restFacade = new JiraRESTFacade2Impl();

        worker = (JIRAServerFacade2) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { JIRAServerFacade2.class },
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    ConnectionCfg connection = (ConnectionCfg) args[0];
                    boolean useRest = restCapable.contains(connection);
                    boolean useSoapAndXml = notRestCapable.contains(connection);
                    try {
                        if (!useRest && !useSoapAndXml) {
                            try {
                                if (restFacade.supportsRest(connection)) {
                                    restCapable.add(connection);
                                    useRest = true;
                                } else {
                                    notRestCapable.add(connection);
                                }
                            } catch (JIRAException e) {
                                Class<?>[] exceptionTypes = method.getExceptionTypes();
                                for (Class<?> exceptionType : exceptionTypes) {
                                    if (exceptionType.isAssignableFrom(JIRAException.class)) {
                                        throw e;
                                    }
                                }
                                throw new RemoteApiException(e);
                            }
                        }
                        if (useRest) {
                            return method.invoke(restFacade, args);
                        }
                        return method.invoke(soapAndXmlFacade, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                }
            }
        );
    }

    public void reset() {
        restFacade.reset();
        soapAndXmlFacade.reset();
        restCapable.clear();
        notRestCapable.clear();
    }

    public boolean usesRest(JiraServerData jiraServerData) {
        return worker.usesRest(jiraServerData);
    }

    public static void setLogger(Logger logger) {
        JIRASoapAndXmlServerFacade2Impl.setLogger(logger);
        JiraRESTFacade2Impl.setLogger(logger);
    }

    public ServerType getServerType() {
        return ServerType.JIRA_SERVER;
    }

//    public List<JIRAIssue> getIssues(ConnectionCfg httpConnectionCfg, String queryString, String sort, String sortOrder, int start, int size) throws JIRAException {
//        return worker.getIssues(httpConnectionCfg, queryString, sort, sortOrder, start, size);
//    }
//
//    public List<JIRAIssue> getIssues(ConnectionCfg httpConnectionCfg, List<JIRAQueryFragment> query, String sort, String sortOrder, int start, int size) throws JIRAException {
//        return worker.getIssues(httpConnectionCfg, query, sort, sortOrder, start, size);
//    }


    public List<JIRAIssue> getIssues(ConnectionCfg httpConnectionCfg, JiraFilter filter, String sort, String sortOrder, int start, int size) throws JIRAException {
        return worker.getIssues(httpConnectionCfg, filter, sort, sortOrder, start, size);
    }

    public List<JIRAIssue> getIssues(JiraServerData server, String query, String sort, String sortOrder, int start, int size) throws JIRAException {
        return worker.getIssues(server, query, sort, sortOrder, start, size);
    }

    public List<JIRAIssue> getSavedFilterIssues(ConnectionCfg httpConnectionCfg, JIRASavedFilter filter, String sort, String sortOrder, int start, int size) throws JIRAException {
        return worker.getSavedFilterIssues(httpConnectionCfg, filter, sort, sortOrder, start, size);
    }

//    public List<JIRAIssue> getSavedFilterIssues(ConnectionCfg httpConnectionCfg, List<JIRAQueryFragment> query, String sort, String sortOrder, int start, int size) throws JIRAException {
//        return worker.getSavedFilterIssues(httpConnectionCfg, query, sort, sortOrder, start, size);
//    }

    public List<JIRAProject> getProjects(ConnectionCfg httpConnectionCfg) throws JIRAException {
        return worker.getProjects(httpConnectionCfg);
    }

    public List<JIRAProject> getProjectsForIssueCreation(ConnectionCfg httpConnectionCfg) throws JIRAException {
        return worker.getProjectsForIssueCreation(httpConnectionCfg);
    }

    public List<JIRAConstant> getStatuses(ConnectionCfg httpConnectionCfg) throws JIRAException {
        return worker.getStatuses(httpConnectionCfg);
    }

    public List<JIRAConstant> getIssueTypes(ConnectionCfg httpConnectionCfg) throws JIRAException {
        return worker.getIssueTypes(httpConnectionCfg);
    }

    public List<JIRAConstant> getIssueTypesForProject(ConnectionCfg httpConnectionCfg, long projectId, String project) throws JIRAException {
        return worker.getIssueTypesForProject(httpConnectionCfg, projectId, project);
    }

    public List<JIRAConstant> getSubtaskIssueTypes(ConnectionCfg httpConnectionCfg) throws JIRAException {
        return worker.getSubtaskIssueTypes(httpConnectionCfg);
    }

    public List<JIRAConstant> getSubtaskIssueTypesForProject(ConnectionCfg httpConnectionCfg, long projectId, String project) throws JIRAException {
        return worker.getSubtaskIssueTypesForProject(httpConnectionCfg, projectId, project);
    }

    public List<JIRAQueryFragment> getSavedFilters(ConnectionCfg httpConnectionCfg) throws JIRAException {
        return worker.getSavedFilters(httpConnectionCfg);
    }

    public List<JIRAComponentBean> getComponents(ConnectionCfg httpConnectionCfg, String projectKey) throws JIRAException {
        return worker.getComponents(httpConnectionCfg, projectKey);
    }

    public List<JIRAVersionBean> getVersions(ConnectionCfg httpConnectionCfg, String projectKey) throws JIRAException {
        return worker.getVersions(httpConnectionCfg, projectKey);
    }

    public List<JIRAPriorityBean> getPriorities(ConnectionCfg httpConnectionCfg) throws JIRAException {
        return worker.getPriorities(httpConnectionCfg);
    }

    public List<JIRAResolutionBean> getResolutions(ConnectionCfg httpConnectionCfg) throws JIRAException {
        return worker.getResolutions(httpConnectionCfg);
    }

    public List<JIRAAction> getAvailableActions(ConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException {
        return worker.getAvailableActions(httpConnectionCfg, issue);
    }

    public List<JIRAActionField> getFieldsForAction(ConnectionCfg httpConnectionCfg, JIRAIssue issue, JIRAAction action) throws JIRAException {
        return worker.getFieldsForAction(httpConnectionCfg, issue, action);
    }

    public void progressWorkflowAction(ConnectionCfg httpConnectionCfg, JIRAIssue issue, JIRAAction action) throws JIRAException {
        worker.progressWorkflowAction(httpConnectionCfg, issue, action);
    }

    public void progressWorkflowAction(ConnectionCfg httpConnectionCfg, JIRAIssue issue, JIRAAction action, List<JIRAActionField> fields) throws JIRAException {
        worker.progressWorkflowAction(httpConnectionCfg, issue, action, fields);
    }

    public void addComment(ConnectionCfg httpConnectionCfg, String issueKey, String comment) throws JIRAException {
        worker.addComment(httpConnectionCfg, issueKey, comment);
    }

    public void addAttachment(ConnectionCfg httpConnectionCfg, String issueKey, String name, byte[] content) throws JIRAException {
        worker.addAttachment(httpConnectionCfg, issueKey, name, content);
    }

    public JIRAIssue createIssue(ConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException {
        return worker.createIssue(httpConnectionCfg, issue);
    }

    public JIRAIssue createSubtask(JiraServerData jiraServerData, JIRAIssue parent, JIRAIssue issue) throws JIRAException {
        return worker.createSubtask(jiraServerData, parent, issue);
    }

    public JIRAIssue getIssue(ConnectionCfg httpConnectionCfg, String key) throws JIRAException {
        return worker.getIssue(httpConnectionCfg, key);
    }

    public JIRAIssue getIssueDetails(ConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException {
        return worker.getIssueDetails(httpConnectionCfg, issue);
    }

    public void logWork(ConnectionCfg httpConnectionCfg, JIRAIssue issue, String timeSpent, Calendar startDate, String comment, boolean updateEstimate, String newEstimate) throws JIRAException {
        worker.logWork(httpConnectionCfg, issue, timeSpent, startDate, comment, updateEstimate, newEstimate);
    }

    public void setField(ConnectionCfg httpConnectionCfg, JIRAIssue issue, String fieldId, String value) throws JIRAException {
        worker.setField(httpConnectionCfg, issue, fieldId, value);
    }

    public void setField(ConnectionCfg httpConnectionCfg, JIRAIssue issue, String fieldId, String[] values) throws JIRAException {
        worker.setField(httpConnectionCfg, issue, fieldId, values);
    }

    public void setFields(ConnectionCfg httpConnectionCfg, JIRAIssue issue, List<JIRAActionField> fields) throws JIRAException {
        worker.setFields(httpConnectionCfg, issue, fields);
    }

    public JIRAUserBean getUser(ConnectionCfg httpConnectionCfg, String loginName) throws JIRAException, JiraUserNotFoundException {
        return worker.getUser(httpConnectionCfg, loginName);
    }

    public List<JIRAComment> getComments(ConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException {
        return worker.getComments(httpConnectionCfg, issue);
    }

    public Collection<JIRAAttachment> getIssueAttachements(ConnectionCfg httpConnectionCfg, JIRAIssue issue) throws JIRAException {
        return worker.getIssueAttachements(httpConnectionCfg, issue);
    }

    public List<JIRASecurityLevelBean> getSecurityLevels(ConnectionCfg connectionCfg, String projectKey) throws JIRAException {
        return worker.getSecurityLevels(connectionCfg, projectKey);
    }

    public void testServerConnection(final ConnectionCfg httpConnectionCfg) throws RemoteApiException {
        worker.testServerConnection(httpConnectionCfg);
    }
}
