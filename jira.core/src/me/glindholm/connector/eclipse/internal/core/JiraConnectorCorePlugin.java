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

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import me.glindholm.theplugin.commons.util.LoggerImpl;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Shawn Minto
 */
public class JiraConnectorCorePlugin extends Plugin {
    // The plug-in ID
    public static final String PLUGIN_ID = "me.glindholm.connector.eclipse.core";

    private static final String TRACE_COMMONS_PROPERTY_NAME = "me.glindholm.connector.eclipse.bamboo.core/trace/commons";

    public static final boolean TRACE_COMMONS = "true".equalsIgnoreCase(Platform.getDebugOption(TRACE_COMMONS_PROPERTY_NAME));

    // The shared instance
    private static JiraConnectorCorePlugin plugin;

    /**
     * The constructor
     */
    public JiraConnectorCorePlugin() {
        // make sure that we
        LoggerImpl.setInstance(new JiraConnectorLogger());
    }

    public String getVersion() {
        final Object version = getBundle().getHeaders().get(Constants.BUNDLE_VERSION);
        return version == null ? "0.0.0" : version.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static JiraConnectorCorePlugin getDefault() {
        return plugin;
    }

}
