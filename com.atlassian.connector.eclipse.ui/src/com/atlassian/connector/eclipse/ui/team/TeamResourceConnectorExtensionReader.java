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

import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility read the teamConnectors extension point
 * 
 * @author Shawn Minto
 */
public final class TeamResourceConnectorExtensionReader {

	private static final String EXTENSION_TEAM_CONNECTOR = "com.atlassian.connector.eclipse.ui.teamConnector";

	private static final String ELEMENT_TEAM_CONNECTOR = "teamConnector";

	private static final String ATTRIBUTE_CLASS = "class";

	private static Set<ITeamResourceConnector> teamConnectors;

	private TeamResourceConnectorExtensionReader() {
	}

	public static synchronized Set<ITeamResourceConnector> getTeamConnectors() {
		if (teamConnectors == null) {
			readTeamConnectors();
		}

		return teamConnectors;
	}

	private static void readTeamConnectors() {
		teamConnectors = new HashSet<ITeamResourceConnector>();

		IExtensionRegistry registry = Platform.getExtensionRegistry();

		IExtensionPoint teamConnectorExtensionPoint = registry.getExtensionPoint(EXTENSION_TEAM_CONNECTOR);
		if (teamConnectorExtensionPoint != null) {
			for (IExtension templateExtension : teamConnectorExtensionPoint.getExtensions()) {
				IConfigurationElement[] elements = templateExtension.getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if (element.getName().equals(ELEMENT_TEAM_CONNECTOR)) {
						readTeamConnector(element);
					}
				}
			}
		}
	}

	private static void readTeamConnector(IConfigurationElement element) {
		try {
			Object object = element.createExecutableExtension(ATTRIBUTE_CLASS);
			if (!(object instanceof ITeamResourceConnector)) {
				StatusHandler.log(new Status(IStatus.WARNING, AtlassianUiPlugin.PLUGIN_ID,
						"Could not load team connector: " + object.getClass().getCanonicalName() + " must implement "
								+ ITeamResourceConnector.class.getCanonicalName()));
				return;
			}

			ITeamResourceConnector teamConnector = (ITeamResourceConnector) object;
			teamConnectors.add(teamConnector);
		} catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.WARNING, AtlassianUiPlugin.PLUGIN_ID,
					"Could not load team connector extension", e)); //$NON-NLS-1$
		}
	}

}
