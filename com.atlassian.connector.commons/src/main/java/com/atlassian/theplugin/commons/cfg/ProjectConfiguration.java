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
package com.atlassian.theplugin.commons.cfg;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.util.MiscUtil;

import java.util.ArrayList;
import java.util.Collection;

public class ProjectConfiguration {
	private Collection<ServerCfg> servers;

	private ServerIdImpl defaultCrucibleServerId;
	private String defaultCrucibleProject;
	private String defaultCrucibleRepo;
	private ServerIdImpl defaultFishEyeServerId;
	private String defaultFishEyeRepo;
	private String fishEyeProjectPath;
	private ServerIdImpl defaultJiraServerId;

	private static final int HASHCODE_MAGIC = 31;


	public ProjectConfiguration(final ProjectConfiguration other) {
		servers = cloneArrayList(other.getServers());
		defaultCrucibleServerId = other.defaultCrucibleServerId;
		defaultFishEyeServerId = other.defaultFishEyeServerId;
		defaultCrucibleProject = other.defaultCrucibleProject;
		defaultCrucibleRepo = other.defaultCrucibleRepo;
		defaultFishEyeRepo = other.defaultFishEyeRepo;
		fishEyeProjectPath = other.fishEyeProjectPath;
		defaultJiraServerId = other.defaultJiraServerId;
	}

	public static Collection<ServerCfg> cloneArrayList(final Collection<ServerCfg> collection) {
		final ArrayList<ServerCfg> res = new ArrayList<ServerCfg>(collection.size());
		for (ServerCfg serverCfg : collection) {
			res.add(serverCfg.getClone());
		}
		return res;
	}


	public ProjectConfiguration(final Collection<ServerCfg> servers) {
		if (servers == null) {
			throw new NullPointerException("Servers cannot be null");
		}
		this.servers = servers;
	}

