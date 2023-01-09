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

package me.glindholm.theplugin.commons.bamboo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNull;

import me.glindholm.bamboo.api.BuildApi;
import me.glindholm.bamboo.invoker.ApiClient;
import me.glindholm.bamboo.invoker.ApiException;
import me.glindholm.bamboo.model.RestPlans;
import me.glindholm.connector.commons.api.BambooServerFacade2;
import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.theplugin.commons.ServerType;
import me.glindholm.theplugin.commons.bamboo.api.AutoRenewBambooSession;
import me.glindholm.theplugin.commons.bamboo.api.BambooSession;
import me.glindholm.theplugin.commons.bamboo.api.BambooSessionImpl;
import me.glindholm.theplugin.commons.bamboo.api.LoginBambooSession;
import me.glindholm.theplugin.commons.cfg.SubscribedPlan;
import me.glindholm.theplugin.commons.exception.ServerPasswordNotProvidedException;
import me.glindholm.theplugin.commons.remoteapi.ProductSession;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiLoginException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiLoginFailedException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import me.glindholm.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import me.glindholm.theplugin.commons.util.Logger;

/**
 * Class used for communication wiht Bamboo Server.
 *
 * @author sginter + others Date: Jan 15, 2008
 * @deprecated this class is evil (e.g. due to leaking sessions). You should use
 *             directly BambooSession(Impl) instead of this
 */
@Deprecated
public final class BambooServerFacadeImpl implements BambooServerFacade2 {
    private final Map<String, BambooSession> sessions = new WeakHashMap<>();

    private final Logger logger;

    private final BambooSessionFactory bambooSessionFactory;

    private final HttpSessionCallback callback;

    public BambooServerFacadeImpl(final Logger loger, @NonNull final BambooSessionFactory factory, @NonNull final HttpSessionCallback callback) {
        logger = loger;
        this.callback = callback;
        bambooSessionFactory = factory;
    }

    public BambooServerFacadeImpl(final Logger loger, final HttpSessionCallback callback) {
        this(loger, new SimpleBambooSessionFactory(loger), callback);
    }

    @Override
    public ServerType getServerType() {
        return ServerType.BAMBOO_SERVER;
    }

    @Override
    public synchronized BambooSession getSession(final ConnectionCfg server) throws RemoteApiException {
        // @todo old server will stay on map - remove them !!!
        final String username = server.getUsername();
        final String password = server.getPassword();
        final String key = username + server.getUrl() + password + server.getId();
        BambooSession session = sessions.get(key);
        if (session == null) {
            session = bambooSessionFactory.createSession(server, callback);
            sessions.put(key, session);
        }
        if (!session.isLoggedIn()) {
            session.login(username, password.toCharArray());
        }
        return session;
    }

    /**
     * Test connection to Bamboo server.
     *
     * @param httpConnectionCfg The configuration for the server that we want to
     *                          test the connection for
     * @throws RemoteApiException on failed login
     * @see RemoteApiLoginFailedException
     */

    @Override
    public void testServerConnection(final ConnectionCfg httpConnectionCfg) throws RemoteApiException {
        final ProductSession apiHandler = bambooSessionFactory.createLoginSession(httpConnectionCfg, callback);
        apiHandler.login(httpConnectionCfg.getUsername(), httpConnectionCfg.getPassword().toCharArray());
        apiHandler.logout();
    }

