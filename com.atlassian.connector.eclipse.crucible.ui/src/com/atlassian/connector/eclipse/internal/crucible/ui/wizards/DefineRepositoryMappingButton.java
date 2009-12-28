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

import com.atlassian.connector.eclipse.fisheye.ui.preferences.FishEyePreferenceContextData;
import com.atlassian.connector.eclipse.fisheye.ui.preferences.SourceRepositoryMappingPreferencePage;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class DefineRepositoryMappingButton {

	private final AbstractCrucibleWizardPage page;

	private final Composite composite;

	private final TaskRepository repository;

	private final Control defineMappingButton;

	private String missingMapping;

	public DefineRepositoryMappingButton(final AbstractCrucibleWizardPage page, Composite composite,
			final TaskRepository repository) {
		this.page = page;
		this.composite = composite;
		this.repository = repository;

		this.defineMappingButton = createDefineRepositoryMappingsButton();

	}

	public Control getControl() {
		return defineMappingButton;
	}

	public void setMissingMapping(String mapping) {
		this.missingMapping = mapping;
	}

	private Control createDefineRepositoryMappingsButton() {

		Button updateData = new Button(composite, SWT.PUSH);
		updateData.setText("Define Repository Mappings");

		updateData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FishEyePreferenceContextData data = page.isPageComplete() ? null : new FishEyePreferenceContextData(
						missingMapping == null ? "" : missingMapping, repository);
				final PreferenceDialog prefDialog = PreferencesUtil.createPreferenceDialogOn(page.getShell(),
						SourceRepositoryMappingPreferencePage.ID, null, data);
				if (prefDialog != null) {
					if (prefDialog.open() == Window.OK) {
						page.validatePage();
					}
				}
			}
		});
		return updateData;
	}
}