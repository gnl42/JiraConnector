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

package com.atlassian.connector.eclipse.internal.crucible.ui.operations;

import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.DefineRepositoryMappingsPage;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import java.util.Collection;
import java.util.Set;

public class DefineRepositoryMappingsWizard extends Wizard {

	private DefineRepositoryMappingsPage repositoryMappings;

	private final TaskRepository taskRepository;

	private final Set<IResource> resources;

	public DefineRepositoryMappingsWizard(TaskRepository repository, Collection<IResource> resources) {
		super();
		this.taskRepository = repository;
		this.resources = MiscUtil.buildHashSet();
		this.resources.addAll(resources);

		setWindowTitle("Define Repository Mapping");
		setNeedsProgressMonitor(true);
	}

	public void addPages() {
		if (repositoryMappings == null) {
			repositoryMappings = new DefineRepositoryMappingsPage(taskRepository, resources);
		}
		addPage(repositoryMappings);
	}

	@Override
	public boolean performFinish() {
		return true;
	}

}
