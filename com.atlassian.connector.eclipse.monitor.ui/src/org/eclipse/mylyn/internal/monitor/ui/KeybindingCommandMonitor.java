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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.monitor.ui.AbstractCommandMonitor;

/**
 * @author Mik Kersten
 */
public class KeybindingCommandMonitor extends AbstractCommandMonitor {

	public static final String COMMAND_INVOKED = "keybinding"; //$NON-NLS-1$

	@Override
	protected void handleCommandExecution(String commandId, ExecutionEvent event) {
		InteractionEvent interactionEvent = InteractionEvent.makeCommand(commandId, COMMAND_INVOKED);
		MonitorUiPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}

}
