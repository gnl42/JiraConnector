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

package me.glindholm.connector.eclipse.internal.bamboo.ui.views;

import java.util.List;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

import me.glindholm.theplugin.commons.bamboo.TestDetails;

/**
 * Copies the names of the methods that failed and their traces to the clipboard.
 */
public class CopyFailureListAction extends Action {
    private static final String lineDelim = System.lineSeparator();
    private final Clipboard fClipboard;

    private final TestResultsView fRunner;

    public CopyFailureListAction(TestResultsView runner, Clipboard clipboard) {
        super("Copy Failure List");
        fRunner = runner;
        fClipboard = clipboard;
    }

    /*
     * @see IAction#run()
     */
    @Override
    public void run() {
        TextTransfer plainTextTransfer = TextTransfer.getInstance();

        try {
            fClipboard.setContents(new String[] { getAllFailureTraces() }, new Transfer[] { plainTextTransfer });
        } catch (SWTError e) {
            if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) {
                throw e;
            }
            if (MessageDialog.openQuestion(JavaPlugin.getActiveWorkbenchShell(), "Problem Copying Failure List to Clipboard",
                    "There was a problem when accessing the system clipboard. Retry?")) {
                run();
            }
        }
    }

    public String getAllFailureTraces() {
        StringBuilder buf = new StringBuilder();
        List<TestDetails> failures = fRunner.getAllFailures();

        for (TestDetails failure : failures) {
            buf.append(failure.getTestMethodName()).append(lineDelim);
            String failureTrace = failure.getErrors();
            if (failureTrace != null) {
                failureTrace = failureTrace.replaceAll("\\r\\n|\\r|\\n", lineDelim); //$NON-NLS-1$
                buf.append(failureTrace);
            }
        }
        return buf.toString();
    }

}
