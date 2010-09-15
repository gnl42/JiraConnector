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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleRepositoriesLabelProvider;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.Set;

/**
 * Page for adding a patch (from the clipboard) to a review
 * 
 * @author Thomas Ehrnhoefer
 */
public class CrucibleAddPatchPage extends WizardPage {

	private Text patchText;

	private final TaskRepository taskRepository;

	private String selectedRepository;

	private Set<Repository> cachedRepositories;

	private ComboViewer comboViewer;

	private boolean includePatch = false;

	protected boolean patchPasted = false;

	public CrucibleAddPatchPage(TaskRepository repository) {
		super("cruciblePatch"); //$NON-NLS-1$
		setTitle("Add Patch to Review");
		setDescription("Attach a patch from the clipboard to the review.");
		this.taskRepository = repository;
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).margins(5, 5).create());

		final Button includePatchButton = new Button(composite, SWT.CHECK);
		includePatchButton.setText("Include this Patch from the clipboard in the review:");
		GridDataFactory.fillDefaults().span(2, 1).applyTo(includePatchButton);
		includePatchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				includePatch = includePatchButton.getSelection();
				if (includePatch) {
					if (!CrucibleUiUtil.hasCachedData(taskRepository)) {
						CrucibleUiUtil.updateTaskRepositoryCache(taskRepository, getContainer(),
								CrucibleAddPatchPage.this);
					}
					//copy content from clipboard, only if not already done
					Clipboard clipboard = new Clipboard(Display.getDefault());
					Object patch = clipboard.getContents(TextTransfer.getInstance());
					if (patch != null && patch instanceof String && !patchPasted) {
						patchText.setText((String) patch);
						patchPasted = true;
					}
					if (cachedRepositories == null) {
						cachedRepositories = CrucibleUiUtil.getCachedRepositories(taskRepository);
					}
					comboViewer.setInput(cachedRepositories);
				}

				validatePage();
			}
		});

		patchText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		GridDataFactory.fillDefaults().span(2, 1).hint(SWT.DEFAULT, 220).grab(true, true).applyTo(patchText);
		patchText.setEditable(false);

		Label label = new Label(composite, SWT.NONE);
		label.setText("Select the repository on Crucible:");
		GridDataFactory.fillDefaults().grab(false, false).applyTo(label);
		comboViewer = new ComboViewer(composite);
		comboViewer.getCombo().setText("Select Repository");
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setLabelProvider(new CrucibleRepositoriesLabelProvider());
		comboViewer.setSorter(new ViewerSorter());
		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (comboViewer.getSelection() instanceof IStructuredSelection) {
					Object selected = ((IStructuredSelection) comboViewer.getSelection()).getFirstElement();
					if (cachedRepositories.contains(selected)) {
						selectedRepository = ((Repository) selected).getName();
					}
				}
				validatePage();
			}
		});
		GridDataFactory.fillDefaults()
				.align(SWT.BEGINNING, SWT.BEGINNING)
				.grab(true, false)
				.hint(100, SWT.DEFAULT)
				.applyTo(comboViewer.getCombo());

		Button updateData = new Button(composite, SWT.PUSH);
		updateData.setText("Update Repository Data");
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(updateData);
		updateData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CrucibleUiUtil.updateTaskRepositoryCache(taskRepository, getContainer(), CrucibleAddPatchPage.this);
			}
		});

		Dialog.applyDialogFont(composite);
		setControl(composite);
	}

	public boolean hasPatch() {
		return includePatch && patchText.getText().length() > 0 && selectedRepository != null;
	}

	public String getPatch() {
		return patchText.getText();
	}

	/*
	 * checks if page is complete updates the buttons
	 */
	private void validatePage() {
		setErrorMessage(null);

		boolean allFine = true;
		String errorMessage = null;
		if (patchText.getText().length() < 1) {
			errorMessage = "In order to create a review from a patch,"
					+ " copy the patch to the clipboard before opening this Wizard.";
			allFine = false;
		} else if (selectedRepository == null) {
			errorMessage = "Choose a repository on Crucible this patch relates to.";
			allFine = false;
		}
		if (includePatch) {
			setPageComplete(allFine);
			if (errorMessage != null) {
				setErrorMessage(errorMessage);
			}
		} else {
			setPageComplete(true);
		}

		getContainer().updateButtons();
	}

	public String getPatchRepository() {
		return selectedRepository;
	}

}
