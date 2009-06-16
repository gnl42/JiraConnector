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

package com.atlassian.connector.eclipse.internal.crucible.ui.annotations;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Class that tracks the opening and closing of editors for use in the annotation model
 * 
 * @author Shawn Minto
 */
public class CrucibleEditorTracker implements IPartListener {

	public CrucibleEditorTracker() {
	}

	/**
	 * Must be called in UI thread
	 */
	public void init() {
		CrucibleAnnotationModelManager.attachAllOpenEditors();
	}

	public void dispose() {
		CrucibleAnnotationModelManager.dettachAllOpenEditors();
	}

	public void partActivated(IWorkbenchPart part) {
	}

	public void partBroughtToTop(IWorkbenchPart part) {
	}

	public void partClosed(IWorkbenchPart part) {
		if (part instanceof ITextEditor) {
			ITextEditor editor = (ITextEditor) part;
			CrucibleAnnotationModelManager.detach(editor);
		}
	}

	public void partDeactivated(IWorkbenchPart part) {
		// ignore

	}

	public void partOpened(IWorkbenchPart part) {
		annotateEditor(part);
	}

	private void annotateEditor(IWorkbenchPart part) {
		if (part instanceof ITextEditor) {
			ITextEditor editor = (ITextEditor) part;
			CrucibleAnnotationModelManager.attach(editor);
		}
	}

}
