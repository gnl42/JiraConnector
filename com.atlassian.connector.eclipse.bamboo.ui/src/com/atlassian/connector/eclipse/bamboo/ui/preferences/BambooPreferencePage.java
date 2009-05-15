package com.atlassian.connector.eclipse.bamboo.ui.preferences;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooConstants;
import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.connector.eclipse.ui.preferences.PreferencesAdapter;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class BambooPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final int MIN_REFRESH_RATE = 1;

	private static final int MAX_REFRESH_RATE = 10000;

	public BambooPreferencePage() {
		super(GRID);
		setPreferenceStore(new PreferencesAdapter(BambooCorePlugin.getDefault().getPluginPreferences(),
				new PreferencesAdapter.SaveHandler() {
					public void doSave() {
						BambooCorePlugin.getDefault().savePluginPreferences();
					}
				}));
		setDescription("Atlassian Bamboo Settings");
	}

	public void createFieldEditors() {
		final IntegerFieldEditor refreshRate = new IntegerFieldEditor(BambooConstants.PREFERENCE_REFRESH_INTERVAL,
				"Auto &Refresh Interval Rate (minutes)", getFieldEditorParent(), 4);
		refreshRate.setValidRange(MIN_REFRESH_RATE, MAX_REFRESH_RATE);
		addField(refreshRate);
		addField(new BooleanFieldEditor(BambooConstants.PREFERENCE_AUTO_REFRESH, "Refresh &Automatically",
				getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}