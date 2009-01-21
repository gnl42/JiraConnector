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

import java.util.Set;

/**
 * Manager for the team resource connectors
 * 
 * @author Shawn Minto
 */
public class TeamResourceManager {

	private final Set<ITeamResourceConnector> teamConnectors;

	public TeamResourceManager() {
		teamConnectors = TeamResourceConnectorExtensionReader.getTeamConnectors();
	}

	public Set<ITeamResourceConnector> getTeamConnectors() {
		return teamConnectors;
	}

}
