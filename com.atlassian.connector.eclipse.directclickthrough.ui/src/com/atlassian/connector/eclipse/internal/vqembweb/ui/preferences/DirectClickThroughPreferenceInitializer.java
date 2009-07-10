package com.atlassian.connector.eclipse.internal.vqembweb.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.osgi.service.prefs.Preferences;

import com.atlassian.connector.eclipse.internal.vqembweb.ui.AtlassianVqembwebUiPlugin;
import com.atlassian.connector.eclipse.internal.vqembweb.ui.IDirectClickThroughPreferenceConstants;

public class DirectClickThroughPreferenceInitializer extends
		AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		Preferences node = new DefaultScope().getNode(AtlassianVqembwebUiPlugin.PLUGIN_ID);
		node.putBoolean(IDirectClickThroughPreferenceConstants.ENABLED, IDirectClickThroughPreferenceConstants.DEFAULT_ENABLED);
		node.putInt(IDirectClickThroughPreferenceConstants.PORT_NUMBER, IDirectClickThroughPreferenceConstants.DEFAULT_PORT_NUMBER);
	}

}
