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

package com.atlassian.connector.eclipse.fisheye.ui.preferences;

import com.atlassian.connector.eclipse.internal.core.AtlassianCorePlugin;
import com.atlassian.connector.eclipse.internal.fisheye.ui.FishEyeUiPlugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import java.io.IOException;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By subclassing
 * <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main
 * plug-in class. That way, preferences can be accessed directly via the preference store.
 */

public class FishEyePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "com.atlassian.connector.eclipse.fisheye.ui.preferences.FishEyePreferencePage";

	private TableViewer tableViewer;

	private SourceRepostioryMappingEditor mappingEditor;

	public FishEyePreferencePage() {
		super("FishEye Preferences");
		setDescription("Add, remove or edit FishEye mapping configuration.\n"
				+ "Mapping your local SCM repositories to a Fisheye Server Repository "
				+ "is necessary for looking up files in Fisheye correctly.");
		noDefaultAndApplyButton();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	@Override
	public void applyData(Object data) {
		if (data instanceof FishEyePreferenceContextData) {
			final FishEyePreferenceContextData initialData = (FishEyePreferenceContextData) data;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					AddOrEditFishEyeMappingDialog dialog = new AddOrEditFishEyeMappingDialog(getShell(), null,
							initialData.getScmPath(), null);
					if (dialog.open() == Window.OK) {
						final FishEyeMappingConfiguration cfg = new FishEyeMappingConfiguration(
								dialog.getTaskRepository(), dialog.getScmPath(), dialog.getSourceRepository());
						if (cfg != null) {

							mappingEditor.addMapping(cfg);
							tableViewer.refresh();
						}
					}
				}
			});
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
	}

	@Override
	protected Control createContents(Composite ancestor) {
		initializeDialogUnits(ancestor);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(ancestor);

		Composite parent = new Composite(ancestor, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(parent);

		mappingEditor = new SourceRepostioryMappingEditor(parent);
		mappingEditor.setMapping(FishEyeUiPlugin.getDefault().getFishEyeSettingsManager().getMappings());

		return ancestor;
	}

	@Override
	public boolean performOk() {
		try {
			FishEyeUiPlugin.getDefault().getFishEyeSettingsManager().setMappings(mappingEditor.getMapping());
			FishEyeUiPlugin.getDefault().getFishEyeSettingsManager().save();
		} catch (IOException e) {
			ErrorDialog.openError(getShell(), AtlassianCorePlugin.PRODUCT_NAME,
					"Error while saving FishEye mapping configuration", new Status(IStatus.ERROR,
							FishEyeUiPlugin.PLUGIN_ID, e.getMessage(), e));
			return false;
		}
		return super.performOk();
	}
}