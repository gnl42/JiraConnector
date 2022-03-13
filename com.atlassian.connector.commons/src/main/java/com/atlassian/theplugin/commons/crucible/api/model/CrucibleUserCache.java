package com.atlassian.theplugin.commons.crucible.api.model;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import org.jetbrains.annotations.NotNull;

public interface CrucibleUserCache {
	User getUser(@NotNull CrucibleServerFacade2 facade, ConnectionCfg server, String userId, boolean fetchIfNotExist);

	void addUser(ConnectionCfg server, User user);
}
