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

package com.atlassian.connector.eclipse.internal.crucible.ui.util;

import com.atlassian.connector.eclipse.internal.crucible.ui.CruciblePreCommitFileInput;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.CrucibleFileInfoCompareEditorInput;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.util.StringUtil;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public final class EditorUtil {
	private EditorUtil() {
		// do not instantiate
	}

	/**
	 * Tests if a CU is currently shown in an editor
	 * 
	 * @param inputElement
	 *            the input element
	 * @return the IEditorPart if shown, null if element is not open in an editor
	 */
	public static IEditorPart isOpenInEditor(Object inputElement) {
		IWorkbenchPage page = getActivePage();
		if (page != null) {
			IEditorReference[] editors = page.getEditorReferences();
			if (editors != null) {
				for (IEditorReference ref : editors) {
					try {
						IEditorInput input = ref.getEditorInput();
						if (input instanceof CruciblePreCommitFileInput) {
							if (inputElement.equals(((CruciblePreCommitFileInput) input).getCrucibleFile()
									.getCrucibleFileInfo())) {
								return ref.getEditor(true);
							}
						}
						if (input instanceof CrucibleFileInfoCompareEditorInput) {
							if (((CrucibleFileInfoCompareEditorInput) input).getCrucibleFileInfo().equals(inputElement)) {
								return ref.getEditor(true);
							}
						}
						if (inputElement instanceof CrucibleFileInfo) {
							final CrucibleFileInfo fileInfo = (CrucibleFileInfo) inputElement;
							if (input instanceof FileEditorInput) {
								String location = StringUtil.removeLeadingAndTrailingSlashes(((FileEditorInput) input).getFile()
										.getFullPath()
										.toString());
								if (location.equals(StringUtil.removeLeadingAndTrailingSlashes(fileInfo.getOldFileDescriptor()
										.getUrl()))
										|| location.equals(StringUtil.removeLeadingAndTrailingSlashes(fileInfo.getFileDescriptor()
												.getUrl()))) {
									return ref.getEditor(true);
								}
							}
						}
					} catch (PartInitException e) {
						// ignore
					}
				}
			}
		}

		return null;
	}

	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
		return window.getActivePage();
	}

}