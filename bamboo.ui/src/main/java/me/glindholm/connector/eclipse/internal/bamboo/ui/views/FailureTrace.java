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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.IOpenEventListener;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;

import me.glindholm.connector.eclipse.internal.bamboo.ui.model.TestElement;

/**
 * A pane that shows a stack trace of a failed test.
 */
public class FailureTrace implements IMenuListener {
	private static final int MAX_LABEL_LENGTH = 256;

	static final String FRAME_PREFIX = "at "; //$NON-NLS-1$

	private final Table fTable;

	private final TestResultsView fTestRunner;

	private String fInputTrace;

	private final Clipboard fClipboard;

	private TestElement fFailure;

	private final FailureTableDisplay fFailureTableDisplay;

	public FailureTrace(Composite parent, Clipboard clipboard, TestResultsView testRunner, ToolBar toolBar) {
		Assert.isNotNull(clipboard);

		fTable = new Table(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		fTestRunner = testRunner;
		fClipboard = clipboard;

		OpenStrategy handler = new OpenStrategy(fTable);
		handler.addOpenListener(new IOpenEventListener() {
			public void handleOpen(SelectionEvent e) {
				if (fTable.getSelection().length != 0) {
					Action a = createOpenEditorAction(getSelectedText());
					if (a != null) {
						a.run();
					}
				}
			}
		});

		initMenu();

		fFailureTableDisplay = new FailureTableDisplay(fTable);
	}

	private void initMenu() {
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(this);
		Menu menu = menuMgr.createContextMenu(fTable);
		fTable.setMenu(menu);
	}

	public void menuAboutToShow(IMenuManager manager) {
		if (fTable.getSelectionCount() > 0) {
			Action a = createOpenEditorAction(getSelectedText());
			if (a != null) {
				manager.add(a);
			}
			manager.add(new JUnitCopyAction(FailureTrace.this, fClipboard));
		}
	}

	public String getTrace() {
		return fInputTrace;
	}

	private String getSelectedText() {
		return fTable.getSelection()[0].getText();
	}

	private Action createOpenEditorAction(String traceLine) {
		try {
			String testName = traceLine;
			testName = testName.substring(testName.indexOf(FRAME_PREFIX));
			testName = testName.substring(FRAME_PREFIX.length(), testName.lastIndexOf('(')).trim();
			testName = testName.substring(0, testName.lastIndexOf('.'));
			int innerSeparatorIndex = testName.indexOf('$');
			if (innerSeparatorIndex != -1) {
				testName = testName.substring(0, innerSeparatorIndex);
			}

			String lineNumber = traceLine;
			lineNumber = lineNumber.substring(lineNumber.indexOf(':') + 1, lineNumber.lastIndexOf(')'));
			int line = Integer.valueOf(lineNumber).intValue();
			return new OpenEditorAtLineAction(fTestRunner, testName, line);
		} catch (NumberFormatException e) {
		} catch (IndexOutOfBoundsException e) {
		}
		return null;
	}

	/**
	 * Returns the composite used to present the trace
	 * 
	 * @return The composite
	 */
	Composite getComposite() {
		return fTable;
	}

	/**
	 * Refresh the table from the trace.
	 */
	public void refresh() {
		updateTable(fInputTrace);
	}

	/**
	 * Shows a TestFailure
	 * 
	 * @param test
	 *            the failed test
	 */
	public void showFailure(TestElement test) {
		fFailure = test;
		String trace = ""; //$NON-NLS-1$
		if (test != null) {
			trace = test.getTrace();
		}
		if (fInputTrace == trace) {
			return;
		}
		fInputTrace = trace;
		updateTable(trace);
	}

	private void updateTable(String trace) {
		if (trace == null || trace.trim().equals("")) { //$NON-NLS-1$
			clear();
			return;
		}
		trace = trace.trim();
		fTable.setRedraw(false);
		fTable.removeAll();
		new TextualTrace(trace, getFilterPatterns()).display(fFailureTableDisplay, MAX_LABEL_LENGTH);
		fTable.setRedraw(true);
	}

	private String[] getFilterPatterns() {
		return new String[0];
	}

	/**
	 * Shows other information than a stack trace.
	 * 
	 * @param text
	 *            the informational message to be shown
	 */
	public void setInformation(String text) {
		clear();
		TableItem tableItem = fFailureTableDisplay.newTableItem();
		tableItem.setText(text);
	}

	/**
	 * Clears the non-stack trace info
	 */
	public void clear() {
		fTable.removeAll();
		fInputTrace = null;
	}

	public TestElement getFailedTest() {
		return fFailure;
	}

	public Shell getShell() {
		return fTable.getShell();
	}

	public FailureTableDisplay getFailureTableDisplay() {
		return fFailureTableDisplay;
	}
}
