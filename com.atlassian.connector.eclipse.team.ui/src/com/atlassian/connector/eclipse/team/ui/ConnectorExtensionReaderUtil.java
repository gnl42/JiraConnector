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
 * @author Wojciech Seliga
 */
public final class ConnectorExtensionReaderUtil {

	private static final String ATTRIBUTE_CLASS = "class";

	private ConnectorExtensionReaderUtil() {
	}

	public static <T> Set<T> getTeamConnectors(String extensionName, String elementName, Class<T> clazz) {
		Set<T> teamConnectors = new HashSet<T>();

		IExtensionRegistry registry = Platform.getExtensionRegistry();

		IExtensionPoint teamConnectorExtensionPoint = registry.getExtensionPoint(extensionName);
		if (teamConnectorExtensionPoint != null) {
			for (IExtension templateExtension : teamConnectorExtensionPoint.getExtensions()) {
				IConfigurationElement[] elements = templateExtension.getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if (element.getName().equals(elementName)) {
						try {
							teamConnectors.add(getTeamConnector(element, clazz));
						} catch (CoreException e) {
							StatusHandler.log(new Status(IStatus.WARNING, AtlassianTeamUiPlugin.PLUGIN_ID,
									"Could not load team connector extension", e)); //$NON-NLS-1$
						}
					}
				}
			}
		}
		return teamConnectors;
	}

	private static <T> T getTeamConnector(IConfigurationElement element, Class<T> clazz) throws CoreException {
//		try {
		Object object = element.createExecutableExtension(ATTRIBUTE_CLASS);
		if (!clazz.isInstance(object)) {
			throw new CoreException(new Status(IStatus.WARNING, AtlassianTeamUiPlugin.PLUGIN_ID,
					"Could not load team connector: " + object.getClass().getCanonicalName() + " must implement "
							+ clazz.getCanonicalName()));
		}
		return clazz.cast(object);

//			ITeamUiResourceConnector teamConnector = (ITeamUiResourceConnector) object;
//			teamConnectors.add(teamConnector);
//		} catch (CoreException e) {
//			StatusHandler.log(new Status(IStatus.WARNING, AtlassianUiPlugin.PLUGIN_ID,
//					"Could not load team connector extension", e)); //$NON-NLS-1$
//		}
	}
}
