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

package com.atlassian.connector.eclipse.team.core;


import org.eclipse.core.resources.IResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Manager for the team resource connectors
 * 
 * @author Shawn Minto
 * @author Wojciech Seliga
 */
public class TeamResourceManager {

	private final Set<ITeamResourceConnector> teamConnectors;

	private static final String EXTENSION_TEAM_CONNECTOR = "com.atlassian.connector.eclipse.team.ui.teamUiConnector";

	private static final String ELEMENT_TEAM_CONNECTOR = "teamUiConnector";

	public TeamResourceManager() {
		teamConnectors = ConnectorExtensionReaderUtil.getTeamConnectors(EXTENSION_TEAM_CONNECTOR,
				ELEMENT_TEAM_CONNECTOR, ITeamResourceConnector.class);
	}

	@NotNull
	public Set<ITeamResourceConnector> getTeamConnectors() {
		return teamConnectors;
	}

	/**
	 * 
	 * @param name
	 *            name you're looking for, to make easier using this method we also accept nulls
	 * @return
	 */
	@Nullable
	public ITeamResourceConnector getTeamConnectorByName(@Nullable String name) {
		if (name != null) {
			Set<ITeamResourceConnector> connectors = getTeamConnectors();
			for (ITeamResourceConnector connector : connectors) {
				if (connector.getName().equals(name)) {
					return connector;
				}
			}
		}
		return null;
	}

	@Nullable
	public ITeamResourceConnector getTeamConnector(@NotNull IResource resource) {
		for (ITeamResourceConnector connector : getTeamConnectors()) {
			if (connector.isResourceManagedBy(resource)) {
				return connector;
			}
		}
		return null;
	}

}
