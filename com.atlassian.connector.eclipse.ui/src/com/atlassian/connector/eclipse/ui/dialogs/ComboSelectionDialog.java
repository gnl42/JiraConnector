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

package com.atlassian.connector.eclipse.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Dialog with a ComboViewer instead of a Text
 * 
 * @author Thomas Ehrnhoefer
 */
public class ComboSelectionDialog<T> extends Dialog {

	private T selection;

	private final String title;

	private final String labelText;

	private final Collection<T> inputObjects;

	private final T preselect;

	private final IBaseLabelProvider labelProvider;

	public ComboSelectionDialog(@NotNull Shell parentShell, @NotNull String shellTitle, @NotNull String labelText,
			@NotNull IBaseLabelProvider labelProvider, @NotNull Collection<T> input, @Nullable T preselect) {
		super(parentShell);
		this.title = shellTitle;
		this.labelText = labelText;
		this.inputObjects = input;
		this.preselect = preselect;
		this.labelProvider = labelProvider;
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
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setLabelProvider(labelProvider);
		comboViewer.setSorter(new ViewerSorter());
		comboViewer.setInput(inputObjects);
		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@SuppressWarnings("unchecked")
			public void selectionChanged(SelectionChangedEvent event) {
				if (comboViewer.getSelection() instanceof IStructuredSelection) {
					T selected = (T) ((IStructuredSelection) comboViewer.getSelection()).getFirstElement();
					if (inputObjects.contains(selected)) {
						selection = selected;
					}
				}
				// we preselect comboViewer and button may equal to null
				Button okButton = getButton(IDialogConstants.OK_ID);
				if (okButton != null) {
					okButton.setEnabled(selection != null);
				}
			}
		});
		GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).applyTo(comboViewer.getCombo());

		applyDialogFont(composite);

		if (preselect != null) {
			comboViewer.setSelection(new StructuredSelection(preselect));
		}

		return composite;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		// ignore
		Control control = super.createButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		return control;
	}

	public T getSelection() {
		return selection;
	}

}
