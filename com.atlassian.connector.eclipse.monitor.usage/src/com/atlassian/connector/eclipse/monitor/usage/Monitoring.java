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

package com.atlassian.connector.eclipse.monitor.usage;

import java.util.Date;

import com.atlassian.connector.eclipse.internal.monitor.usage.UiUsageMonitorPlugin;
import com.atlassian.connector.eclipse.monitor.usage.InteractionEvent.Kind;

public final class Monitoring {
	private final String pluginId;

	public Monitoring(String pluginId) {
		this.pluginId = pluginId;
	}

	public void logAction(String actionId, String details) {
		if (UiUsageMonitorPlugin.getDefault().isMonitoringEnabled()) {
			UiUsageMonitorPlugin.getDefault().getInteractionLogger().interactionObserved(
					new InteractionEvent(Kind.ACTION, pluginId, actionId, details, new Date()));
		}
	}

	public void logJob(String jobId, String details) {
		if (UiUsageMonitorPlugin.getDefault().isMonitoringEnabled()) {
			UiUsageMonitorPlugin.getDefault().getInteractionLogger().interactionObserved(
					new InteractionEvent(Kind.JOB, pluginId, jobId, details, new Date()));
		}
	}
}
