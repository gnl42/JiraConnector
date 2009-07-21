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

import org.eclipse.jface.action.IAction;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.monitor.ui.IActionExecutionListener;

/**
 * @author Mik Kersten
 */
public class ActionExecutionMonitor implements IActionExecutionListener {

	public void actionObserved(IAction action) {
		InteractionEvent interactionEvent = InteractionEvent.makeCommand(action.getId(), ""); //$NON-NLS-1$
		MonitorUiPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}
}
