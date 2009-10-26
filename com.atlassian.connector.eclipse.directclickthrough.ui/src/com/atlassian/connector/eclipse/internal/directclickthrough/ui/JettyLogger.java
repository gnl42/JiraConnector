/**
 * 
 */
package com.atlassian.connector.eclipse.internal.directclickthrough.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.mortbay.log.Logger;

public final class JettyLogger implements Logger {
	private boolean debug;

	public void warn(String msg, Object arg0, Object arg1) {
		StatusHandler.log(new Status(IStatus.WARNING, DirectClickThroughUiPlugin.PLUGIN_ID, msg));
	}

	public void warn(String msg, Throwable th) {
		StatusHandler.log(new Status(IStatus.WARNING, DirectClickThroughUiPlugin.PLUGIN_ID, msg, th));
	}

	public void setDebugEnabled(boolean enabled) {
		debug = enabled;
	}

	public boolean isDebugEnabled() {
		return debug;
	}

	public void info(String msg, Object arg0, Object arg1) {
		// don't care about info
		// StatusHandler.log(new Status(IStatus.INFO, DirectClickThroughUiPlugin.PLUGIN_ID, msg));
	}

	public Logger getLogger(String name) {
		return this;
	}

	public void debug(String msg, Object arg0, Object arg1) {
//		don't care about debug
//		if (!debug) {
//			return;
//		}
//		StatusHandler.log(new Status(IStatus.INFO, DirectClickThroughUiPlugin.PLUGIN_ID, msg));
	}

	public void debug(String msg, Throwable th) {
//		don't care about debug
//		if (!debug) {
//			return;
//		}
//		StatusHandler.log(new Status(IStatus.INFO, DirectClickThroughUiPlugin.PLUGIN_ID, msg, th));
	}
}