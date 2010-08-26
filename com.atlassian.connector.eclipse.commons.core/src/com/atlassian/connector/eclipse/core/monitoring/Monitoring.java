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

package com.atlassian.connector.eclipse.core.monitoring;

import com.atlassian.connector.eclipse.monitor.core.InteractionEvent;
import com.atlassian.connector.eclipse.monitor.core.MonitorCorePlugin;
import com.atlassian.connector.eclipse.monitor.core.InteractionEvent.Kind;

import java.util.Date;

public final class Monitoring {
	private final String pluginId;

	private boolean available;

	public Monitoring(String pluginId) {
		this.pluginId = pluginId;

		try {
			Class.forName("com.atlassian.connector.eclipse.monitor.core.MonitorCorePlugin");
			available = true;
		} catch (ClassNotFoundException e) {
			available = false;
		}
	}

	public void logAction(String actionId, String details) {
		if (!available) {
			return;
		}

		MonitorCorePlugin plugin = MonitorCorePlugin.getDefault();
		if (plugin != null && plugin.isMonitoringEnabled() && plugin.getInteractionLogger() != null) {
			plugin.getInteractionLogger().interactionObserved(
					new InteractionEvent(Kind.ACTION, pluginId, actionId, details, new Date()));
		}
	}

	public void logJob(String jobId, String details) {
		if (!available) {
			return;
		}

		MonitorCorePlugin plugin = MonitorCorePlugin.getDefault();
		if (plugin != null && plugin.isMonitoringEnabled() && plugin.getInteractionLogger() != null) {
			plugin.getInteractionLogger().interactionObserved(
					new InteractionEvent(Kind.JOB, pluginId, jobId, details, new Date()));
		}
	}
}
