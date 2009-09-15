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

package com.atlassian.connector.eclipse.ui.team;

import com.atlassian.theplugin.commons.util.MiscUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Manager for the team resource connectors
 * 
 * @author Shawn Minto
 */
public class TeamResourceManager {

	private final Set<ITeamResourceConnector> teamConnectors = MiscUtil.buildHashSet();

	public TeamResourceManager() {
		teamConnectors.addAll(TeamResourceConnectorExtensionReader.getTeamConnectors());
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
}
