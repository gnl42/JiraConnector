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

package com.atlassian.theplugin.commons.bamboo;

import com.atlassian.connector.commons.api.BambooServerFacade2;
import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.bamboo.api.AutoRenewBambooSession;
import com.atlassian.theplugin.commons.bamboo.api.BambooServerVersionNumberConstants;
import com.atlassian.theplugin.commons.bamboo.api.BambooSession;
import com.atlassian.theplugin.commons.bamboo.api.BambooSessionImpl;
import com.atlassian.theplugin.commons.bamboo.api.LoginBambooSession;
import com.atlassian.theplugin.commons.cfg.SubscribedPlan;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.ProductSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginFailedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.util.Logger;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Class used for communication wiht Bamboo Server.
 *
 * @author sginter + others Date: Jan 15, 2008
 * @deprecated this class is evil (e.g. due to leaking sessions). You should use directly BambooSession(Impl) instead of this
 */
@Deprecated
public final class BambooServerFacadeImpl implements BambooServerFacade2 {
	private final Map<String, BambooSession> sessions = new WeakHashMap<String, BambooSession>();

	private final Logger logger;

	private final BambooSessionFactory bambooSessionFactory;

	private final HttpSessionCallback callback;

	public BambooServerFacadeImpl(Logger loger, @NotNull BambooSessionFactory factory, @NotNull HttpSessionCallback callback) {
		this.logger = loger;
		this.callback = callback;
		this.bambooSessionFactory = factory;
	}

	public BambooServerFacadeImpl(Logger loger, HttpSessionCallback callback) {
		this(loger, new SimpleBambooSessionFactory(loger), callback);
	}

	public ServerType getServerType() {
		return ServerType.BAMBOO_SERVER;
	}

	public synchronized BambooSession getSession(ConnectionCfg server) throws RemoteApiException {
		// @todo old server will stay on map - remove them !!!
		String key = server.getUsername() + server.getUrl() + server.getPassword() + server.getId();
		BambooSession session = sessions.get(key);
		if (session == null) {
			session = bambooSessionFactory.createSession(server, callback);
			sessions.put(key, session);
		}
		if (!session.isLoggedIn()) {
			session.login(server.getUsername(), server.getPassword().toCharArray());
		}
		return session;
	}

	/**
	 * Test connection to Bamboo server.
	 *
	 * @param httpConnectionCfg
	 *            The configuration for the server that we want to test the connection for
	 * @throws RemoteApiException
	 *             on failed login
	 * @see RemoteApiLoginFailedException
	 */

    public void testServerConnection(ConnectionCfg httpConnectionCfg) throws RemoteApiException {
		ProductSession apiHandler = bambooSessionFactory.createLoginSession(httpConnectionCfg, callback);
		apiHandler.login(httpConnectionCfg.getUsername(), httpConnectionCfg.getPassword().toCharArray());
		apiHandler.logout();
    }

