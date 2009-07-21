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

import org.eclipse.core.runtime.Platform;
import org.eclipse.mylyn.internal.context.core.InteractionContextManager;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;

/**
 * Translates interaction with the shell/window to interaction events.
 * 
 * @author Mik Kersten
 */
@SuppressWarnings("restriction")
public class ShellLifecycleListener implements ShellListener {

	private final InteractionContextManager manager;

	public ShellLifecycleListener(InteractionContextManager manager) {
		this.manager = manager;

		String productId = InteractionContextManager.ACTIVITY_ORIGINID_WORKBENCH;
		if (Platform.getProduct() != null) {
			productId = Platform.getProduct().getId();
		}

		manager.processActivityMetaContextEvent(new InteractionEvent(InteractionEvent.Kind.ATTENTION,
				InteractionContextManager.ACTIVITY_STRUCTUREKIND_LIFECYCLE, productId,
				InteractionContextManager.ACTIVITY_ORIGINID_WORKBENCH, null,
				InteractionContextManager.ACTIVITY_DELTA_STARTED, 1f));
	}

	public void shellClosed(ShellEvent shellEvent) {
		// XXX: if this is only used for saving this is not right
		manager.deactivateAllContexts();

		String productId = InteractionContextManager.ACTIVITY_ORIGINID_WORKBENCH;
		if (Platform.getProduct() != null) {
			productId = Platform.getProduct().getId();
		}

		manager.processActivityMetaContextEvent(new InteractionEvent(InteractionEvent.Kind.ATTENTION,
				InteractionContextManager.ACTIVITY_STRUCTUREKIND_LIFECYCLE, productId,
				InteractionContextManager.ACTIVITY_ORIGINID_WORKBENCH, null,
				InteractionContextManager.ACTIVITY_DELTA_STOPPED, 1f));
//		ContextCore.getContextManager().saveActivityMetaContext();
	}

	public void shellDeactivated(ShellEvent arg0) {
		// ignore
	}

	public void shellActivated(ShellEvent arg0) {
		// ignore
	}

	public void shellDeiconified(ShellEvent arg0) {
		// ignore
	}

	public void shellIconified(ShellEvent arg0) {
		// ignore
	}
}
