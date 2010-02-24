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

package com.atlassian.connector.eclipse.team.ui;

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
public class TeamUiResourceManager {

	private final Set<ITeamUiResourceConnector> teamConnectors;

	private static final String EXTENSION_TEAM_CONNECTOR = "com.atlassian.connector.eclipse.team.ui.teamUiConnector";

	private static final String ELEMENT_TEAM_CONNECTOR = "teamUiConnector";

	public TeamUiResourceManager() {
		teamConnectors = ConnectorExtensionReaderUtil.getTeamConnectors(EXTENSION_TEAM_CONNECTOR,
				ELEMENT_TEAM_CONNECTOR, ITeamUiResourceConnector.class);
	}

	@NotNull
	public Set<ITeamUiResourceConnector> getTeamConnectors() {
		return teamConnectors;
	}

	public void addTeamConnector(ITeamUiResourceConnector teamConnector) {
		teamConnectors.add(teamConnector);
	}

	/**
	 * 
	 * @param name
	 *            name you're looking for, to make easier using this method we also accept nulls
	 * @return
	 */
	@Nullable
	public ITeamUiResourceConnector getTeamConnectorByName(@Nullable String name) {
		if (name != null) {
			Set<ITeamUiResourceConnector> connectors = getTeamConnectors();
			for (ITeamUiResourceConnector connector : connectors) {
				if (connector.getName().equals(name)) {
					return connector;
				}
			}
		}
		return null;
	}

	@Nullable
	public ITeamUiResourceConnector getTeamConnector(@NotNull IResource resource) {
		for (ITeamUiResourceConnector connector : getTeamConnectors()) {
			if (connector.isResourceManagedBy(resource)) {
				return connector;
			}
		}
		return null;
	}

}