    /**
	 * List projects defined on Bamboo server.
	 *
	 * @param bambooServer Bamboo server information
	 * @return list of projects or null on error
	 * @throws ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 */
	public Collection<BambooProject> getProjectList(ConnectionCfg bambooServer) throws ServerPasswordNotProvidedException,
			RemoteApiException {
		try {
			return getSession(bambooServer).listProjectNames();
		} catch (RemoteApiException e) {
			logger.error("Bamboo exception: " + e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * List plans defined on Bamboo server.
	 *
	 * @param bambooServer Bamboo server information
	 * @return list of plans
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 */
	public Collection<BambooPlan> getPlanList(ConnectionCfg bambooServer) throws ServerPasswordNotProvidedException,
			RemoteApiException {
		BambooSession api = getSession(bambooServer);
		return api.getPlanList();
	}

	/**
	 * List details on subscribed plans.
	 * <p/>
	 * Returns info on all subscribed plans including information about failed attempt.
	 * <p/>
	 * Throws ServerPasswordNotProvidedException when invoked for Server that has not had the password set, when the server
	 * returns a meaningful exception response.
	 *
	 * @param bambooServer
	 *            Bamboo server information
	 * @return results on subscribed builds
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *             when invoked for Server that has not had the password set yet
	 * @throws RemoteApiLoginException
	 *             when login failed
	 * @see com.atlassian.theplugin.commons.bamboo.api.BambooSessionImpl#login(String, char[])
	 */
	Collection<BambooBuild> getSubscribedPlansResultsOld(ConnectionCfg bambooServer,
			final Collection<SubscribedPlan> plans, boolean isUseFavourities, int timezoneOffset)
			throws ServerPasswordNotProvidedException, RemoteApiException {

		// This method is internally somewhat broken. It messes up error handling with proper results
		// generally it hides IntelliJ Connector inability to survive remote exceptions and maintain
		// last displayed status

		final Collection<BambooBuild> builds = new ArrayList<BambooBuild>();
		Throwable connectionError;
		BambooSession api = null;
		try {
			api = getSession(bambooServer);
			connectionError = null;
		} catch (RemoteApiLoginFailedException e) {
			if (bambooServer.getPassword().length() > 0) {
				logger.error("Bamboo login exception: " + e.getMessage());
				connectionError = e;
				throw e;
			} else {
				throw new ServerPasswordNotProvidedException(e);
			}
		} catch (RemoteApiLoginException e) {
			throw e;
		} catch (RemoteApiException e) {
			logger.error("Bamboo exception: " + e.getMessage());
			connectionError = e;
			if (!isUseFavourities) {
				for (SubscribedPlan plan : plans) {
					builds.add(constructBuildErrorInfo(bambooServer, plan.getKey(), null, connectionError == null ? ""
							: connectionError.getMessage(), connectionError));
				}
			}
			return builds;

		}
		return api.getSubscribedPlansResults(plans, isUseFavourities, timezoneOffset);
	}

	public Collection<BambooBuild> getSubscribedPlansResults(ConnectionCfg connectionCfg,
			final Collection<SubscribedPlan> plans, boolean isUseFavourities, boolean isShowBranches, boolean myBranchesOnly, int timezoneOffset)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		final BambooSession session = getSession(connectionCfg);
		if (session.getBamboBuildNumber() >= BambooServerVersionNumberConstants.BAMBOO_1401_BUILD_NUMBER) {
			return getSubscribedPlansResultsNew(
                    connectionCfg, plans, isUseFavourities,
                    isShowBranches && session.getBamboBuildNumber() >= BambooServerVersionNumberConstants.BAMBOO_3600_BUILD_NUMBER,
                    myBranchesOnly,
                    timezoneOffset);
		} else {
			return getSubscribedPlansResultsOld(connectionCfg, plans, isUseFavourities, timezoneOffset);
		}
	}

	/**
	 * This is the new version of {@link #getSubscribedPlansResults(ConnectionCfg, java.util.Collection, boolean, int)} It
	 * returns info about 'building' or 'in queue' state.
	 * <p/>
	 * Throws ServerPasswordNotProvidedException when invoked for Server that has not had the password set, when the server
	 * returns a meaningful exception response.
	 *
	 * @param bambooServer
	 *            Bamboo server information
	 * @param plans
	 * @param isUseFavourities
	 * @param timezoneOffset
	 * @return results on subscribed builds
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *             when invoked for Server that has not had the password set yet
	 * @throws RemoteApiLoginException
	 *             when we cannot log in
	 * @see com.atlassian.theplugin.commons.bamboo.api.BambooSessionImpl#login(String, char[])
	 */
	Collection<BambooBuild> getSubscribedPlansResultsNew(
            ConnectionCfg bambooServer, final Collection<SubscribedPlan> plans,
            boolean isUseFavourities, boolean isShowBranches, boolean myBranchesOnly, int timezoneOffset)
			throws ServerPasswordNotProvidedException, RemoteApiLoginException {
		Collection<BambooBuild> builds = new ArrayList<BambooBuild>();

		Throwable connectionError;
		BambooSession api = null;
		try {
			api = getSession(bambooServer);
			connectionError = null;
		} catch (RemoteApiLoginFailedException e) {
			if (bambooServer.getPassword().length() > 0) {
				logger.error("Bamboo login exception: " + e.getMessage());
				connectionError = e;
				throw e;
			} else {
				throw new ServerPasswordNotProvidedException(e);
			}
		} catch (RemoteApiLoginException e) {
			throw e;
		} catch (RemoteApiException e) {
			logger.error("Bamboo exception: " + e.getMessage());
			connectionError = e;
		}

		Collection<BambooPlan> plansForServer = null;
		try {
			plansForServer = getPlanList(bambooServer);
		} catch (RemoteApiException e) {
			// can go further, no disabled info will be available
			logger.warn("Cannot fetch plan list from Bamboo server [" + bambooServer.getUrl() + "]");
		}

		if (isUseFavourities) {
			if (plansForServer != null) {
				for (BambooPlan bambooPlan : plansForServer) {
					if (bambooPlan.isFavourite()) {
                        String key = bambooPlan.getKey();
                        if (api != null && api.isLoggedIn()) {
							try {
                                if (isShowBranches) {
                                    Collection<String> branches = api.getBranchKeys(key, isUseFavourities, myBranchesOnly);
                                    branches.add(key);
                                    for (String branch : branches) {
                                        BambooBuild buildInfo = api.getLatestBuildForPlanNew(branch, key.equals(branch) ? null : key, bambooPlan.isEnabled(), timezoneOffset);
                                        builds.add(buildInfo);
                                    }
                                } else {
                                    BambooBuild buildInfo = api.getLatestBuildForPlanNew(key, null, bambooPlan.isEnabled(), timezoneOffset);
                                    builds.add(buildInfo);
                                }
							} catch (RemoteApiException e) {
								// go ahead, there are other builds
								logger.warn("Cannot fetch latest build for plan [" + key
										+ "] from Bamboo server [" + bambooServer.getUrl() + "]");
							}
						} else {
							builds.add(constructBuildErrorInfo(bambooServer, key,
									bambooPlan.getName(), connectionError == null ? ""
									: connectionError.getMessage(), connectionError));
						}
					}
				}
			}
		} else {
			for (SubscribedPlan plan : plans) {
                String key = plan.getKey();
                if (api != null && api.isLoggedIn()) {
					try {
                        Boolean planEnabled = plansForServer != null
                            ? BambooSessionImpl.isPlanEnabled(plansForServer, key)
                            : null;
                        if (isShowBranches) {
                            Collection<String> branches = api.getBranchKeys(key, isUseFavourities, myBranchesOnly);
                            branches.add(key);
                            for (String branch : branches) {
                                BambooBuild buildInfo = api.getLatestBuildForPlanNew(branch, key.equals(branch) ? null : key, planEnabled != null && planEnabled, timezoneOffset);
                                builds.add(buildInfo);
                            }
                        } else {
                            BambooBuild buildInfo = api.getLatestBuildForPlanNew(key, null, planEnabled != null && planEnabled, timezoneOffset);
						    builds.add(buildInfo);
                        }
					} catch (RemoteApiException e) {
						// go ahead, there are other builds
						// go ahead, there are other builds
						logger.warn("Cannot fetch latest build for plan [" + key + "] from Bamboo server ["
								+ bambooServer.getUrl() + "]");
					}
				} else {
					builds.add(constructBuildErrorInfo(bambooServer, key, null, connectionError == null ? ""
							: connectionError.getMessage(), connectionError));
				}
			}
		}

		return builds;
	}

	/**
	 * List history for provided plan.
	 * <p/>
	 * Returns last 15 builds on provided plan including information about failed attempt.
	 * <p/>
	 * <p/>
	 * Throws ServerPasswordNotProvidedException when invoked for Server that has not had the password set, when the
	 * server returns a meaningful exception response.
	 *
	 * @param bambooServer   Bamboo server information
	 * @param planKey		key of the plan to query
	 * @param timezoneOffset
	 * @return results on history for plan
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 * @see com.atlassian.theplugin.commons.bamboo.api.BambooSessionImpl#login(String, char[])
	 */
	public Collection<BambooBuild> getRecentBuildsForPlans(ConnectionCfg bambooServer, String planKey,
			final int timezoneOffset) throws ServerPasswordNotProvidedException {
		Collection<BambooBuild> builds = new ArrayList<BambooBuild>();

		BambooSession api;
		try {
			api = getSession(bambooServer);
		} catch (RemoteApiLoginFailedException e) {
			// TODO wseliga used to be bambooServer.getIsConfigInitialized() here
			if (bambooServer.getPassword().length() > 0) {
				logger.error("Bamboo login exception: " + e.getMessage());
				builds.add(constructBuildErrorInfo(bambooServer, planKey, null, e.getMessage(), e));
				return builds;
			} else {
				throw new ServerPasswordNotProvidedException(e);
			}
		} catch (RemoteApiException e) {
			logger.error("Bamboo exception: " + e.getMessage());
			builds.add(constructBuildErrorInfo(bambooServer, planKey, null, e.getMessage(), e));
			return builds;
		}

		try {
			builds.addAll(api.getRecentBuildsForPlan(planKey, timezoneOffset));
		} catch (RemoteApiException e) {
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
	 * Throws ServerPasswordNotProvidedException when invoked for Server that has not had the password set, when the
	 * server returns a meaningful exception response.
	 *
	 * @param bambooServer Bamboo server information
	 * @return results on history for plan
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 * @see com.atlassian.theplugin.commons.bamboo.api.BambooSessionImpl#login(String, char[])
	 */
	public Collection<BambooBuild> getRecentBuildsForUser(ConnectionCfg bambooServer, final int timezoneOffset)
			throws ServerPasswordNotProvidedException {
		Collection<BambooBuild> builds = new ArrayList<BambooBuild>();

		BambooSession api;
		try {
			api = getSession(bambooServer);
		} catch (RemoteApiLoginFailedException e) {
			if (bambooServer.getPassword().length() > 0) {
				logger.error("Bamboo login exception: " + e.getMessage());
				builds.add(constructBuildErrorInfo(bambooServer, "", null, e.getMessage(), e));
				return builds;
			} else {
				throw new ServerPasswordNotProvidedException(e);
			}
		} catch (RemoteApiException e) {
			logger.error("Bamboo exception: " + e.getMessage());
			builds.add(constructBuildErrorInfo(bambooServer, "", null, e.getMessage(), e));
			return builds;
		}

		try {
			builds.addAll(api.getRecentBuildsForUser(timezoneOffset));
		} catch (RemoteApiException e) {
			logger.error("Bamboo exception: " + e.getMessage());
			builds.add(constructBuildErrorInfo(bambooServer, "", null, e.getMessage(), e));
		}

		return builds;
	}

	/**
	 * @param bambooServer server data
	 * @param planKey	  key of the build
	 * @param buildNumber  unique number of the build
	 * @return build data
	 * @throws ServerPasswordNotProvidedException
	 *
	 * @throws RemoteApiException
	 */
	public BuildDetails getBuildDetails(ConnectionCfg bambooServer, @NotNull String planKey, int buildNumber)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		try {
			BambooSession api = getSession(bambooServer);
			return api.getBuildResultDetails(planKey, buildNumber);
		} catch (RemoteApiException e) {
			logger.info("Bamboo exception: " + e.getMessage());
			throw e;
		}
	}

	public BambooBuild getBuildForPlanAndNumber(ConnectionCfg bambooServer, @NotNull String planKey,
			final int buildNumber, final int timezoneOffset)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		try {
			BambooSession api = getSession(bambooServer);
			return api.getBuildForPlanAndNumber(planKey, buildNumber, timezoneOffset);
		} catch (RemoteApiException e) {
			logger.info("Bamboo exception: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * @param bambooServer server data
	 * @param planKey	  key of the build
	 * @param buildNumber  unique number of the build
	 * @param buildLabel   label to add to the build
	 * @throws ServerPasswordNotProvidedException
	 *
	 * @throws RemoteApiException
	 */
	public void addLabelToBuild(ConnectionCfg bambooServer, @NotNull String planKey, int buildNumber, String buildLabel)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		try {
			BambooSession api = getSession(bambooServer);
			api.addLabelToBuild(planKey, buildNumber, buildLabel);
		} catch (RemoteApiException e) {
			logger.info("Bamboo exception: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * @param bambooServer server data
	 * @param planKey	  key of the build
	 * @param buildNumber  unique number of the build
	 * @param buildComment user comment to add to the build
	 * @throws ServerPasswordNotProvidedException
	 *
	 * @throws RemoteApiException
	 */
	public void addCommentToBuild(ConnectionCfg bambooServer, @NotNull String planKey, int buildNumber, String buildComment)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		try {
			BambooSession api = getSession(bambooServer);
			api.addCommentToBuild(planKey, buildNumber, buildComment);
		} catch (RemoteApiException e) {
			logger.info("Bamboo exception: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * Runs selected plan
	 *
	 * @param bambooServer server data
	 * @param buildKey	 key of the build
	 * @throws ServerPasswordNotProvidedException
	 *
	 * @throws RemoteApiException
	 */
	public void executeBuild(ConnectionCfg bambooServer, @NotNull String buildKey)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		try {
			BambooSession api = getSession(bambooServer);
			api.executeBuild(buildKey);
		} catch (RemoteApiException e) {
			logger.info("Bamboo exception: " + e.getMessage());
			throw e;
		}
	}

	public String getBuildLogs(ConnectionCfg bambooServer, @NotNull String planKey, int buildNumber)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		try {
			BambooSession api = getSession(bambooServer);
			return api.getBuildLogs(planKey, buildNumber);
		} catch (RemoteApiException e) {
			logger.info("Bamboo exception: " + e.getMessage());
			throw e;
		}
	}

    public Collection<BuildIssue> getIssuesForBuild(ConnectionCfg bambooServer, @NotNull String planKey, int buildNumber)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            BambooSession api = getSession(bambooServer);
            return api.getIssuesForBuild(planKey, buildNumber);
        } catch (RemoteApiException e) {
            logger.info("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }

    /**
	 * List plans defined on Bamboo server.
	 *
	 * @param bambooServer Bamboo server information
	 * @return list of plans or null on error
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 * @throws com.atlassian.theplugin.commons.remoteapi.RemoteApiException
	 *          in case of some IO or similar problem
	 */
	public Collection<String> getFavouritePlans(ConnectionCfg bambooServer) throws ServerPasswordNotProvidedException,
			RemoteApiException {
		try {
			return getSession(bambooServer).getFavouriteUserPlans();
		} catch (RemoteApiException e) {
			logger.error("Bamboo exception: " + e.getMessage());
			throw e;
		}
	}

	private BambooBuild constructBuildErrorInfo(ConnectionCfg server, @NotNull String planKey, String planName,
			String message, Throwable exception) {
		return new BambooBuildInfo.Builder(planKey, null, server, planName, null, BuildStatus.UNKNOWN).errorMessage(
				message, exception).pollingTime(new Date()).build();
	}

	private static class SimpleBambooSessionFactory implements BambooSessionFactory {

		private final Logger logger;

		SimpleBambooSessionFactory(Logger logger) {
			this.logger = logger;
		}

		public BambooSession createSession(final ConnectionCfg serverData, final HttpSessionCallback callback)
				throws RemoteApiException {
			return new AutoRenewBambooSession(serverData, callback, logger);
		}

		public ProductSession createLoginSession(final ConnectionCfg serverData, final HttpSessionCallback callback)
				throws RemoteApiMalformedUrlException {
			return new LoginBambooSession(serverData, callback);
		}
	}
}
