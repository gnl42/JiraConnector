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

package org.eclipse.mylyn.internal.monitor.core.collection;

import java.util.Set;

import org.eclipse.mylyn.monitor.core.InteractionEvent;

/**
 * A usage scanner will see all events for a user before any consumers
 * 
 * @author Gail Murphy
 */
public interface IUsageScanner {

	public void scanEvent(InteractionEvent event, int userId);

	public boolean accept(int userId);

	public Set<Integer> acceptedUsers();

}
