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
import com.atlassian.connector.eclipse.ui.team.ICustomChangesetLogEntry;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryFilter;
import org.eclipse.mylyn.internal.tasks.ui.wizards.SelectRepositoryPage;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import java.util.SortedSet;

/**
 * Page for selecting a crucible repository
 * 
 * @author Thomas Ehrnhoefer
 */
public class SelectCrucibleRepositoryPage extends SelectRepositoryPage {

	private final SortedSet<ICustomChangesetLogEntry> logEntries;

	public SelectCrucibleRepositoryPage(SortedSet<ICustomChangesetLogEntry> logEntries) {
		super(new ITaskRepositoryFilter() {
			public boolean accept(TaskRepository repository, AbstractRepositoryConnector connector) {
				if (CrucibleCorePlugin.getRepositoryConnector().getConnectorKind().equals(connector.getConnectorKind())) {
					return true;
				}
				return false;
			}
		});
		this.logEntries = logEntries;
	}

	protected IWizard createWizard(TaskRepository taskRepository) {
		return new CrucibleReviewWizard(taskRepository, logEntries);
	}
}
