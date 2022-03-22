/**
 *
 */
package me.glindholm.connector.eclipse.internal.directclickthrough.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.mylyn.commons.core.StatusHandler;

public final class JettyLogger implements Logger {
    private boolean debug;

    public Logger getLogger(String name) {
        return this;
    }

    @Override
    public String getName() {
        return "JettyLogger";
    }

    @Override
    public void warn(String msg) {
        StatusHandler.log(new Status(IStatus.WARNING, DirectClickThroughUiPlugin.PLUGIN_ID, msg));
    }

    public void warn(String msg, Object arg0, Object arg1) {
        StatusHandler.log(new Status(IStatus.WARNING, DirectClickThroughUiPlugin.PLUGIN_ID, msg));
    }

    public void warn(String msg, Throwable th) {
        StatusHandler.log(new Status(IStatus.WARNING, DirectClickThroughUiPlugin.PLUGIN_ID, msg, th));
    }

    public void setDebugEnabled(boolean enabled) {
        debug = enabled;
    }

    @Override
    public void info(String arg0) {
        // don't care about info
        // StatusHandler.log(new Status(IStatus.INFO, DirectClickThroughUiPlugin.PLUGIN_ID, msg));
    }

    public void info(String msg, Object arg0, Object arg1) {
        // don't care about info
        // StatusHandler.log(new Status(IStatus.INFO, DirectClickThroughUiPlugin.PLUGIN_ID, msg));
    }

    public boolean isDebugEnabled() {
        return debug;
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

    @Override
    public void debug(String msg) {
        // TODO Auto-generated method stub

    }

}