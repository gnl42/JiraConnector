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

package org.eclipse.mylyn.internal.monitor.ui;

import org.eclipse.mylyn.monitor.ui.AbstractUserActivityMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @author Mik Kersten
 * @author Rob Elves
 */
public class WorkbenchUserActivityMonitor extends AbstractUserActivityMonitor {

	private Listener interactionActivityListener;

	private Display display;

	@Override
	public void start() {
		display = MonitorUiPlugin.getDefault().getWorkbench().getDisplay();
		interactionActivityListener = new Listener() {
			public void handleEvent(Event event) {
				setLastEventTime(System.currentTimeMillis());
			}
		};

		display.addFilter(SWT.KeyUp, interactionActivityListener);
		display.addFilter(SWT.MouseUp, interactionActivityListener);
	}

	@Override
	public void stop() {
		if (display != null && !display.isDisposed() && interactionActivityListener != null) {
			display.removeFilter(SWT.KeyUp, interactionActivityListener);
			display.removeFilter(SWT.MouseUp, interactionActivityListener);
		}
	}
}
