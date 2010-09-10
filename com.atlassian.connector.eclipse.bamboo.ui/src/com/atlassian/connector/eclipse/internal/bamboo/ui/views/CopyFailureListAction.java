/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
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

package com.atlassian.connector.eclipse.internal.bamboo.ui.views;

import com.atlassian.theplugin.commons.bamboo.TestDetails;

import org.eclipse.jdt.internal.junit.ui.JUnitMessages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

import java.util.List;

/**
 * Copies the names of the methods that failed and their traces to the clipboard.
 */
public class CopyFailureListAction extends Action {

	private final Clipboard fClipboard;

	private final TestResultsView fRunner;

	public CopyFailureListAction(TestResultsView runner, Clipboard clipboard) {
		super(JUnitMessages.CopyFailureList_action_label);
		fRunner = runner;
		fClipboard = clipboard;
	}

	/*
	 * @see IAction#run()
	 */
	public void run() {
		TextTransfer plainTextTransfer = TextTransfer.getInstance();

		try {
			fClipboard.setContents(new String[] { getAllFailureTraces() }, new Transfer[] { plainTextTransfer });
		} catch (SWTError e) {
			if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) {
				throw e;
			}
			if (MessageDialog.openQuestion(JavaPlugin.getActiveWorkbenchShell(), JUnitMessages.CopyFailureList_problem,
					JUnitMessages.CopyFailureList_clipboard_busy)) {
				run();
			}
		}
	}

	public String getAllFailureTraces() {
		StringBuffer buf = new StringBuffer();
		List<TestDetails> failures = fRunner.getAllFailures();

		String lineDelim = System.getProperty("line.separator", "\n"); //$NON-NLS-1$//$NON-NLS-2$
		for (TestDetails failure : failures) {
			buf.append(failure.getTestMethodName()).append(lineDelim);
			String failureTrace = failure.getErrors();
			if (failureTrace != null) {
				int start = 0;
				while (start < failureTrace.length()) {
					int idx = failureTrace.indexOf('\n', start);
					if (idx != -1) {
						String line = failureTrace.substring(start, idx);
						buf.append(line).append(lineDelim);
						start = idx + 1;
					} else {
						start = Integer.MAX_VALUE;
					}
				}
			}
		}
		return buf.toString();
	}

}
