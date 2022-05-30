package me.glindholm.connector.eclipse.internal.directclickthrough.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.osgi.service.prefs.Preferences;

import me.glindholm.connector.eclipse.internal.directclickthrough.ui.DirectClickThroughUiPlugin;
import me.glindholm.connector.eclipse.internal.directclickthrough.ui.IDirectClickThroughPreferenceConstants;

public class DirectClickThroughPreferenceInitializer extends
AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        Preferences node = DefaultScope.INSTANCE.getNode(DirectClickThroughUiPlugin.PLUGIN_ID);
        node.putBoolean(IDirectClickThroughPreferenceConstants.ENABLED, IDirectClickThroughPreferenceConstants.DEFAULT_ENABLED);
        node.putInt(IDirectClickThroughPreferenceConstants.PORT_NUMBER, IDirectClickThroughPreferenceConstants.DEFAULT_PORT_NUMBER);
    }

}
