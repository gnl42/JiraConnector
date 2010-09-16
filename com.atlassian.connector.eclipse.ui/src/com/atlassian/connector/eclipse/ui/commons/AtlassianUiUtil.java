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

package com.atlassian.connector.eclipse.ui.commons;

import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Provides utility methods for the Atlassian Connector for Eclipse
 * 
 * @author Thomas Ehrnhoefer
 */
public final class AtlassianUiUtil {

	private AtlassianUiUtil() {
	}

	/**
	 * Must be invoked in UI thread
	 * 
	 * @param viewId
	 * @return <code>true</code> when the view has been successfully made visible or it already was, <code>false</code>
	 *         if operation failed
	 */
	public static boolean ensureViewIsVisible(String viewId) {
		final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null) {
			return false;
		}
		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		if (activePage == null) {
			return false;
		}
		for (IViewReference view : activePage.getViewReferences()) {
			if (view.getId().equals(viewId)) {
				return true;
			}
		}
		try {
			activePage.showView(viewId, null, IWorkbenchPage.VIEW_ACTIVATE);
			return true;
		} catch (PartInitException e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, "Could not initialize " + viewId
					+ " view."));
			return false;
		}
	}

	public static boolean showViewInActiveWorkbenchPage(String viewId) {
		final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null) {
			return false;
		}
		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		if (activePage == null) {
			return false;
		}
		try {
			activePage.showView(viewId);
			return true;
		} catch (PartInitException e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, "Could not initialize " + viewId
					+ " view."));
			return false;
		}
	}

	public static LineRange getSelectedLineNumberRangeFromEditorInput(IEditorPart editor, IEditorInput editorInput) {

		if (editor instanceof ITextEditor && editor.getEditorInput() == editorInput) {
			ISelection selection = ((ITextEditor) editor).getSelectionProvider().getSelection();
			return getLineRange(selection);
		} else if (editor.getAdapter(ITextEditor.class) != null) {
			ISelection selection = ((ITextEditor) editor.getAdapter(ITextEditor.class)).getSelectionProvider()
					.getSelection();
			return getLineRange(selection);
		}
		return null;
	}

	private static LineRange getLineRange(ISelection selection) {
		if (selection instanceof TextSelection) {
			TextSelection textSelection = ((TextSelection) selection);
			return new LineRange(textSelection.getStartLine() + 1, textSelection.getEndLine()
					- textSelection.getStartLine() + 1);
		}
		return null;
	}

}
