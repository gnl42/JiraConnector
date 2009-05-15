package com.atlassian.connector.eclipse.bamboo.ui.preferences;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooConstants;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooUiPlugin;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = BambooUiPlugin.getDefault().getPreferenceStore();
		store.setDefault(BambooConstants.PREFERENCE_REFRESH_INTERVAL, BambooConstants.DEFAULT_REFRESH_INTERVAL);
		store.setDefault(BambooConstants.PREFERENCE_AUTO_REFRESH, BambooConstants.DEFAULT_AUTO_REFRESH);
	}

}
