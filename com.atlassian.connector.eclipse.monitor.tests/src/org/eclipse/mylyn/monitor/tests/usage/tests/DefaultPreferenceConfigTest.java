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

package org.eclipse.mylyn.monitor.tests.usage.tests;

import junit.framework.TestCase;

import org.eclipse.mylyn.internal.monitor.usage.MonitorPreferenceConstants;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;

/**
 * @author Mik Kersten
 */
public class DefaultPreferenceConfigTest extends TestCase {

	public void testMonitorPreferences() {
		assertNotNull(UiUsageMonitorPlugin.getDefault());
		assertTrue(UiUsageMonitorPlugin.getPrefs().getBoolean(MonitorPreferenceConstants.PREF_MONITORING_OBFUSCATE));
	}

}
