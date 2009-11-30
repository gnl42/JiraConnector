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

import com.atlassian.connector.eclipse.ui.team.RepositoryInfo;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import java.util.Collection;
import java.util.Collections;

public class CrucibleRepositoryMappingPageImpl extends CrucibleRepositoryMappingPage {

	private final RepositoryInfo scmRepositoryInfo;

	protected CrucibleRepositoryMappingPageImpl(TaskRepository repository, ReviewWizard wizard,
			RepositoryInfo scmRepositoryInfo) {
		super("crucibleRepoMapping", repository, wizard, 700);
		this.scmRepositoryInfo = scmRepositoryInfo;

		setTitle("Define Repository Mapping");
		setDescription("Define repository mapping used to create review.");
	}

	@Override
	protected Collection<RepositoryInfo> getMappingViewerInput() {
		return Collections.singleton(scmRepositoryInfo);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		validatePage();
	}

	@Override
	protected void validatePage() {
		// just set page incomplete (do not shot error) 
		if (scmRepositoryInfo != null && getRepositoryMappings().get(scmRepositoryInfo.getScmPath()) != null) {
			setPageComplete(true);
		} else {
			setPageComplete(false);
		}
	}

	public void createControl(Composite parent) {
		// TODO maybe the control should be smaller
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).margins(5, 5).create());

		Composite repositoryMappingViewer = createRepositoryMappingComposite(composite);
		GridDataFactory.fillDefaults().span(3, 1).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(
				repositoryMappingViewer);
		repositoryMappingViewer.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		getRepositoriesMappingViewer().setInput(Collections.singleton(scmRepositoryInfo));

		Control button = createUpdateRepositoryDataButton(composite);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(button);

		Dialog.applyDialogFont(composite);
		setControl(composite);
	}
}