	public ProjectConfiguration() {
		this.servers = MiscUtil.buildArrayList();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ProjectConfiguration)) {
			return false;
		}

		final ProjectConfiguration that = (ProjectConfiguration) o;

		if (defaultCrucibleProject != null
				? !defaultCrucibleProject.equals(that.defaultCrucibleProject)
				: that.defaultCrucibleProject != null) {
			return false;
		}
		if (defaultCrucibleServerId != null
				? !defaultCrucibleServerId.equals(that.defaultCrucibleServerId)
				: that.defaultCrucibleServerId != null) {
			return false;
		}
		if (defaultCrucibleRepo != null
				? !defaultCrucibleRepo.equals(that.defaultCrucibleRepo)
				: that.defaultCrucibleRepo != null) {
			return false;
		}
		if (defaultFishEyeServerId != null
				? !defaultFishEyeServerId.equals(that.defaultFishEyeServerId)
				: that.defaultFishEyeServerId != null) {
			return false;
		}
		if (defaultFishEyeRepo != null
				? !defaultFishEyeRepo.equals(that.defaultFishEyeRepo)
				: that.defaultFishEyeRepo != null) {
			return false;
		}
		if (fishEyeProjectPath != null
				? !fishEyeProjectPath.equals(that.fishEyeProjectPath)
				: that.fishEyeProjectPath != null) {
			return false;
		}
		if (defaultJiraServerId != null
				? !defaultJiraServerId.equals(that.defaultJiraServerId)
				: that.defaultJiraServerId != null) {
			return false;
		}

		//noinspection RedundantIfStatement
		if (!servers.equals(that.servers)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = servers.hashCode();
		result = HASHCODE_MAGIC * result + (defaultCrucibleServerId != null ? defaultCrucibleServerId.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (defaultFishEyeServerId != null ? defaultFishEyeServerId.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (defaultCrucibleProject != null ? defaultCrucibleProject.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (defaultCrucibleRepo != null ? defaultCrucibleRepo.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (fishEyeProjectPath != null ? fishEyeProjectPath.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (defaultJiraServerId != null ? defaultJiraServerId.hashCode() : 0);
		return result;
	}

	public Collection<ServerCfg> getServers() {
		return servers;
	}

	public ServerCfg getServerCfg(ServerId serverId) {
		for (ServerCfg serverCfg : servers) {
			if (serverId.equals(serverCfg.getServerId())) {
				return serverCfg;
			}
		}
		return null;
	}

	public static ProjectConfiguration emptyConfiguration() {
		return new ProjectConfiguration();
	}

	public ProjectConfiguration getClone() {
		return new ProjectConfiguration(this);
	}

	public ServerIdImpl getDefaultCrucibleServerId() {
		if (defaultCrucibleServerId == null && getAllCrucibleServers().size() == 1) {

            final CrucibleServerCfg serverCfg = getAllCrucibleServers().iterator().next();
            if (serverCfg.isEnabled()) {
                defaultCrucibleServerId = serverCfg.getServerId();
            }
        }

        return defaultCrucibleServerId;
	}

	public CrucibleServerCfg getDefaultCrucibleServer() {
		if (getDefaultCrucibleServerId() == null) {
			return null;
		}

		ServerCfg serverCfg = getServerCfg(getDefaultCrucibleServerId());

		// no additional check - let IDE handle such error in a standard way (error dialog)
		// in unlikely event of some fuck-up
		final CrucibleServerCfg crucible = (CrucibleServerCfg) serverCfg;
		if (crucible == null || !crucible.isEnabled()) {
			return null;
		}
		return crucible;
	}

	public void setDefaultCrucibleServerId(final ServerIdImpl defaultCrucibleServerId) {
		this.defaultCrucibleServerId = defaultCrucibleServerId;
		if (defaultCrucibleServerId == null) {
			setDefaultCrucibleProject(null);
			setDefaultCrucibleRepo(null);
		}
	}

	public ServerIdImpl getDefaultFishEyeServerId() {
        if (defaultFishEyeServerId == null && getAllFisheyeServers().size() == 1) {
            final FishEyeServerCfg cfg = getAllFisheyeServers().iterator().next();
            if (cfg.isEnabled()) {
                defaultFishEyeServerId = cfg.getServerId();
            }
        }
		return defaultFishEyeServerId;
	}

	public FishEyeServer getDefaultFishEyeServer() {
		if (getDefaultFishEyeServerId() == null) {
			return null;
		}

		final ServerCfg serverCfg = getServerCfg(getDefaultFishEyeServerId());

		// no additional check - let IDE handle such error in a standard way (error dialog)
		// in unlikely event of some fuck-up
		if (serverCfg == null || !serverCfg.isEnabled()) {
			return null;
		}

		FishEyeServer res = serverCfg.asFishEyeServer();
		if (res == null || !res.isEnabled()) {
			return null;
		}
		return res;
	}

	public void setDefaultFishEyeServerId(final ServerIdImpl defaultFishEyeServerId) {
		this.defaultFishEyeServerId = defaultFishEyeServerId;
		if (defaultFishEyeServerId == null) {
			defaultFishEyeRepo = null;
		}
	}

	public ServerId getDefaultJiraServerId() {
        if (defaultJiraServerId == null && getAllJIRAServers().size() == 1) {
            final JiraServerCfg jiraServerCfg = getAllJIRAServers().iterator().next();
            if (jiraServerCfg.isEnabled()) {
                defaultJiraServerId = jiraServerCfg.getServerId();
            }
        }

		return defaultJiraServerId;
	}

	public JiraServerCfg getDefaultJiraServer() {
		if (getDefaultJiraServerId() == null) {
			return null;
		}

		ServerCfg serverCfg = getServerCfg(getDefaultJiraServerId());

		// no additional check - let IDE handle such error in a standard way (error dialog)
		// in unlikely event of some fuck-up
		final JiraServerCfg jiraServerCfg = (JiraServerCfg) serverCfg;
		if (jiraServerCfg == null || !jiraServerCfg.isEnabled()) {
			return null;
		}
		return jiraServerCfg;
	}

	public void setDefaultJiraServerId(final ServerIdImpl defaultJiraServerId) {
		this.defaultJiraServerId = defaultJiraServerId;
	}

	public String getDefaultCrucibleProject() {
		return defaultCrucibleProject;
	}

	public void setDefaultCrucibleProject(final String defaultCrucibleProject) {
		this.defaultCrucibleProject = defaultCrucibleProject;
	}

	public String getDefaultCrucibleRepo() {
		return defaultCrucibleRepo;
	}

	public void setDefaultCrucibleRepo(final String defaultCrucibleRepo) {
		this.defaultCrucibleRepo = defaultCrucibleRepo;
	}

	public String getFishEyeProjectPath() {
		return fishEyeProjectPath;
	}

	public void setFishEyeProjectPath(final String fishEyeProjectPath) {
		this.fishEyeProjectPath = fishEyeProjectPath;
	}

	public String getDefaultFishEyeRepo() {
		return defaultFishEyeRepo;
	}

	public void setDefaultFishEyeRepo(final String defaultFishEyeRepo) {
		this.defaultFishEyeRepo = defaultFishEyeRepo;
	}

	public boolean isDefaultFishEyeServerValid() {
		if (getDefaultFishEyeServerId() == null) {
			return true;
		}

		ServerCfg serverCfg = getServerCfg(getDefaultFishEyeServerId());
		if (serverCfg == null) {
			return false;
		}

		FishEyeServer fishEye = serverCfg.asFishEyeServer();
		return fishEye != null && fishEye.isEnabled();
	}

	public boolean isDefaultCrucibleServerValid() {
		if (getDefaultCrucibleServerId() == null) {
			return true;
		}

		ServerCfg serverCfg = getServerCfg(getDefaultCrucibleServerId());

		// no additional check - let IDE handle such error in a standard way (error dialog)
		// in unlikely event of some fuck-up
		final CrucibleServerCfg crucible = (CrucibleServerCfg) serverCfg;
		return crucible != null && crucible.isEnabled();
	}

	public boolean isDefaultJiraServerValid() {
		if (getDefaultJiraServerId() == null) {
			return true;
		}

		ServerCfg serverCfg = getServerCfg(getDefaultJiraServerId());

		// no additional check - let IDE handle such error in a standard way (error dialog)
		// in unlikely event of some fuck-up
		final JiraServerCfg jiraServerCfg = (JiraServerCfg) serverCfg;
		return jiraServerCfg != null && jiraServerCfg.isEnabled();
	}

	public Collection<JiraServerCfg> getAllJIRAServers() {
		Collection<JiraServerCfg> jiraServers = MiscUtil.buildArrayList();

		for (ServerCfg server : servers) {
			if (server.getServerType() == ServerType.JIRA_SERVER && server instanceof JiraServerCfg) {
				jiraServers.add((JiraServerCfg) server);
			}
		}

		return jiraServers;
	}

	public Collection<BambooServerCfg> getAllBambooServers() {
		Collection<BambooServerCfg> bambooServers = MiscUtil.buildArrayList();

		for (ServerCfg server : servers) {
			if (server.getServerType() == ServerType.BAMBOO_SERVER && server instanceof BambooServerCfg) {
				bambooServers.add((BambooServerCfg) server);
			}
		}

		return bambooServers;
	}

	public Collection<CrucibleServerCfg> getAllCrucibleServers() {
		Collection<CrucibleServerCfg> crucibleServers = MiscUtil.buildArrayList();

		for (ServerCfg server : servers) {
			if (server.getServerType() == ServerType.CRUCIBLE_SERVER && server instanceof CrucibleServerCfg) {
				crucibleServers.add((CrucibleServerCfg) server);
			}
		}
		return crucibleServers;

	}

	public Collection<FishEyeServerCfg> getAllFisheyeServers() {
		Collection<FishEyeServerCfg> fisheyeServers = MiscUtil.buildArrayList();

		for (ServerCfg server : servers) {
			if (server.getServerType() == ServerType.FISHEYE_SERVER && server instanceof FishEyeServerCfg) {
				fisheyeServers.add((FishEyeServerCfg) server);
			}
		}

		return fisheyeServers;
	}

	public Collection<ServerCfg> getAllEnabledServersWithDefaultCredentials() {
		Collection<ServerCfg> defServers = MiscUtil.buildArrayList();
		for (ServerCfg server : servers) {
			if (server.isUseDefaultCredentials() && server.isEnabled()) {
				defServers.add(server);
			}
		}
		return defServers;
	}
}
