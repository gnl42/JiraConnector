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

import com.atlassian.connector.eclipse.fisheye.ui.FishEyeUiUtil;
import com.atlassian.connector.eclipse.internal.core.CoreConstants;
import com.atlassian.connector.eclipse.internal.fisheye.ui.FishEyeUiPlugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
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

public class SourceRepositoryMappingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "com.atlassian.connector.eclipse.fisheye.ui.preferences.FishEyePreferencePage";

	private SourceRepostioryMappingEditor mappingEditor;

	public SourceRepositoryMappingPreferencePage() {
		super("FishEye Preferences");
		setDescription("Map your local repositories to FishEye/Crucible repositories.");
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
					AddOrEditFishEyeMappingDialog dialog = new AddOrEditFishEyeMappingDialog(getShell(),
							initialData.getTaskRepository(), initialData.getScmPath(), null);
					if (dialog.open() == Window.OK) {
						final FishEyeMappingConfiguration cfg = new FishEyeMappingConfiguration(
								dialog.getTaskRepository(), dialog.getScmPath(), dialog.getSourceRepository());
						if (cfg != null) {
							mappingEditor.addOrEditMapping(cfg, null);
						}
					}
				}
			});
		}
	}

	@Override
	protected Control createContents(Composite ancestor) {
		initializeDialogUnits(ancestor);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(ancestor);

		Composite parent = new Composite(ancestor, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true)/*.hint(500, SWT.DEFAULT)*/.applyTo(parent);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(parent);
		Link instructionsLabel = new Link(parent, SWT.LEFT | SWT.WRAP);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).hint(600, SWT.DEFAULT).applyTo(instructionsLabel);

		instructionsLabel.setText("The connector needs a mapping from the local SCM path used by your project(s), "
				+ "to a FishEye/Crucible server and repository. This allows you to create code reviews from files "
				+ "in your workspace, conduct code reviews in your IDE and link workspace files to FishEye. "
				+ "See the <a>instructions</a> and <a>more details</a>.");
		instructionsLabel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if ("instructions".equals(event.text)) {
					TasksUiUtil.openUrl(DOC_URL_PREFIX + "Configuring+your+FishEye+Options+in+Eclipse");
				} else if ("more details".equals(event.text)) {
					TasksUiUtil.openUrl(DOC_URL_PREFIX + "More+about+Configuring+FishEye+Repositories+in+Eclipse");
				}
			}
		});
		mappingEditor = new SourceRepostioryMappingEditor(parent, null);
		mappingEditor.setRepositoryMappings(FishEyeUiUtil.getActiveScmRepositoryMappings());

		return ancestor;
	}

	private static final String DOC_URL_PREFIX = "http://confluence.atlassian.com/display/IDEPLUGIN/";

	@Override
	public boolean performOk() {
		try {
			FishEyeUiUtil.setScmRepositoryMappings(mappingEditor.getMapping());
		} catch (IOException e) {
			ErrorDialog.openError(getShell(), CoreConstants.PRODUCT_NAME,
					"Error while saving FishEye mapping configuration", new Status(IStatus.ERROR,
							FishEyeUiPlugin.PLUGIN_ID, e.getMessage(), e));
			return false;
		}
		return super.performOk();
	}
}