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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.atlassian.connector.eclipse.internal.monitor.usage.MonitorUiPlugin;
import com.atlassian.connector.eclipse.internal.monitor.usage.MonitorUiPreferenceConstants;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = MonitorUiPlugin.getDefault().getPreferenceStore();

		store.setDefault(MonitorUiPreferenceConstants.PREF_MONITORING_ENABLED,
				MonitorUiPreferenceConstants.PREF_MONITORING_INITIALLY_ENABLED);

		store.setDefault(MonitorUiPreferenceConstants.PREF_MONITORING_FIRST_TIME, true);
	}
}
