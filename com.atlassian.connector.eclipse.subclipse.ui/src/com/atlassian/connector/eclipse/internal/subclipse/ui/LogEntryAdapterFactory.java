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

package com.atlassian.connector.eclipse.internal.subclipse.ui;

import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;
import com.atlassian.connector.eclipse.ui.team.CustomChangeSetLogEntry;
import com.atlassian.connector.eclipse.ui.team.ICustomChangesetLogEntry;
import com.atlassian.connector.eclipse.ui.team.ITeamResourceConnector;
import com.atlassian.connector.eclipse.ui.team.RepositoryInfo;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.tigris.subversion.subclipse.core.history.ILogEntry;
import org.tigris.subversion.subclipse.core.history.LogEntryChangePath;

import java.util.Set;

/**
 * Adapter factory for adapting between a CustomChangesetLogEntry and the ILogEntry of subclipse
 * 
 * @author Thomas Ehrnhoefer
 */
public class LogEntryAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("unchecked")
	private static final Class[] ADAPTERS = { ICustomChangesetLogEntry.class };

	@SuppressWarnings("unchecked")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (!ICustomChangesetLogEntry.class.equals(adapterType)) {
			return null;
		}
		if (adaptableObject instanceof ILogEntry) {
			ILogEntry logEntry = (ILogEntry) adaptableObject;
			LogEntryChangePath[] logEntryChangePaths = logEntry.getLogEntryChangePaths();
			String[] changed = new String[logEntryChangePaths.length];
			for (int i = 0; i < logEntryChangePaths.length; i++) {
				changed[i] = logEntryChangePaths[i].getPath();

			}

			Set<ITeamResourceConnector> connectors = AtlassianUiPlugin.getDefault()
					.getTeamResourceManager()
					.getTeamConnectors();

			// Only because I don't want to publish getRepository as a ITeamResourceConnector member
			for (ITeamResourceConnector connector : connectors) {
				if (connector instanceof SubclipseTeamResourceConnector) {
					SubclipseTeamResourceConnector strc = (SubclipseTeamResourceConnector) connector;
					RepositoryInfo repository = strc.getRepository(logEntry.getResource()
							.getRepository()
							.getUrl()
							.toString(), new NullProgressMonitor());
					return new CustomChangeSetLogEntry(logEntry.getComment(), logEntry.getAuthor(),
							logEntry.getRevision().toString(), logEntry.getDate(), changed, repository);
				}
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public Class[] getAdapterList() {
		return ADAPTERS;
	}

}