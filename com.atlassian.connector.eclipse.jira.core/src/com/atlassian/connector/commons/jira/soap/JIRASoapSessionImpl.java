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

package com.atlassian.connector.commons.jira.soap;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.jira.*;
import com.atlassian.connector.commons.jira.beans.JIRAAttachment;
import com.atlassian.connector.commons.jira.beans.JIRAComment;
import com.atlassian.connector.commons.jira.beans.JIRACommentBean;
import com.atlassian.connector.commons.jira.beans.JIRAComponentBean;
import com.atlassian.connector.commons.jira.beans.JIRAConstant;
import com.atlassian.connector.commons.jira.beans.JIRAIssueTypeBean;
import com.atlassian.connector.commons.jira.beans.JIRAPriorityBean;
import com.atlassian.connector.commons.jira.beans.JIRAProject;
import com.atlassian.connector.commons.jira.beans.JIRAProjectBean;
import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.atlassian.connector.commons.jira.beans.JIRAResolutionBean;
import com.atlassian.connector.commons.jira.beans.JIRASavedFilterBean;
import com.atlassian.connector.commons.jira.beans.JIRASecurityLevelBean;
import com.atlassian.connector.commons.jira.beans.JIRAStatusBean;
import com.atlassian.connector.commons.jira.beans.JIRAUserBean;
import com.atlassian.connector.commons.jira.beans.JIRAVersionBean;
import com.atlassian.connector.commons.jira.cache.CacheConstants;
import com.atlassian.connector.commons.jira.soap.axis.JiraSoapService;
import com.atlassian.connector.commons.jira.soap.axis.JiraSoapServiceServiceLocator;
import com.atlassian.connector.commons.jira.soap.axis.RemoteAttachment;
import com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException;
import com.atlassian.connector.commons.jira.soap.axis.RemoteComment;
import com.atlassian.connector.commons.jira.soap.axis.RemoteComponent;
import com.atlassian.connector.commons.jira.soap.axis.RemoteField;
import com.atlassian.connector.commons.jira.soap.axis.RemoteFieldValue;
import com.atlassian.connector.commons.jira.soap.axis.RemoteFilter;
import com.atlassian.connector.commons.jira.soap.axis.RemoteIssue;
import com.atlassian.connector.commons.jira.soap.axis.RemoteIssueType;
import com.atlassian.connector.commons.jira.soap.axis.RemoteNamedObject;
import com.atlassian.connector.commons.jira.soap.axis.RemotePriority;
import com.atlassian.connector.commons.jira.soap.axis.RemoteProject;
import com.atlassian.connector.commons.jira.soap.axis.RemoteResolution;
import com.atlassian.connector.commons.jira.soap.axis.RemoteSecurityLevel;
import com.atlassian.connector.commons.jira.soap.axis.RemoteStatus;
import com.atlassian.connector.commons.jira.soap.axis.RemoteUser;
import com.atlassian.connector.commons.jira.soap.axis.RemoteVersion;
import com.atlassian.connector.commons.jira.soap.axis.RemoteWorklog;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import com.atlassian.theplugin.commons.util.HttpConfigurableAdapter;
import com.atlassian.theplugin.commons.util.Logger;
import org.apache.axis.AxisProperties;
import org.apache.commons.codec.binary.Base64;
import org.xml.sax.SAXException;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class JIRASoapSessionImpl implements JIRASessionPartOne {

    private String token;
    private final JiraSoapService service;
    private final ConnectionCfg httpConnectionCfg;
    private boolean loggedIn;
    private final Logger logger;
    private Date lastUsed = new Date();


    public Date getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }

    //
    // AxisProperties are shit - if you try to set nonexistent property to null, NPE is thrown. Moreover, sometimes
    // setting apparently *existing* property to null also throws NPE (see bug PL-412)! Crap, crap, crap...
    //
    private void setAxisProperty(String name, String value) {
        if (value == null) {
            if (AxisProperties.getProperty(name) != null) {
                try {
                    AxisProperties.setProperty(name, "");
                } catch (NullPointerException e) {
                    logger.info("Setting AXIS property " + name + " to empty", e);
                }
            }
        } else {
            AxisProperties.setProperty(name, value);
        }
    }

    private void setSystemProperty(String name, String value) {

        if (value == null) {
            //if (System.getProperty(name) != null) {
            try {
                System.setProperty(name, "");
            } catch (NullPointerException e) {
                logger.info("Setting system property " + name + " to empty", e);
            }
            //}
        } else {
            System.setProperty(name, value);
        }
    }

    private void setProxy() {
        boolean useIdeaProxySettings =
                ConfigurationFactory.getConfiguration().getGeneralConfigurationData().getUseIdeaProxySettings();
        HttpConfigurableAdapter proxyInfo = ConfigurationFactory.getConfiguration().transientGetHttpConfigurable();
        String host = null;
        String port = null;
        String user = null;
        String password = null;
        if (useIdeaProxySettings && proxyInfo.isUseHttpProxy()) {
            host = proxyInfo.getProxyHost();
            port = String.valueOf(proxyInfo.getProxyPort());
            if (proxyInfo.isProxyAuthentication()) {
                user = proxyInfo.getProxyLogin();
                password = proxyInfo.getPlainProxyPassword();
            }
        }

        //
        // well - re-setting proxy does not really work - Axis bug
        // see: http://issues.apache.org/jira/browse/AXIS-2295
        // So in order to apply new proxy settings, IDEA has to be restarted
        // all software sucks
        //
        setAxisProperty("http.proxyHost", host);
        setAxisProperty("http.proxyPort", port);

        setSystemProperty("http.proxyHost", host);
        setSystemProperty("http.proxyPort", port);

        setAxisProperty("http.proxyUser", user);
        setSystemProperty("http.proxyUser", user);

        setAxisProperty("http.proxyPassword", password);
        setSystemProperty("http.proxyPassword", password);

    }

    public JIRASoapSessionImpl(Logger logger, ConnectionCfg connectionCfg, AxisSessionCallback callback)
            throws ServiceException, MalformedURLException {
        this.logger = logger;
        URL portAddress = new URL(connectionCfg.getUrl() + "/rpc/soap/jirasoapservice-v2");
        JiraSoapServiceServiceLocator loc = new JiraSoapServiceServiceLocator();
        AbstractHttpSession.setUrl(portAddress); // dirty hack
        service = loc.getJirasoapserviceV2(portAddress);
        // to use Basic HTTP Authentication:
        if (callback != null) {
            callback.configureRemoteService(service, connectionCfg);
        }

        setProxy();

        this.httpConnectionCfg = connectionCfg;
    }

    public void login(String userName, String password) throws RemoteApiException {
        try {
            token = service.login(userName, password);
        } catch (RemoteAuthenticationException e) {
            if (e != null && e.getFaultString() != null
                    && e.getFaultString().contains("The maximum number of failed login attempts")) {
                throw new RemoteApiLoginException("Due to multiple failed login attempts, "
                        + "you have been temporarily banned from using the remote API.\n"
                        + "To re-enable the remote API please log into your server via the web interface", e);
            } else {
                throw new RemoteApiLoginException("Authentication failed", e);
            }
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString());
        }
        loggedIn = true;
    }

    public void logout() {
        try {
            if (service.logout(token)) {
                token = null;
                loggedIn = false;
            }
        } catch (java.rmi.RemoteException e) {
            // todo: log the exception
        }
    }

    public void logWork(JIRAIssue issue, String timeSpent, Calendar startDate, String comment,
                        boolean updateEstimate, String newEstimate)
            throws RemoteApiException {
        RemoteWorklog workLog = new RemoteWorklog();
        workLog.setStartDate(startDate);
        workLog.setTimeSpent(timeSpent);
        if (comment != null) {
            workLog.setComment(comment);
        }
        try {
            if (updateEstimate) {
                if (newEstimate != null) {
                    service.addWorklogWithNewRemainingEstimate(token, issue.getKey(), workLog, newEstimate);
                } else {
                    service.addWorklogAndAutoAdjustRemainingEstimate(token, issue.getKey(), workLog);
                }
            } else {
                service.addWorklogAndRetainRemainingEstimate(token, issue.getKey(), workLog);
            }
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        }
    }

    public JIRAIssue createIssue(JIRAIssue issue) throws RemoteApiException {
        RemoteIssue remoteIssue = new RemoteIssue();

        remoteIssue.setProject(issue.getProjectKey());
        remoteIssue.setType(String.valueOf(issue.getTypeConstant().getId()));
        remoteIssue.setSummary(issue.getSummary());
        if (issue.getPriorityConstant().getId() != CacheConstants.ANY_ID) {
            remoteIssue.setPriority(String.valueOf(issue.getPriorityConstant().getId()));
        }

        if (issue.getDescription() != null) {
            remoteIssue.setDescription(issue.getDescription());
        }
        if (issue.getAssignee() != null) {
            remoteIssue.setAssignee(issue.getAssignee());
        }

        final List<JIRAConstant> components = issue.getComponents();
        if (components != null && components.size() > 0) {
            RemoteComponent[] remoteComponents = new RemoteComponent[components.size()];
            int i = 0;
            for (JIRAConstant component : components) {
                remoteComponents[i] = new RemoteComponent(String.valueOf(component.getId()), component.getName());
                i++;
            }
            remoteIssue.setComponents(remoteComponents);
        }

        final List<JIRAConstant> versions = issue.getAffectsVersions();
        if (versions != null && versions.size() > 0) {
            RemoteVersion[] remoteVersions = new RemoteVersion[versions.size()];
            int i = 0;
            for (JIRAConstant version : versions) {
                remoteVersions[i] = new RemoteVersion();
                remoteVersions[i].setId(String.valueOf(version.getId()));
                ++i;
            }
            remoteIssue.setAffectsVersions(remoteVersions);
        }

        final List<JIRAConstant> fixVersions = issue.getFixVersions();
        if (fixVersions != null && fixVersions.size() > 0) {
            RemoteVersion[] remoteFixVersions = new RemoteVersion[fixVersions.size()];
            int i = 0;
            for (JIRAConstant version : fixVersions) {
                remoteFixVersions[i] = new RemoteVersion();
                remoteFixVersions[i].setId(String.valueOf(version.getId()));
                ++i;
            }
            remoteIssue.setFixVersions(remoteFixVersions);
        }

        try {
            remoteIssue = service.createIssue(token, remoteIssue);
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        }

        // todo: fill in all other fields. For now only the issue key and URL is being displayed
        JIRAIssueBean retVal = new JIRAIssueBean(httpConnectionCfg.getUrl(), remoteIssue);

        retVal.setKey(remoteIssue.getKey());
        return retVal;
    }

    public JIRAIssue getIssueDetails(JIRAIssue issue) throws RemoteApiException {
        RemoteSecurityLevel securityLevel = null;

        try {
            securityLevel = service.getSecurityLevel(token, issue.getKey());
        } catch (RemoteException e) {
            if (logger != null) {
                logger.warn(
                        "Soap method 'getSecurityLevel' thrown exception. "
                                + "Probably there is no 'SecurityLevel' on JIRA (non enterprise version of JIRA).", e);
            }
        } catch (ClassCastException e) {
            if (logger != null) {
                logger.warn(
                        "Soap method 'getSecurityLevel' thrown ClassCastException. Probably some JIRA error.", e);
            }
        } catch (Exception e) {
            // PL-1492 and PL-1609
            if (e instanceof SAXException && logger != null) {
                logger.warn(
                        "Soap method 'getSecurityLevel' thrown SAXException. Probably some JIRA error.", e);
            }
            throw new RemoteApiException(e);
        }

        try {
            RemoteIssue rIssue = service.getIssue(token, issue.getKey());


            if (rIssue == null) {
                throw new RemoteApiException("Unable to retrieve issue details");
            }
            JIRAIssueBean issueBean = new JIRAIssueBean(issue);

            if (securityLevel != null) {
                issueBean.setSecurityLevel(
                        new JIRASecurityLevelBean(Long.valueOf(securityLevel.getId()), securityLevel.getName()));
            }

            issueBean.setWikiDescription(rIssue.getDescription());

            RemoteVersion[] aVers = rIssue.getAffectsVersions();
            List<JIRAConstant> av = new ArrayList<JIRAConstant>();
            for (RemoteVersion v : aVers) {
                av.add(new JIRAVersionBean(Long.valueOf(v.getId()), v.getName(), v.isReleased()));
            }
            issueBean.setAffectsVersions(av);

            RemoteVersion[] fVers = rIssue.getFixVersions();
            List<JIRAConstant> fv = new ArrayList<JIRAConstant>();
            for (RemoteVersion v : fVers) {
                fv.add(new JIRAVersionBean(Long.valueOf(v.getId()), v.getName(), v.isReleased()));
            }
            issueBean.setFixVersions(fv);

            RemoteComponent[] comps = rIssue.getComponents();
            List<JIRAConstant> c = new ArrayList<JIRAConstant>();
            for (RemoteComponent rc : comps) {
                c.add(new JIRAComponentBean(Long.valueOf(rc.getId()), rc.getName()));
            }
            issueBean.setComponents(c);

            issueBean.setProjectKey(rIssue.getProject());
            issueBean.setSummary(rIssue.getSummary());

            issueBean.setApiIssueObject(rIssue);

            return issueBean;

        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        } catch (ClassCastException e) {
            throw new RemoteApiException(e.toString(), e);
        } catch (Exception e) {
            // PL-1644 - there is this bloke who seems to have some customized RPC plugin,
            // which returns non-standard fields in SOAP response. Axis croaks on it,
            // we have to intercept
            if (e instanceof SAXException && logger != null) {
                logger.warn("Soap method 'getIssue' thrown SAXException. "
                        + "Probably some JIRA error or weird JIRA SOAP plugin.", e);
            }
            throw new RemoteApiException(e);
        }

    }

    public void addComment(String issueKey, String comment) throws RemoteApiException {
        try {
            RemoteComment rComment = new RemoteComment();
            rComment.setBody(comment);
            service.addComment(token, issueKey, rComment);
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        }
    }

    public void addAttachment(String issueKey, String name, byte[] content) throws RemoteApiException {
        String[] encodedContents = new String[]{new String(new Base64().encode(content))};
        String[] names = new String[]{name};
        try {
            service.addBase64EncodedAttachmentsToIssue(token, issueKey, names, encodedContents);
        } catch (RemoteException e) {
            if (e.toString().startsWith("java.lang.OutOfMemoryError")) {
                throw new RemoteApiException("Attachment size is too large, try uploading directly from web browser", e);
            }
            throw new RemoteApiException(e.toString(), e);
        }
    }

    public List<JIRAProject> getProjects() throws RemoteApiException {
        try {
            RemoteProject[] projects = service.getProjectsNoSchemes(token);
            List<JIRAProject> projectList = new ArrayList<JIRAProject>(projects.length);
            if (projects != null) {
                for (RemoteProject p : projects) {
                    JIRAProjectBean project = new JIRAProjectBean();

                    project.setName(p.getName());
                    project.setKey(p.getKey());
                    project.setDescription(p.getDescription());
                    project.setUrl(p.getUrl());
                    project.setLead(p.getLead());
                    project.setId(Long.valueOf(p.getId()));

                    projectList.add(project);
                }
            }

            return projectList;
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        }
    }

    private List<JIRAConstant> issueTableToList(RemoteIssueType[] types) throws MalformedURLException {
        List<JIRAConstant> typesList = new ArrayList<JIRAConstant>();
        if (types != null) {
            for (RemoteIssueType type : types) {
                typesList.add(new JIRAIssueTypeBean(Long.valueOf(type.getId()), type.getName(), new URL(type.getIcon())));
            }
        }
        return typesList;
    }

    public List<JIRAConstant> getIssueTypes() throws RemoteApiException {
        try {
            return issueTableToList(service.getIssueTypes(token));
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        } catch (MalformedURLException e) {
            throw new RemoteApiException(e.toString(), e);
        }

    }

    public List<JIRAConstant> getIssueTypesForProject(long projectId, String projectKey) throws RemoteApiException {
        try {
            return issueTableToList(service.getIssueTypesForProject(token, Long.valueOf(projectId).toString()));
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        } catch (MalformedURLException e) {
            throw new RemoteApiException(e.toString(), e);
        }
    }

    public List<JIRAConstant> getSubtaskIssueTypes() throws RemoteApiException {
        try {
            return issueTableToList(service.getSubTaskIssueTypes(token));
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        } catch (MalformedURLException e) {
            throw new RemoteApiException(e.toString(), e);
        }

    }

    public List<JIRAConstant> getSubtaskIssueTypesForProject(long projectId, String projectKey) throws RemoteApiException {
        try {
            return issueTableToList(service.getSubTaskIssueTypesForProject(token, Long.valueOf(projectId).toString()));
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        } catch (MalformedURLException e) {
            throw new RemoteApiException(e.toString(), e);
        }
    }

    public List<JIRAConstant> getStatuses() throws RemoteApiException {
        try {
            RemoteStatus[] statuses = service.getStatuses(token);

            List<JIRAConstant> statusesList = new ArrayList<JIRAConstant>();
            if (statuses != null) {
                for (RemoteStatus status : statuses) {
                    statusesList.add(new JIRAStatusBean(
                            Long.valueOf(status.getId()), status.getName(), new URL(status.getIcon())));
                }
            }
            return statusesList;
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        } catch (MalformedURLException e) {
            throw new RemoteApiException(e.toString(), e);
        }
    }

    public List<JIRAComponentBean> getComponents(String projectKey) throws RemoteApiException {
        try {
            RemoteComponent[] components = service.getComponents(token, projectKey);

            List<JIRAComponentBean> componentsList = new ArrayList<JIRAComponentBean>();
            if (components != null) {
                for (RemoteComponent c : components) {
                    componentsList.add(new JIRAComponentBean(Long.valueOf(c.getId()), c.getName()));
                }
            }
            return componentsList;
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        }
    }

    public List<JIRAVersionBean> getVersions(String projectKey) throws RemoteApiException {
        try {
            RemoteVersion[] versions = service.getVersions(token, projectKey);

            List<JIRAVersionBean> versionsList = new ArrayList<JIRAVersionBean>();
            if (versions != null) {
                for (RemoteVersion v : versions) {
                    versionsList.add(new JIRAVersionBean(Long.valueOf(v.getId()), v.getName(), v.isReleased()));
                }
            }
            return versionsList;
        } catch (RemoteException e) {
            throw new RemoteApiException(
                    e.toString() != null ? e.toString() : "Cannot fetch project '" + projectKey + "' versions", e);
        }
    }

    public List<JIRAPriorityBean> getPriorities() throws RemoteApiException {
        try {
            RemotePriority[] priorities = service.getPriorities(token);

            List<JIRAPriorityBean> prioritiesList = new ArrayList<JIRAPriorityBean>();
            int i = 0;
            if (priorities != null) {
                for (RemotePriority p : priorities) {
                    // PL-1164 - The "i" parameter defines the order in which priorities
                    // are shown in the issue tree. I am assuming that JIRA returns the
                    // list of priorities in the order that the user defined, and not
                    // in some random order. This does seem to be the case with my test httpConnectionCfg
                    prioritiesList.add(new JIRAPriorityBean(Long.valueOf(p.getId()), i, p.getName(), new URL(p.getIcon())));
                    ++i;
                }
            }
            return prioritiesList;
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        } catch (MalformedURLException e) {
            throw new RemoteApiException(e.toString(), e);
        }
    }

    public List<JIRAResolutionBean> getResolutions() throws RemoteApiException {
        try {
            RemoteResolution[] resolutions = service.getResolutions(token);

            List<JIRAResolutionBean> resolutionsList = new ArrayList<JIRAResolutionBean>();
            if (resolutions != null) {
                for (RemoteResolution p : resolutions) {
                    resolutionsList.add(new JIRAResolutionBean(Long.valueOf(p.getId()), p.getName()));
                }
            }
            return resolutionsList;
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        }
    }

    public List<JIRAQueryFragment> getSavedFilters() throws RemoteApiException {
        try {
            RemoteFilter[] filters = service.getSavedFilters(token);

            List<JIRAQueryFragment> filtersList = new ArrayList<JIRAQueryFragment>(filters != null ? filters.length : 0);
            if (filters != null) {
                for (RemoteFilter f : filters) {
                    if (f == null) {
                        continue;
                    }
                    filtersList.add(new JIRASavedFilterBean(f.getName(), Long.valueOf(f.getId())));
                }
            }
            return filtersList;
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        }

    }

    public void setField(JIRAIssue issue, String fieldId, String value) throws RemoteApiException {
        setField(issue, fieldId, new String[]{value});
    }

    public void setField(JIRAIssue issue, String fieldId, String[] values) throws RemoteApiException {
        RemoteFieldValue v = new RemoteFieldValue();
        RemoteFieldValue[] vTable = {v};
        v.setId(fieldId);
        v.setValues(values);
        try {
            service.updateIssue(token, issue.getKey(), vTable);
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        }
    }

    public void setFields(JIRAIssue issue, List<JIRAActionField> fields) throws RemoteApiException {
        RemoteFieldValue[] vTable = new RemoteFieldValue[fields.size()];
        int i = 0;
        for (JIRAActionField field : fields) {
            vTable[i] = new RemoteFieldValue();
            vTable[i].setId(field.getFieldId());
            vTable[i].setValues(field.getValues().toArray(new String[field.getValues().size()]));
            i++;
        }
        try {
            service.updateIssue(token, issue.getKey(), vTable);
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        }
    }

    public List<JIRAAction> getAvailableActions(JIRAIssue issue) throws RemoteApiException {
        try {
            RemoteNamedObject[] actions = service.getAvailableActions(token, issue.getKey());
            List<JIRAAction> actionList = new ArrayList<JIRAAction>(actions != null ? actions.length : 0);
            if (actions != null) {
                for (RemoteNamedObject action : actions) {
                    actionList.add(new JIRAActionBean(Long.valueOf(action.getId()), action.getName()));
                }
            }
            return actionList;
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        } catch (ClassCastException e) {
            throw new RemoteApiException(e.toString(), e);
        } catch (Exception e) {
            // PL-1609
            if (e instanceof SAXException && logger != null) {
                logger.warn(
                        "Soap method 'getSecurityLevel' thrown SAXException. Probably some JIRA error.", e);
            }
            throw new RemoteApiException(e);
        }
    }

    public List<JIRASecurityLevelBean> getSecurityLevels(final String projectKey) throws RemoteApiException {
        List<JIRASecurityLevelBean> levels = new ArrayList<JIRASecurityLevelBean>();
        try {
            RemoteSecurityLevel[] remoteSecurityLevels = service.getSecurityLevels(token, projectKey);
            for (RemoteSecurityLevel remoteLevel : remoteSecurityLevels) {
                levels.add(new JIRASecurityLevelBean(Long.valueOf(remoteLevel.getId()), remoteLevel.getName()));
            }
        } catch (RemoteException e) {
            throw new RemoteApiException(e);
        }

        return levels;
    }

    public List<JIRAActionField> getFieldsForAction(JIRAIssue issue, JIRAAction action) throws RemoteApiException {
        try {
            RemoteField[] fields = service.getFieldsForAction(
                    token, issue.getKey(), Long.valueOf(action.getId()).toString());
            List<JIRAActionField> fieldList = new ArrayList<JIRAActionField>(fields.length);
            for (RemoteField f : fields) {
                fieldList.add(new JIRAActionFieldBean(f.getId(), f.getName()));
            }
            return fieldList;
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        }
    }

    public void progressWorkflowAction(JIRAIssue issue, JIRAAction action, List<JIRAActionField> fields)
            throws RemoteApiException {
        try {
            if (fields == null) {
                RemoteFieldValue[] dummyValues = new RemoteFieldValue[0];
                service.progressWorkflowAction(token, issue.getKey(), String.valueOf(action.getId()), dummyValues);
            } else {

                CopyOnWriteArrayList<JIRAActionField> safeFields = new CopyOnWriteArrayList<JIRAActionField>(fields);

                for (JIRAActionField field : safeFields) {
                    if (field.getValues() == null) {
                        safeFields.remove(field);
                    }
                }

                int i = 0;
                RemoteFieldValue[] values = new RemoteFieldValue[safeFields.size()];

                for (JIRAActionField field : safeFields) {
                    List<String> fieldValues = field.getValues();
                    String[] fieldValueTable = fieldValues.toArray(new String[fieldValues.size()]);
                    values[i] = new RemoteFieldValue(field.getFieldId(), fieldValueTable);
                    ++i;
                }
                service.progressWorkflowAction(token, issue.getKey(), String.valueOf(action.getId()), values);
            }
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        }
    }

    public List<JIRAComment> getComments(JIRAIssue issue) throws RemoteApiException {
        try {
            RemoteComment[] comments = service.getComments(token, issue.getKey());
            if (comments == null) {
                throw new RemoteApiException("Unable to retrieve comments");
            }

            List<JIRAComment> commentsList = new ArrayList<JIRAComment>(comments.length);
            for (RemoteComment c : comments) {
                commentsList.add(new JIRACommentBean(c.getId(), c.getAuthor(), c.getBody(), c.getCreated()));
            }
            return commentsList;
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        } catch (IllegalArgumentException e) {
            // PL-1756
            throw new RemoteApiException(e.toString(), e);
        }
    }

    public JIRAUserBean getUser(String loginName) throws RemoteApiException, JiraUserNotFoundException {
        try {
            RemoteUser ru = service.getUser(token, loginName);
            if (ru == null) {
                throw new JiraUserNotFoundException("User Name for " + loginName + " not found");
            }
            return new JIRAUserBean(-1, ru.getFullname(), ru.getName()) {
                @Override
                public String getQueryStringFragment() {
                    return null;
                }

                public JIRAQueryFragment getClone() {
                    return null;
                }
            };
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        } catch (ClassCastException e) {
            throw new RemoteApiException(e.toString(), e);
        }
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public Collection<JIRAAttachment> getIssueAttachements(JIRAIssue issue) throws RemoteApiException {
        try {
            RemoteAttachment[] attachements = service.getAttachmentsFromIssue(token, issue.getKey());

            List<JIRAAttachment> attachmentList = new ArrayList<JIRAAttachment>(attachements != null ? attachements.length : 0);
            if (attachements != null) {
                for (RemoteAttachment a : attachements) {
                    attachmentList.add(new JIRAAttachment(a.getId(),
                            a.getAuthor(), a.getFilename(), a.getFilesize(),
                            a.getMimetype(), a.getCreated()));
                }
            }
            return attachmentList;
        } catch (RemoteException e) {
            throw new RemoteApiException(e.toString(), e);
        } catch (ClassCastException e) {
            throw new RemoteApiException("Soap axis remote request failed to properly cast response while "
                    + "acquiring issue attachments", e);
        } catch (Exception e) {
            if (e.getMessage().contains("Bad types")) {
                throw new RemoteApiException("Soap axis remote request failed to properly cast response while "
                        + "acquiring issue attachments", e);
            } else {
                throw new RemoteApiException(e.toString(), e);
            }

        }
    }
}

