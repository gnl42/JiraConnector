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

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Shawn Minto
 */
public class AtlassianCorePlugin extends Plugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "com.atlassian.connector.eclipse.core";

	private static final String TRACE_COMMONS_PROPERTY_NAME = "com.atlassian.connector.eclipse.bamboo.core/trace/commons";

	public static final boolean TRACE_COMMONS = "true".equalsIgnoreCase(Platform.getDebugOption(TRACE_COMMONS_PROPERTY_NAME));

	// The shared instance
	private static AtlassianCorePlugin plugin;

	/**
	 * The constructor
	 */
	public AtlassianCorePlugin() {
		// make sure that we 
		LoggerImpl.setInstance(new AtlassianLogger());
	}

	public String getVersion() {
		Object version = getBundle().getHeaders().get(Constants.BUNDLE_VERSION);
		return version == null ? "0.0.0" : version.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static AtlassianCorePlugin getDefault() {
		return plugin;
	}

}
