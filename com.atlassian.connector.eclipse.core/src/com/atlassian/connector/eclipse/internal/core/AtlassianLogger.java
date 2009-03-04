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

package com.atlassian.connector.eclipse.internal.core;

import com.atlassian.theplugin.commons.util.LoggerImpl;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;

/**
 * Class for handling Eclipse-based logging from the commons
 * 
 * @author sminto
 */
public class AtlassianLogger extends LoggerImpl {

	private final String pluginId;

	public AtlassianLogger() {
		this.pluginId = AtlassianCorePlugin.PLUGIN_ID;
	}

	public AtlassianLogger(String pluginId) {
		if (pluginId == null) {
			this.pluginId = AtlassianCorePlugin.PLUGIN_ID;
		} else {
			this.pluginId = pluginId;
		}
	}

	public void log(int level, String msg, Throwable t) {
		if (AtlassianCorePlugin.TRACE_COMMONS) {
			int statusCode = IStatus.ERROR;
			if (level == LOG_INFO) {
				statusCode = IStatus.INFO;
			} else if (level == LOG_WARN) {
				statusCode = IStatus.WARNING;
			}

			StatusHandler.log(new Status(statusCode, pluginId, msg, t));
		}
	}
}
