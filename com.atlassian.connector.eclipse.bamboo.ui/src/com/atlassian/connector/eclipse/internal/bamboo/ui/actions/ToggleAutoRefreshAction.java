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

package com.atlassian.connector.eclipse.internal.bamboo.ui.actions;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.action.Action;

public class ToggleAutoRefreshAction extends Action {

	public static final String ID = "com.atlassian.connector.eclipse.internal.bamboo.ui.actions.refresh.background"; //$NON-NLS-1$

	public ToggleAutoRefreshAction() {
		setText("Refresh Automatically");
		setId(ID);
		setChecked(BambooCorePlugin.isAutoRefresh());

		IEclipsePreferences preferences = new InstanceScope().getNode(BambooCorePlugin.PLUGIN_ID);
		preferences.addPreferenceChangeListener(new IPreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent event) {
				setChecked(BambooCorePlugin.isAutoRefresh());
			}
		});
	}

	@Override
	public void run() {
		BambooCorePlugin.toggleAutoRefresh();
	}

	/* it does not work (at least on Linux) as expected so I am using IPropertyChangeListener 
	@Override
	public boolean isChecked() {
		return BambooCorePlugin.isAutoRefresh();
	}
	*/
}
