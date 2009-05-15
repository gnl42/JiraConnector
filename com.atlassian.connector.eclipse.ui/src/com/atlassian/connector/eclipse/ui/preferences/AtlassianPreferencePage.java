package com.atlassian.connector.eclipse.ui.preferences;

import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By subclassing
 * <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main
 * plug-in class. That way, preferences can be accessed directly via the preference store.
 */

public class AtlassianPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	@Override
	protected Control createContents(Composite parent) {
		return new Composite(parent, SWT.NULL);
	}

	public AtlassianPreferencePage() {
		noDefaultAndApplyButton();
		setPreferenceStore(AtlassianUiPlugin.getDefault().getPreferenceStore());
		setDescription("Atlassian IntelliJ Connector Preferences");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}