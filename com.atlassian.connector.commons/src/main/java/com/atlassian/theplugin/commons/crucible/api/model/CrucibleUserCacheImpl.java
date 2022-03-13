package com.atlassian.theplugin.commons.crucible.api.model;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CrucibleUserCacheImpl implements CrucibleUserCache {
	private final Map<ConnectionCfg, Map<String, User>> serverMap = new HashMap<ConnectionCfg, Map<String, User>>();

	public User getUser(@NotNull CrucibleServerFacade2 facade,
			ConnectionCfg server, String userId, boolean fetchIfNotExist) {
		Map<String, User> userMap = serverMap.get(server);
		if (userMap == null && fetchIfNotExist) {
			userMap = new HashMap<String, User>();
			serverMap.put(server, userMap);
			List<User> users;
			try {
				users = facade.getUsers(server);
			} catch (RemoteApiException e) {
				return null;
			} catch (ServerPasswordNotProvidedException e) {
				return null;
			}
			for (User u : users) {
				userMap.put(u.getUsername(), u);
			}
		}
		if (userMap != null) {
			return userMap.get(userId);
		}
		return null;
	}

	public void addUser(ConnectionCfg server, User user) {
		Map<String, User> userMap = serverMap.get(server);
		if (userMap == null) {
			userMap = new HashMap<String, User>();
			serverMap.put(server, userMap);
		}
		userMap.put(user.getUsername(), user);
	}
}
