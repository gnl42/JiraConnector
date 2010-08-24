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

package com.atlassian.connector.eclipse.internal.monitor.usage.preferences;

import java.util.UUID;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.atlassian.connector.eclipse.internal.monitor.usage.MonitorPreferenceConstants;
import com.atlassian.connector.eclipse.internal.monitor.usage.UiUsageMonitorPlugin;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = UiUsageMonitorPlugin.getDefault().getPreferenceStore();

		store.setDefault(MonitorPreferenceConstants.PREF_MONITORING_ENABLED,
				MonitorPreferenceConstants.PREF_MONITORING_INITIALLY_ENABLED);

		store.setDefault(MonitorPreferenceConstants.PREF_MONITORING_SUBMIT_FREQUENCY,
				UiUsageMonitorPlugin.DEFAULT_DELAY_BETWEEN_TRANSMITS);

		store.setDefault(MonitorPreferenceConstants.PREF_MONITORING_FIRST_TIME, true);

		if (!store.contains(MonitorPreferenceConstants.PREF_MONITORING_USER_ID)
				|| "".equals(store.getString(MonitorPreferenceConstants.PREF_MONITORING_USER_ID))) {
			store.setValue(MonitorPreferenceConstants.PREF_MONITORING_USER_ID, UUID.randomUUID().toString());
		}

		store.setValue(MonitorPreferenceConstants.PREF_MONITORING_STARTED, false);
	}
}
