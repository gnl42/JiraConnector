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

import java.util.Arrays;
import java.util.HashSet;
import java.util.SortedSet;

/**
 * Page for selecting a crucible repository
 * 
 * @author Thomas Ehrnhoefer
 */
@SuppressWarnings("restriction")
public class SelectCrucibleRepositoryPage extends SelectRepositoryPage {

	private final SortedSet<ICustomChangesetLogEntry> logEntries;

	public static final ITaskRepositoryFilter CRUCIBLE_REPOSITORY_FILTER = new ITaskRepositoryFilter() {
		public boolean accept(TaskRepository repository, AbstractRepositoryConnector connector) {
			if (CrucibleCorePlugin.getRepositoryConnector().getConnectorKind().equals(connector.getConnectorKind())) {
				return true;
			}
			return false;
		}
	};

	public SelectCrucibleRepositoryPage(SortedSet<ICustomChangesetLogEntry> logEntries) {
		super(CRUCIBLE_REPOSITORY_FILTER);
		this.logEntries = logEntries;
	}

	protected IWizard createWizard(TaskRepository taskRepository) {
		ReviewWizard wizard = new ReviewWizard(taskRepository, new HashSet<ReviewWizard.Type>(
				Arrays.asList(ReviewWizard.Type.ADD_CHANGESET)));
		wizard.setLogEntries(logEntries);
		return wizard;
	}
}
