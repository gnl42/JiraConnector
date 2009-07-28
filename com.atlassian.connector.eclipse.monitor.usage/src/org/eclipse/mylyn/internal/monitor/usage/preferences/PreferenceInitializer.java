/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.monitor.usage.preferences;

import java.util.UUID;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.mylyn.internal.monitor.usage.MonitorPreferenceConstants;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = UiUsageMonitorPlugin.getDefault().getPreferenceStore();

		store.setDefault(MonitorPreferenceConstants.PREF_MONITORING_OBFUSCATE, true);

		if (!store.contains(MonitorPreferenceConstants.PREF_MONITORING_INITIALLY_ENABLED)) {
			store.setValue(MonitorPreferenceConstants.PREF_MONITORING_INITIALLY_ENABLED, false);
			store.setValue(MonitorPreferenceConstants.PREF_MONITORING_ENABLED, false);
		}

		if (!store.contains(MonitorPreferenceConstants.PREF_MONITORING_ENABLE_SUBMISSION_INITITALLY_ENABLED)) {
			store.setValue(MonitorPreferenceConstants.PREF_MONITORING_ENABLE_SUBMISSION_INITITALLY_ENABLED, true);
			store.setValue(MonitorPreferenceConstants.PREF_MONITORING_ENABLE_SUBMISSION, true);
		}

		if (!store.contains(MonitorPreferenceConstants.PREF_MONITORING_USER_ID)) {
			store.setValue(MonitorPreferenceConstants.PREF_MONITORING_USER_ID, UUID.randomUUID().toString());
		}

		store.setValue(MonitorPreferenceConstants.PREF_MONITORING_STARTED, false);
	}
}
