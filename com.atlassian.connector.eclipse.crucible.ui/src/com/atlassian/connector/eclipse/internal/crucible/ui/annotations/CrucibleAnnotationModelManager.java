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
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.CrucibleFileInfoCompareEditorInput;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Class to manage the annotation model for the open editors
 * 
 * @author Shawn Minto
 */
public final class CrucibleAnnotationModelManager {

	private CrucibleAnnotationModelManager() {
		// ignore
	}

	public static void updateAllOpenEditors(Review activeReview) {
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				for (IEditorReference editorReference : page.getEditorReferences()) {
					IWorkbenchPart editorPart = editorReference.getPart(false);
					if (editorPart instanceof CompareEditor) {
						update((CompareEditor) editorPart, activeReview);
					}
				}
			}
		}
	}

	private static void update(CompareEditor editor, Review activeReview) {
		IEditorInput editorInput = editor.getEditorInput();
		if (editorInput instanceof CrucibleFileInfoCompareEditorInput) {
			if (!CrucibleUiPlugin.getDefault().getActiveReviewManager().isReviewActive() || activeReview == null
					|| CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview() != activeReview) {
				return;
			}
			((CrucibleFileInfoCompareEditorInput) editorInput).getAnnotationModelToAttach().updateCrucibleFile(
					activeReview);
		}
	}

}
