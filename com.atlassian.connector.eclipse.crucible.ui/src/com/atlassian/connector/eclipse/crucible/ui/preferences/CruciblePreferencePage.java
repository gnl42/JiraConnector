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

package com.atlassian.connector.eclipse.crucible.ui.preferences;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiConstants;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class CruciblePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public CruciblePreferencePage() {
		super(GRID);
		setDescription("Atlassian Crucible Settings");
		setPreferenceStore(CrucibleUiPlugin.getDefault().getPreferenceStore());
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
		final RadioGroupFieldEditor reviewActivation = new RadioGroupFieldEditor(
				CrucibleUiConstants.PREFERENCE_ACTIVATE_REVIEW,
				"Activate Review when opening a file from within the Review Editor", 3, new String[][] {
						{ ActivateReview.ALWAYS.getLabel(), ActivateReview.ALWAYS.getKey() },
						{ ActivateReview.NEVER.getLabel(), ActivateReview.NEVER.getKey() },
						{ ActivateReview.PROMPT.getLabel(), ActivateReview.PROMPT.getKey() } }, getFieldEditorParent(),
				true);
		addField(reviewActivation);
	}

}
