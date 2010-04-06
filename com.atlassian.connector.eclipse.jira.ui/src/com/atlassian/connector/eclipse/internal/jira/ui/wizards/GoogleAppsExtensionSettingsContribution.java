/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.ui.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class GoogleAppsExtensionSettingsContribution extends AbstractTaskRepositoryPageContribution {

	public GoogleAppsExtensionSettingsContribution() {
		super(Messages.GoogleAppsExtensionSettingsContribution_title, Messages.GoogleAppsExtensionSettingsContribution_description);
	}

	@Override
	public Control createControl(Composite parentControl) {
		Composite parent = new Composite(parentControl, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 0;
		parent.setLayout(layout);

		Label label = new Label(parent, SWT.WRAP);
		GridDataFactory.fillDefaults().grab(true, true).hint(500, SWT.DEFAULT).applyTo(label);
		label.setText(Messages.GoogleAppsExtensionSettingsContribution_help_message);

		return parent;
	}

	@Override
	public boolean isPageComplete() {
		return true;
	}

	@Override
	public boolean canFlipToNextPage() {
		return true;
	}

	@Override
	public IStatus validate() {
		return null;
	}

	@Override
	public void applyTo(TaskRepository repository) {
	}

}
