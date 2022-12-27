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

package me.glindholm.connector.eclipse.internal.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;

import me.glindholm.theplugin.commons.util.LoggerImpl;

/**
 * Class for handling Eclipse-based logging from the commons
 *
 * @author sminto
 */
public class JiraConnectorLogger extends LoggerImpl {

    private final String pluginId;

    public JiraConnectorLogger() {
        pluginId = JiraConnectorCorePlugin.PLUGIN_ID;
    }

    public JiraConnectorLogger(final String pluginId) {
        if (pluginId == null) {
            this.pluginId = JiraConnectorCorePlugin.PLUGIN_ID;
        } else {
            this.pluginId = pluginId;
        }
    }

    @Override
    public void log(final int level, final String msg, final Throwable t) {
        if (JiraConnectorCorePlugin.TRACE_COMMONS) {
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
