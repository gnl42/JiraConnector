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

package com.atlassian.connector.eclipse.internal.crucible.ui.wizards;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;

import org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryFilter;
import org.eclipse.mylyn.internal.tasks.ui.wizards.SelectRepositoryPage;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * Page for selecting a crucible repository
 * 
 * @author Thomas Ehrnhoefer
 */
@SuppressWarnings("restriction")
public abstract class SelectCrucibleRepositoryPage extends SelectRepositoryPage {

	public static final ITaskRepositoryFilter ENABLED_CRUCIBLE_REPOSITORY_FILTER = new ITaskRepositoryFilter() {
		public boolean accept(TaskRepository repository, AbstractRepositoryConnector connector) {
			if (CrucibleCorePlugin.getRepositoryConnector().getConnectorKind().equals(connector.getConnectorKind())
					&& !repository.isOffline()) {
				return true;
			}
			return false;
		}
	};

	public static final ITaskRepositoryFilter CRUCIBLE_REPOSITORY_FILTER = new ITaskRepositoryFilter() {
		public boolean accept(TaskRepository repository, AbstractRepositoryConnector connector) {
			if (CrucibleCorePlugin.getRepositoryConnector().getConnectorKind().equals(connector.getConnectorKind())) {
				return true;
			}
			return false;
		}
	};

	public SelectCrucibleRepositoryPage(ITaskRepositoryFilter filter) {
		super(filter);
	}
}
