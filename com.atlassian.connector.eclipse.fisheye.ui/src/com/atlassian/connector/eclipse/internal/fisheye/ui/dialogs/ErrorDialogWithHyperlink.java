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

package com.atlassian.connector.eclipse.internal.fisheye.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ErrorDialogWithHyperlink extends IconAndMessageDialog {

	private final String linkText;

	private final Runnable linkHandler;

	private final String title;

	public ErrorDialogWithHyperlink(Shell parentShell, @NotNull String title, @NotNull String message,
			@Nullable String linkText, @Nullable Runnable linkHandler) {
		super(parentShell);
		this.title = title;
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.message = message;
		this.linkText = linkText;
		this.linkHandler = linkHandler;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		createMessageArea(parent);
		getShell().setText(title);
		if (linkText != null) {
			final Link link = new Link(parent, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(link);
			link.setText(linkText);
			link.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					close();
					if (linkHandler != null) {
						linkHandler.run();
					}
				}
			});

		}
		return super.createDialogArea(parent);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.HELP_ID, "Help", false);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
		if (buttonId == IDialogConstants.HELP_ID) {
			TasksUiUtil.openUrl("http://confluence.atlassian.com/display/IDEPLUGIN/Atlassian+Eclipse+Connector");
		}
	}

	/**
	 * for easy testing
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Display display = new Display();
		final Shell shell = new Shell(display);
		String msg = "this is a very long text flsdjlsdjflkdsj fljsljslkdj jlkdjslk jlkjlfdsj END"
				+ "this is a very long text flsdjlsdjflkdsj fljsljslkdj jlkdjslk jlkjlfdsj END"
				+ "this is a very long text flsdjlsdjflkdsj fljsljslkdj jlkdjslk jlkjlfdsj END!";
		new ErrorDialogWithHyperlink(shell, "My title", msg, "My <a>awesome</a> link which is quite long", null).open();
	}

	@Override
	protected Image getImage() {
		return getErrorImage();
	}

}
