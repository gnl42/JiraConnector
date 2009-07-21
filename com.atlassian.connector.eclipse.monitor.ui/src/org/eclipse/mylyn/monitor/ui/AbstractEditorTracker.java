/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.monitor.ui;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Tracks interaction with workbench editors.
 * 
 * @author Mik Kersten
 * @since 2.0
 */
public abstract class AbstractEditorTracker extends AbstractPartTracker {

	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			editorClosed((IEditorPart) part);
		}
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			editorOpened((IEditorPart) part);
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			editorBroughtToTop((IEditorPart) part);
		}
	}

	protected abstract void editorOpened(IEditorPart part);

	protected abstract void editorClosed(IEditorPart part);

	protected abstract void editorBroughtToTop(IEditorPart part);

	@Override
	public void partActivated(IWorkbenchPart part) {
		// ignore
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

}
