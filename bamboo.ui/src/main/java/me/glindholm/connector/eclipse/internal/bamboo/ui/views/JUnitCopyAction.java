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

package me.glindholm.connector.eclipse.internal.bamboo.ui.views;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.actions.SelectionListenerAction;

import me.glindholm.connector.eclipse.internal.bamboo.ui.model.TestElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Copies a test failure stack trace to the clipboard.
 */
public class JUnitCopyAction extends SelectionListenerAction {
	private final FailureTrace fView;

	private final Clipboard fClipboard;

	private TestElement fTestElement;

	public JUnitCopyAction(FailureTrace view, Clipboard clipboard) {
		super("Copy Trace");
		Assert.isNotNull(clipboard);
		fView = view;
		fClipboard = clipboard;
	}

	/*
	 * @see IAction#run()
	 */
	public void run() {
		String trace = fView.getTrace();
		String source = null;
		if (trace != null) {
			source = convertLineTerminators(trace);
		} else if (fTestElement != null) {
			source = fTestElement.getTestName();
		}
		if (source == null || source.length() == 0) {
			return;
		}

		TextTransfer plainTextTransfer = TextTransfer.getInstance();
		try {
			fClipboard.setContents(new String[] { convertLineTerminators(source) },
					new Transfer[] { plainTextTransfer });
		} catch (SWTError e) {
			if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) {
				throw e;
			}
			if (MessageDialog.openQuestion(fView.getComposite().getShell(), "Problem Copying to Clipboard",
					"There was a problem when accessing the system clipboard. Retry?")) {
				run();
			}
		}
	}

	public void handleTestSelected(TestElement test) {
		fTestElement = test;
	}

	private String convertLineTerminators(String in) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		StringReader stringReader = new StringReader(in);
		BufferedReader bufferedReader = new BufferedReader(stringReader);
		String line;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				printWriter.println(line);
			}
		} catch (IOException e) {
			return in; // return the trace unfiltered
		}
		return stringWriter.toString();
	}
}
