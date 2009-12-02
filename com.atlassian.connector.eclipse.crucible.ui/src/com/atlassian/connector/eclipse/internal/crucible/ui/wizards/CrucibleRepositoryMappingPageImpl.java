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

import com.atlassian.connector.eclipse.ui.team.ScmRepository;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class CrucibleRepositoryMappingPageImpl extends CrucibleRepositoryMappingPage {

	private final Set<ScmRepository> scmRepositories;

	public CrucibleRepositoryMappingPageImpl(TaskRepository repository, Collection<ScmRepository> scmRepositoryInfo) {
		super("crucibleRepoMapping", repository);

		this.scmRepositories = MiscUtil.buildHashSet();
		this.scmRepositories.addAll(scmRepositoryInfo);

		setTitle("Define Repository Mapping");
		setDescription("Define repository mapping used to create review.");
	}

	@Override
	protected Collection<ScmRepository> getMappingViewerInput() {
		return Collections.unmodifiableCollection(this.scmRepositories);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		validatePage();
	}

	@Override
	protected void validatePage() {
		setErrorMessage(null);

		//check if all custom repositories are mapped to crucible repositories
		boolean allFine = true;
		for (ScmRepository ri : getScmRepositories()) {
			if (getRepositoryMappings().get(ri.getScmPath()) == null) {
				setErrorMessage("One or more local repositories are not mapped to Crucible repositories.");
				allFine = false;
				break;
			}
		}
		setPageComplete(allFine);
		getContainer().updateButtons();
	}

	private Collection<ScmRepository> getScmRepositories() {
		return scmRepositories;
	}

	public void createControl(Composite parent) {
		// TODO maybe the control should be smaller
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).margins(5, 5).create());

		Composite repositoryMappingViewer = createRepositoryMappingComposite(composite, 700);
		GridDataFactory.fillDefaults().span(3, 1).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(
				repositoryMappingViewer);
		repositoryMappingViewer.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		getRepositoriesMappingViewer().setInput(Collections.unmodifiableSet(scmRepositories));

		Control button = createUpdateRepositoryDataButton(composite);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(button);

		Dialog.applyDialogFont(composite);
		setControl(composite);
	}
}
