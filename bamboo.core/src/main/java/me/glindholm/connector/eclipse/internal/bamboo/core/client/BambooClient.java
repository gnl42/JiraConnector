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

package me.glindholm.connector.eclipse.internal.bamboo.core.client;

import java.net.Proxy;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import me.glindholm.bamboo.model.RestInfo;
import me.glindholm.bamboo.model.UserBean;
import me.glindholm.connector.eclipse.internal.bamboo.core.BambooUtil;
import me.glindholm.connector.eclipse.internal.bamboo.core.PlanBranches;
import me.glindholm.connector.eclipse.internal.bamboo.core.service.BambooClientCache;
import me.glindholm.connector.eclipse.internal.bamboo.core.service.BambooLocalConfiguration;
import me.glindholm.connector.eclipse.internal.bamboo.core.service.rest.BambooRestClientAdapter;
import me.glindholm.theplugin.commons.bamboo.BambooBuild;
import me.glindholm.theplugin.commons.bamboo.BambooPlan;
import me.glindholm.theplugin.commons.bamboo.BuildDetails;
import me.glindholm.theplugin.commons.cfg.SubscribedPlan;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;

/**
 * Bridge between Mylyn and the ACC API's
 *
 * @author Shawn Minto
 * @author Wojciech Seliga
 */
public class BambooClient {
    private static final String URL_REGEXP_HTTP = "http.*"; //$NON-NLS-1$

    private static final String URL_REGEXP_HTTPS = "https.*"; //$NON-NLS-1$

    private static final String PROXY_TYPE_HTTP = "HTTP"; //$NON-NLS-1$

    private static final String PROXY_TYPE_HTTPS = "HTTPS"; //$NON-NLS-1$

    private final String baseUrl;

    private final BambooClientCache cache;

    private BambooRestClientAdapter restClient = null;
    private final AbstractWebLocation location;

    private BambooLocalConfiguration localConfiguration;

    public BambooClient(final AbstractWebLocation location, final BambooLocalConfiguration configuration, final BambooRestClientAdapter restClient) {
        Assert.isNotNull(location);
        Assert.isNotNull(configuration);
        baseUrl = location.getUrl();
        this.location = location;
        localConfiguration = configuration;

        cache = new BambooClientCache(this);
        this.restClient = restClient;

    }

    public BambooClient(final AbstractWebLocation location, final BambooLocalConfiguration configuration) {
        this(location, configuration, null);
    }

