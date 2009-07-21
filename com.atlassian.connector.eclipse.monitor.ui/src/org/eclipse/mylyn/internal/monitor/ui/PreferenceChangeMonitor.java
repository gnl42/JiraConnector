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

package org.eclipse.mylyn.internal.monitor.ui;

import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.mylyn.monitor.core.InteractionEvent;

/**
 * @author Mik Kersten
 */
public class PreferenceChangeMonitor implements IPropertyChangeListener {

	public void propertyChange(PropertyChangeEvent event) {
		String newValue = obfuscateValueIfContainsPath(event.getNewValue().toString());
		InteractionEvent interactionEvent = InteractionEvent.makePreference(event.getProperty(), newValue);
		MonitorUiPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}

	private String obfuscateValueIfContainsPath(String preferenceValue) {
		if (preferenceValue.indexOf(java.io.File.separator) != -1 || preferenceValue.indexOf('/') != -1) {
			return MonitorUiPlugin.OBFUSCATED_LABEL;
		} else {
			return preferenceValue;
		}
	}
}
