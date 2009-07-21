/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Leah Findlater - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.monitor.ui;

import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Logs all bug root window selections (i.e. the window that the workbench is launced with).
 * 
 * @author Mik Kersten
 * @author Leah Findlater
 * 
 */
public class WindowChangeMonitor implements IWindowListener {

	public static final String WINDOW_CLOSED = "closed"; //$NON-NLS-1$

	public static final String WINDOW_OPENED = "opened"; //$NON-NLS-1$

	public static final String WINDOW_ACTIVATED = "activated"; //$NON-NLS-1$

	public static final String WINDOW_DEACTIVATED = "deactivated"; //$NON-NLS-1$

	public WindowChangeMonitor() {
		super();
	}

	// TODO: Should we add the default set of monitors to the new window as
	// well?
	public void windowOpened(IWorkbenchWindow window) {
		InteractionEvent interactionEvent = InteractionEvent.makeCommand(getWindowOrigin(window), WINDOW_OPENED);
		MonitorUiPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}

	public void windowClosed(IWorkbenchWindow window) {
		InteractionEvent interactionEvent = InteractionEvent.makeCommand(getWindowOrigin(window), WINDOW_CLOSED);
		MonitorUiPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}

	public void windowDeactivated(IWorkbenchWindow window) {
//		InteractionEvent interactionEvent = InteractionEvent.makeCommand(getWindowOrigin(window),
//				WINDOW_DEACTIVATED);
//		MylynMonitorUiPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}

	public void windowActivated(IWorkbenchWindow window) {
		InteractionEvent interactionEvent = InteractionEvent.makeCommand(getWindowOrigin(window), WINDOW_ACTIVATED);
		MonitorUiPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}

	protected String getWindowOrigin(IWorkbenchWindow window) {
		return window.getClass().getCanonicalName();// + "@" + window.hashCode();
	}
}