    /**
     * List projects defined on Bamboo server.
     *
     * @param bambooServer Bamboo server information
     * @return list of projects or null on error
     * @throws ServerPasswordNotProvidedException when invoked for Server that has
     *                                            not had the password set yet
     */
    @Override
    public Collection<BambooProject> getProjectList(final ConnectionCfg bambooServer) throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            return getSession(bambooServer).listProjectNames();
        } catch (final RemoteApiException e) {
            logger.error("Bamboo exception: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * List plans defined on Bamboo server.
     *
     * @param bambooServer Bamboo server information
     * @return list of plans
     * @throws me.glindholm.theplugin.commons.exception.ServerPasswordNotProvidedException when
     *                                                                                     invoked
     *                                                                                     for
     *                                                                                     Server
     *                                                                                     that
     *                                                                                     has
     *                                                                                     not
     *                                                                                     had
     *                                                                                     the
     *                                                                                     password
     *                                                                                     set
     *                                                                                     yet
     */
    @Override
    public Collection<BambooPlan> getPlanList(final ConnectionCfg bambooServer) throws ServerPasswordNotProvidedException, RemoteApiException {
        final BambooSession api = getSession(bambooServer);
        return api.getPlanList();
    }

    /**
     * List details on subscribed plans.
     * <p/>
     * Returns info on all subscribed plans including information about failed
     * attempt.
     * <p/>
     * Throws ServerPasswordNotProvidedException when invoked for Server that has
     * not had the password set, when the server returns a meaningful exception
     * response.
     *
     * @param bambooServer Bamboo server information
     * @return results on subscribed builds
     * @throws me.glindholm.theplugin.commons.exception.ServerPasswordNotProvidedException when
     *                                                                                     invoked
     *                                                                                     for
     *                                                                                     Server
     *                                                                                     that
     *                                                                                     has
     *                                                                                     not
     *                                                                                     had
     *                                                                                     the
     *                                                                                     password
     *                                                                                     set
     *                                                                                     yet
     * @throws RemoteApiLoginException                                                     when
     *                                                                                     login
     *                                                                                     failed
     * @see me.glindholm.theplugin.commons.bamboo.api.BambooSessionImpl#login(String,
     *      char[])
     */
    Collection<BambooBuild> getSubscribedPlansResultsOld(final ConnectionCfg bambooServer, final Collection<SubscribedPlan> plans,
            final boolean isUseFavourities, final int timezoneOffset) throws ServerPasswordNotProvidedException, RemoteApiException {

        // This method is internally somewhat broken. It messes up error handling with
        // proper results
        // generally it hides IntelliJ Connector inability to survive remote exceptions
        // and maintain
        // last displayed status

        final Collection<BambooBuild> builds = new ArrayList<>();
        Throwable connectionError;
        BambooSession api = null;
        try {
            api = getSession(bambooServer);
            connectionError = null;
        } catch (final RemoteApiLoginFailedException e) {
            if (bambooServer.getPassword().length() > 0) {
                logger.error("Bamboo login exception: " + e.getMessage());
                connectionError = e;
                throw e;
            } else {
                throw new ServerPasswordNotProvidedException(e);
            }
        } catch (final RemoteApiLoginException e) {
            throw e;
        } catch (final RemoteApiException e) {
            logger.error("Bamboo exception: " + e.getMessage());
            connectionError = e;
            if (!isUseFavourities) {
                for (final SubscribedPlan plan : plans) {
                    builds.add(constructBuildErrorInfo(bambooServer, plan.getKey(), null, connectionError == null ? "" : connectionError.getMessage(),
                            connectionError));
                }
            }
            return builds;

        }
        return api.getSubscribedPlansResults(plans, isUseFavourities, timezoneOffset);
    }

    @Override
    public Collection<BambooBuild> getSubscribedPlansResults(final ConnectionCfg connectionCfg, final Collection<SubscribedPlan> plans,
            final boolean isUseFavourities, final boolean isShowBranches, final boolean myBranchesOnly, final int timezoneOffset)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        final BambooSession session = getSession(connectionCfg);
        return getSubscribedPlansResultsNew(connectionCfg, plans, isUseFavourities, isShowBranches && true, myBranchesOnly, timezoneOffset);
    }

    /**
     * This is the new version of
     * {@link #getSubscribedPlansResults(ConnectionCfg, java.util.Collection, boolean, int)}
     * It returns info about 'building' or 'in queue' state.
     * <p/>
     * Throws ServerPasswordNotProvidedException when invoked for Server that has
     * not had the password set, when the server returns a meaningful exception
     * response.
     *
     * @param bambooServer     Bamboo server information
     * @param plans
     * @param isUseFavourities
     * @param timezoneOffset
     * @return results on subscribed builds
     * @throws me.glindholm.theplugin.commons.exception.ServerPasswordNotProvidedException when
     *                                                                                     invoked
     *                                                                                     for
     *                                                                                     Server
     *                                                                                     that
     *                                                                                     has
     *                                                                                     not
     *                                                                                     had
     *                                                                                     the
     *                                                                                     password
     *                                                                                     set
     *                                                                                     yet
     * @throws RemoteApiLoginException                                                     when
     *                                                                                     we
     *                                                                                     cannot
     *                                                                                     log
     *                                                                                     in
     * @see me.glindholm.theplugin.commons.bamboo.api.BambooSessionImpl#login(String,
     *      char[])
     */
    Collection<BambooBuild> getSubscribedPlansResultsNew(final ConnectionCfg bambooServer, final Collection<SubscribedPlan> plans,
            final boolean isUseFavourities, final boolean isShowBranches, final boolean myBranchesOnly, final int timezoneOffset)
            throws ServerPasswordNotProvidedException, RemoteApiLoginException {
        final Collection<BambooBuild> builds = new ArrayList<>();

        Throwable connectionError;
        BambooSession api = null;
        try {
            api = getSession(bambooServer);
            connectionError = null;
        } catch (final RemoteApiLoginFailedException e) {
            if (bambooServer.getPassword().length() > 0) {
                logger.error("Bamboo login exception: " + e.getMessage());
                connectionError = e;
                throw e;
            } else {
                throw new ServerPasswordNotProvidedException(e);
            }
        } catch (final RemoteApiLoginException e) {
            throw e;
        } catch (final RemoteApiException e) {
            logger.error("Bamboo exception: " + e.getMessage());
            connectionError = e;
        }

        Collection<BambooPlan> plansForServer = null;
        try {
            plansForServer = getPlanList(bambooServer);
        } catch (final RemoteApiException e) {
            // can go further, no disabled info will be available
            logger.warn("Cannot fetch plan list from Bamboo server [" + bambooServer.getUrl() + "]");
        }
        final int i = 1;
        final ApiClient apiClient = bambooServer.getApiClient();
        final BuildApi build = new BuildApi(apiClient);
        RestPlans plansForServer2 = null;
        try {
            plansForServer2 = build.getAllPlanList("plans", null, 5000).get();
        } catch (InterruptedException | ExecutionException | ApiException e1) {
            e1.printStackTrace();
        }
        if (isUseFavourities) {
            if (plansForServer != null) {
                for (final BambooPlan bambooPlan : plansForServer) {
                    if (bambooPlan.isFavourite()) {
                        final String key = bambooPlan.getKey();
                        if (api != null && api.isLoggedIn()) {
                            try {
                                if (isShowBranches) {
                                    final Collection<String> branches = api.getBranchKeys(key, isUseFavourities, myBranchesOnly);
                                    branches.add(key);
                                    for (final String branch : branches) {
                                        final BambooBuild buildInfo = api.getLatestBuildForPlanNew(branch, key.equals(branch) ? null : key,
                                                bambooPlan.isEnabled(), timezoneOffset);
                                        builds.add(buildInfo);
                                    }
                                } else {
                                    final BambooBuild buildInfo = api.getLatestBuildForPlanNew(key, null, bambooPlan.isEnabled(), timezoneOffset);
                                    builds.add(buildInfo);
                                }
                            } catch (final RemoteApiException e) {
                                // go ahead, there are other builds
                                logger.warn("Cannot fetch latest build for plan [" + key + "] from Bamboo server [" + bambooServer.getUrl() + "]");
                            }
                        } else {
                            builds.add(constructBuildErrorInfo(bambooServer, key, bambooPlan.getName(),
                                    connectionError == null ? "" : connectionError.getMessage(), connectionError));
                        }
                    }
                }
            }
        } else {
            for (final SubscribedPlan plan : plans) {
                final String key = plan.getKey();
                if (api != null && api.isLoggedIn()) {
                    try {
                        final Boolean planEnabled = plansForServer != null ? BambooSessionImpl.isPlanEnabled(plansForServer, key) : null;
                        if (isShowBranches) {
                            final Collection<String> branches = api.getBranchKeys(key, isUseFavourities, myBranchesOnly);
                            branches.add(key);
                            for (final String branch : branches) {
                                final BambooBuild buildInfo = api.getLatestBuildForPlanNew(branch, key.equals(branch) ? null : key,
                                        planEnabled != null && planEnabled, timezoneOffset);
                                builds.add(buildInfo);
                            }
                        } else {
                            final BambooBuild buildInfo = api.getLatestBuildForPlanNew(key, null, planEnabled != null && planEnabled, timezoneOffset);
                            builds.add(buildInfo);
                        }
                    } catch (final RemoteApiException e) {
                        // go ahead, there are other builds
                        // go ahead, there are other builds
                        logger.warn("Cannot fetch latest build for plan [" + key + "] from Bamboo server [" + bambooServer.getUrl() + "]");
                    }
                } else {
                    builds.add(constructBuildErrorInfo(bambooServer, key, null, connectionError == null ? "" : connectionError.getMessage(), connectionError));
                }
            }
        }

        return builds;
    }

    /**
     * List history for provided plan.
     * <p/>
     * Returns last 15 builds on provided plan including information about failed
     * attempt.
     * <p/>
     * <p/>
     * Throws ServerPasswordNotProvidedException when invoked for Server that has
     * not had the password set, when the server returns a meaningful exception
     * response.
     *
     * @param bambooServer   Bamboo server information
     * @param planKey        key of the plan to query
     * @param timezoneOffset
     * @return results on history for plan
     * @throws me.glindholm.theplugin.commons.exception.ServerPasswordNotProvidedException when
     *                                                                                     invoked
     *                                                                                     for
     *                                                                                     Server
     *                                                                                     that
     *                                                                                     has
     *                                                                                     not
     *                                                                                     had
     *                                                                                     the
     *                                                                                     password
     *                                                                                     set
     *                                                                                     yet
     * @see me.glindholm.theplugin.commons.bamboo.api.BambooSessionImpl#login(String,
     *      char[])
     */
    @Override
    public Collection<BambooBuild> getRecentBuildsForPlans(final ConnectionCfg bambooServer, final String planKey, final int timezoneOffset)
            throws ServerPasswordNotProvidedException {
        final Collection<BambooBuild> builds = new ArrayList<>();

        BambooSession api;
        try {
            api = getSession(bambooServer);
        } catch (final RemoteApiLoginFailedException e) {
            // TODO wseliga used to be bambooServer.getIsConfigInitialized() here
            if (bambooServer.getPassword().length() > 0) {
                logger.error("Bamboo login exception: " + e.getMessage());
                builds.add(constructBuildErrorInfo(bambooServer, planKey, null, e.getMessage(), e));
                return builds;
            } else {
                throw new ServerPasswordNotProvidedException(e);
            }
        } catch (final RemoteApiException e) {
            logger.error("Bamboo exception: " + e.getMessage());
            builds.add(constructBuildErrorInfo(bambooServer, planKey, null, e.getMessage(), e));
            return builds;
        }

        try {
            builds.addAll(api.getRecentBuildsForPlan(planKey, timezoneOffset));
        } catch (final RemoteApiException e) {
            logger.error("Bamboo exception: " + e.getMessage());
            builds.add(constructBuildErrorInfo(bambooServer, planKey, null, e.getMessage(), e));
        }

        return builds;
    }

    /**
     * List history for current user.
     * <p/>
     * <p/>
     * Returns last builds selected user including information about failed attempt.
     * <p/>
     * <p/>
     * Throws ServerPasswordNotProvidedException when invoked for Server that has
     * not had the password set, when the server returns a meaningful exception
     * response.
     *
     * @param bambooServer Bamboo server information
     * @return results on history for plan
     * @throws me.glindholm.theplugin.commons.exception.ServerPasswordNotProvidedException when
     *                                                                                     invoked
     *                                                                                     for
     *                                                                                     Server
     *                                                                                     that
     *                                                                                     has
     *                                                                                     not
     *                                                                                     had
     *                                                                                     the
     *                                                                                     password
     *                                                                                     set
     *                                                                                     yet
     * @see me.glindholm.theplugin.commons.bamboo.api.BambooSessionImpl#login(String,
     *      char[])
     */
    @Override
    public Collection<BambooBuild> getRecentBuildsForUser(final ConnectionCfg bambooServer, final int timezoneOffset)
            throws ServerPasswordNotProvidedException {
        final Collection<BambooBuild> builds = new ArrayList<>();

        BambooSession api;
        try {
            api = getSession(bambooServer);
        } catch (final RemoteApiLoginFailedException e) {
            if (bambooServer.getPassword().length() > 0) {
                logger.error("Bamboo login exception: " + e.getMessage());
                builds.add(constructBuildErrorInfo(bambooServer, "", null, e.getMessage(), e));
                return builds;
            } else {
                throw new ServerPasswordNotProvidedException(e);
            }
        } catch (final RemoteApiException e) {
            logger.error("Bamboo exception: " + e.getMessage());
            builds.add(constructBuildErrorInfo(bambooServer, "", null, e.getMessage(), e));
            return builds;
        }

        try {
            builds.addAll(api.getRecentBuildsForUser(timezoneOffset));
        } catch (final RemoteApiException e) {
            logger.error("Bamboo exception: " + e.getMessage());
            builds.add(constructBuildErrorInfo(bambooServer, "", null, e.getMessage(), e));
        }

        return builds;
    }

    /**
     * @param bambooServer server data
     * @param planKey      key of the build
     * @param buildNumber  unique number of the build
     * @return build data
     * @throws ServerPasswordNotProvidedException
     *
     * @throws RemoteApiException
     */
    @Override
    public BuildDetails getBuildDetails(final ConnectionCfg bambooServer, @NonNull final String planKey, final int buildNumber)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            final BambooSession api = getSession(bambooServer);
            return api.getBuildResultDetails(planKey, buildNumber);
        } catch (final RemoteApiException e) {
            logger.info("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public BambooBuild getBuildForPlanAndNumber(final ConnectionCfg bambooServer, @NonNull final String planKey, final int buildNumber,
            final int timezoneOffset) throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            final BambooSession api = getSession(bambooServer);
            return api.getBuildForPlanAndNumber(planKey, buildNumber, timezoneOffset);
        } catch (final RemoteApiException e) {
            logger.info("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }

    /**
     * @param bambooServer server data
     * @param planKey      key of the build
     * @param buildNumber  unique number of the build
     * @param buildLabel   label to add to the build
     * @throws ServerPasswordNotProvidedException
     *
     * @throws RemoteApiException
     */
    @Override
    public void addLabelToBuild(final ConnectionCfg bambooServer, @NonNull final String planKey, final int buildNumber, final String buildLabel)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            final BambooSession api = getSession(bambooServer);
            api.addLabelToBuild(planKey, buildNumber, buildLabel);
        } catch (final RemoteApiException e) {
            logger.info("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }

    /**
     * @param bambooServer server data
     * @param planKey      key of the build
     * @param buildNumber  unique number of the build
     * @param buildComment user comment to add to the build
     * @throws ServerPasswordNotProvidedException
     *
     * @throws RemoteApiException
     */
    @Override
    public void addCommentToBuild(final ConnectionCfg bambooServer, @NonNull final String planKey, final int buildNumber, final String buildComment)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            final BambooSession api = getSession(bambooServer);
            api.addCommentToBuild(planKey, buildNumber, buildComment);
        } catch (final RemoteApiException e) {
            logger.info("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Runs selected plan
     *
     * @param bambooServer server data
     * @param buildKey     key of the build
     * @throws ServerPasswordNotProvidedException
     *
     * @throws RemoteApiException
     */
    @Override
    public void executeBuild(final ConnectionCfg bambooServer, @NonNull final String buildKey) throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            final BambooSession api = getSession(bambooServer);
            api.executeBuild(buildKey);
        } catch (final RemoteApiException e) {
            logger.info("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public String getBuildLogs(final ConnectionCfg bambooServer, @NonNull final String planKey, final int buildNumber)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            final BambooSession api = getSession(bambooServer);
            return api.getBuildLogs(planKey, buildNumber);
        } catch (final RemoteApiException e) {
            logger.info("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Collection<BuildIssue> getIssuesForBuild(final ConnectionCfg bambooServer, @NonNull final String planKey, final int buildNumber)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            final BambooSession api = getSession(bambooServer);
            return api.getIssuesForBuild(planKey, buildNumber);
        } catch (final RemoteApiException e) {
            logger.info("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }

    /**
     * List plans defined on Bamboo server.
     *
     * @param bambooServer Bamboo server information
     * @return list of plans or null on error
     * @throws me.glindholm.theplugin.commons.exception.ServerPasswordNotProvidedException when
     *                                                                                     invoked
     *                                                                                     for
     *                                                                                     Server
     *                                                                                     that
     *                                                                                     has
     *                                                                                     not
     *                                                                                     had
     *                                                                                     the
     *                                                                                     password
     *                                                                                     set
     *                                                                                     yet
     * @throws me.glindholm.theplugin.commons.remoteapi.RemoteApiException                 in
     *                                                                                     case
     *                                                                                     of
     *                                                                                     some
     *                                                                                     IO
     *                                                                                     or
     *                                                                                     similar
     *                                                                                     problem
     */
    public Collection<String> getFavouritePlans(final ConnectionCfg bambooServer) throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            return getSession(bambooServer).getFavouriteUserPlans();
        } catch (final RemoteApiException e) {
            logger.error("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }

    private BambooBuild constructBuildErrorInfo(final ConnectionCfg server, @NonNull final String planKey, final String planName, final String message,
            final Throwable exception) {
        return new BambooBuildInfo.Builder(planKey, null, server, planName, null, BuildStatus.UNKNOWN).errorMessage(message, exception)
                .pollingTime(Instant.now()).build();
    }

    private static class SimpleBambooSessionFactory implements BambooSessionFactory {

        private final Logger logger;

        SimpleBambooSessionFactory(final Logger logger) {
            this.logger = logger;
        }

        @Override
        public BambooSession createSession(final ConnectionCfg serverData, final HttpSessionCallback callback) throws RemoteApiException {
            return new AutoRenewBambooSession(serverData, callback, logger);
        }

        @Override
        public ProductSession createLoginSession(final ConnectionCfg serverData, final HttpSessionCallback callback) throws RemoteApiMalformedUrlException {
            return new LoginBambooSession(serverData, callback);
        }
    }
}
