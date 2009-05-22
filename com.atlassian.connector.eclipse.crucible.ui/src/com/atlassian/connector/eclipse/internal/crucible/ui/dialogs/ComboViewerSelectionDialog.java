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

package com.atlassian.connector.eclipse.internal.crucible.ui.dialogs;

import com.atlassian.connector.eclipse.internal.crucible.core.client.model.CrucibleCachedRepository;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleRepositoriesContentProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleRepositoriesLabelProvider;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import java.util.Set;

/**
 * Dialog with a ComboViewer instead of a Text
 * 
 * @author Thomas Ehrnhoefer
 */
public class ComboViewerSelectionDialog extends Dialog {

	private CrucibleCachedRepository selection;

	private final String title;

	private final String labelText;

	private final Set<CrucibleCachedRepository> inputObjects;

	public ComboViewerSelectionDialog(Shell parentShell, String shellTitle, String labelText,
			Set<CrucibleCachedRepository> input) {
		super(parentShell);
		this.title = shellTitle;
		this.labelText = labelText;
		this.inputObjects = input;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(title);

		Composite composite = new Composite((Composite) super.createDialogArea(parent), SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		new Label(composite, SWT.NONE).setText(labelText);

		final ComboViewer comboViewer = new ComboViewer(composite);
		comboViewer.getCombo().setText("Select");
		comboViewer.setContentProvider(new CrucibleRepositoriesContentProvider());
		comboViewer.setLabelProvider(new CrucibleRepositoriesLabelProvider());
		comboViewer.setSorter(new ViewerSorter());
		comboViewer.setInput(inputObjects);
		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (comboViewer.getSelection() instanceof IStructuredSelection) {
					Object selected = ((IStructuredSelection) comboViewer.getSelection()).getFirstElement();
					if (inputObjects.contains(selected)) {
						selection = (CrucibleCachedRepository) selected;
					}
				}
				getButton(IDialogConstants.OK_ID).setEnabled(selection != null);
			}
		});
		GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).applyTo(comboViewer.getCombo());

		applyDialogFont(composite);
		return composite;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		// ignore
		Control control = super.createButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		return control;
	}

	public CrucibleCachedRepository getSelection() {
		return selection;
	}

}
