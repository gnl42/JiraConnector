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

package org.eclipse.mylyn.internal.monitor.reports.collectors;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.mylyn.internal.monitor.core.collection.CommandUsageCollector;
import org.eclipse.mylyn.internal.monitor.core.collection.DelegatingUsageCollector;
import org.eclipse.mylyn.monitor.core.InteractionEvent;

/**
 * @author Mik Kersten
 */
public abstract class AbstractMylynUsageCollector extends DelegatingUsageCollector {

	protected Set<Integer> userIds = new HashSet<Integer>();

	protected Set<Integer> mylynUserIds = new HashSet<Integer>();

	protected Set<Integer> mylynInactiveUserIds = new HashSet<Integer>();

	protected CommandUsageCollector commandUsageCollector = new CommandUsageCollector();

	public AbstractMylynUsageCollector() {
		super.getDelegates().add(commandUsageCollector);
	}

	/**
	 * Overriders must call super.consumeEvent(..)
	 */
	@Override
	public void consumeEvent(InteractionEvent event, int userId) {
		super.consumeEvent(event, userId);
		userIds.add(userId);
		if (FocusedUiUsageDetector.isAMylynActivateCommand(event)) {
			mylynUserIds.add(userId);
			mylynInactiveUserIds.remove(userId);
		}
		if (FocusedUiUsageDetector.isAMylynDeactivateCommand(event)) {
			mylynInactiveUserIds.add(userId);
		}
	}

}
