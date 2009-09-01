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

import com.atlassian.connector.eclipse.ui.team.ICustomChangesetLogEntry;

import org.eclipse.mylyn.tasks.core.TaskRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.SortedSet;

public class ReviewFromChangesetsWizard extends ReviewWizard {

	public ReviewFromChangesetsWizard(TaskRepository taskRepository,
			SortedSet<ICustomChangesetLogEntry> selectedLogEntries) {
		super(taskRepository, new HashSet<Type>(Arrays.asList(Type.ADD_CHANGESET)));
		setLogEntries(selectedLogEntries);
	}

}
