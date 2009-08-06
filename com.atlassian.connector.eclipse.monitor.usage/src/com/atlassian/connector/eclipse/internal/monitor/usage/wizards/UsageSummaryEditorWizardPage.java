/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Meghan Allen - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.monitor.usage.wizards;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.atlassian.connector.eclipse.internal.monitor.usage.Messages;
import com.atlassian.connector.eclipse.internal.monitor.usage.UiUsageMonitorPlugin;

/**
 * @author Meghan Allen
 */
public class UsageSummaryEditorWizardPage extends WizardPage implements IWizardPage {

	private static final String TITLE = Messages.UsageSummaryEditorWizardPage_title;

	private static final String DESCRIPTION = Messages.UsageSummaryEditorWizardPage_description;

	private Button perspectiveCheckbox = null;

	private Button viewCheckbox = null;

	public UsageSummaryEditorWizardPage() {
		super(TITLE);
		setTitle(TITLE);
		setDescription(DESCRIPTION);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(UiUsageMonitorPlugin.ID_PLUGIN,
				"icons/wizban/banner-usage.gif")); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.FILL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;

		Label label = new Label(container, SWT.LEFT);
		label.setText(Messages.UsageSummaryEditorWizardPage_this_will_run_in_background);

		createCheckboxes(container);

		setControl(container);

	}

	private void createCheckboxes(Composite parent) {
		Group checkboxGroup = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;

		checkboxGroup.setLayout(layout);
		checkboxGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		checkboxGroup.setText(Messages.UsageSummaryEditorWizardPage_show_usage_for);
		checkboxGroup.setFont(parent.getFont());

		perspectiveCheckbox = new Button(checkboxGroup, SWT.CHECK | SWT.LEFT | SWT.NO_FOCUS);
		perspectiveCheckbox.setText(Messages.UsageSummaryEditorWizardPage_use_of_perspectives);
		perspectiveCheckbox.setSelection(true);
		perspectiveCheckbox.addSelectionListener(new CheckboxSelectionListener());

		viewCheckbox = new Button(checkboxGroup, SWT.CHECK | SWT.LEFT | SWT.NO_FOCUS);
		viewCheckbox.setText(Messages.UsageSummaryEditorWizardPage_use_of_views);
		viewCheckbox.setSelection(true);
		viewCheckbox.addSelectionListener(new CheckboxSelectionListener());
	}

	public boolean includePerspective() {
		return perspectiveCheckbox.getSelection();
	}

	public boolean includeViews() {
		return viewCheckbox.getSelection();
	}

	private class CheckboxSelectionListener extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (!perspectiveCheckbox.getSelection() && !viewCheckbox.getSelection()) {
				setPageComplete(false);
			} else {
				setPageComplete(true);
			}
		}

	}

}
