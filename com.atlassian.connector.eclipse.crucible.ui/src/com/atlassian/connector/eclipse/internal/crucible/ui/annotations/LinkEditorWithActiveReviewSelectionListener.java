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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;

import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Class that performs a link with editor style action for the active review
 */
public final class LinkEditorWithActiveReviewSelectionListener implements ISelectionListener {

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (CrucibleUiPlugin.getDefault().getActiveReviewManager().isReviewActive()) {
			if (selection instanceof TextSelection && part instanceof ITextEditor) {
				TextSelection textSelection = (TextSelection) selection;
				int offset = textSelection.getOffset();

				CrucibleAnnotationModel annotationModel = CrucibleAnnotationModelManager.getModelForEditor((ITextEditor) part);
				CrucibleUiUtil.highlightAnnotationInRichEditor(offset, annotationModel);
			}
		}
	}
}