    private BambooRestClientAdapter getRestClient() {
        if (restClient == null) {
            restClient = createRestClient(location, cache);
            try { // Make sure we can get a working connection
                final UserBean currentUser = restClient.getCurrentUser();
            } catch (final RemoteApiException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return restClient;
    }

    private BambooRestClientAdapter createRestClient(final AbstractWebLocation location, final BambooClientCache cache) {
        final AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
        final String baseUrl = location.getUrl();

        final Proxy proxy;
        if (baseUrl.matches(URL_REGEXP_HTTPS)) {
            proxy = location.getProxyForHost(baseUrl, PROXY_TYPE_HTTPS);
        } else if (baseUrl.matches(URL_REGEXP_HTTP)) {
            proxy = location.getProxyForHost(baseUrl, PROXY_TYPE_HTTP);
        } else {
            proxy = null;
        }

        String username = ""; //$NON-NLS-1$
        String password = null;

        if (credentials != null) {
            username = credentials.getUserName();
            password = credentials.getPassword();
        }

        return new BambooRestClientAdapter(baseUrl, username, password, proxy, cache, localConfiguration.getFollowRedirects());
    }

    public boolean hasRepositoryData() {
        return cache != null && cache.getData() != null;
    }

    public BambooClientData getClientData() {
        return cache.getData();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public BambooClientCache getCache() {
        return cache;
    }

    public BambooClientData updateRepositoryData(final IProgressMonitor monitor, final TaskRepository taskRepository) throws CoreException, RemoteApiException {
        monitor.subTask("Retrieving plans");
        final BambooClientData newClientData = new BambooClientData();
        final List<BambooPlan> projects = getRestClient().getPlanList();
        newClientData.setPlans(projects);
        return newClientData;
    }

    public Collection<BambooBuild> getBuilds(final IProgressMonitor monitor, final TaskRepository taskRepository, final boolean promptForCredentials)
            throws CoreException, RemoteApiException {
        monitor.subTask("Retrieving builds");
        final boolean showPlanBranches = BambooUtil.getPlanBranches(taskRepository).equals(PlanBranches.NO);
        final boolean showMyPlansBranches = BambooUtil.getPlanBranches(taskRepository).equals(PlanBranches.MINE);
        final Collection<SubscribedPlan> subscribedPlans = BambooUtil.getSubscribedPlans(taskRepository);
        return getRestClient().getSubscribedPlansResults(subscribedPlans, BambooUtil.isUseFavourites(taskRepository), !showPlanBranches, showMyPlansBranches,
                0);
    }

    public BuildDetails getBuildDetails(final IProgressMonitor monitor, final TaskRepository taskRepository, final BambooBuild build)
            throws CoreException, UnsupportedOperationException, RemoteApiException {
        monitor.subTask("Retrieving build details");
        final BuildDetails buildDetails = getRestClient().getBuildDetails(build.getPlanKey(), build.getNumber());
        return buildDetails;
    }

    public String getBuildLogs(final IProgressMonitor monitor, final TaskRepository taskRepository, final BambooBuild build)
            throws CoreException, UnsupportedOperationException, RemoteApiException {
        monitor.subTask("Retrieving build details");
        return getRestClient().getBuildLogs(build.getPlanKey(), build.getNumber());
    }

    public void addLabelToBuild(final IProgressMonitor monitor, final TaskRepository repository, final BambooBuild build, final String label)
            throws CoreException, UnsupportedOperationException, RemoteApiException {
        monitor.subTask("Adding label to build");
        getRestClient().addLabelToBuild(build.getPlanKey(), build.getNumber(), label);
    }

    public void addCommentToBuild(final IProgressMonitor monitor, final TaskRepository repository, final BambooBuild build, final String comment)
            throws CoreException, UnsupportedOperationException, RemoteApiException {
        monitor.subTask("Adding comment to build");
        getRestClient().addCommentToBuild(build.getPlanKey(), build.getNumber(), comment);
    }

    public void runBuild(final IProgressMonitor monitor, final TaskRepository repository, final BambooBuild build) throws CoreException, RemoteApiException {
        monitor.subTask("Run Build");
        getRestClient().executeBuild(build.getPlanKey());
    }

    public BambooBuild getBuildForPlanAndNumber(final IProgressMonitor monitor, final TaskRepository repository, final String planKey, final int buildNumber,
            final int timezoneOffset) throws CoreException, RemoteApiException {
        monitor.subTask("Retrieving build details");
        return getRestClient().getBuildForPlanAndNumber(planKey, buildNumber, timezoneOffset);
    }

    public void setLocalConfiguration(final BambooLocalConfiguration configuration) {
        Assert.isNotNull(configuration);
        localConfiguration = configuration;
    }

    public RestInfo getServerInfo(final IProgressMonitor monitor) throws RemoteApiException {
        return getRestClient().getServerInfo();
    }

    public UserBean getCurrentUser(final IProgressMonitor monitor) throws RemoteApiException {
        return getRestClient().getCurrentUser();
    }

    public void purgeSession() {
        restClient = createRestClient(location, cache);
    }

    public Object getLocalConfiguration() {
        return localConfiguration;
    }

    public void validate(final IProgressMonitor monitor, final TaskRepository taskRepository) throws RemoteApiException {
        getCurrentUser(monitor);

    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("BambooClient [baseUrl=").append(baseUrl).append(", cache=").append(cache).append(", restClient=").append(restClient)
                .append(", location=").append(location).append(", localConfiguration=").append(localConfiguration).append("]");
        return builder.toString();
    }
}
