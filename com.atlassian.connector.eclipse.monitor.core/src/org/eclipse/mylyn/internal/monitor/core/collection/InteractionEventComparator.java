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

import java.util.Comparator;

import org.eclipse.mylyn.monitor.core.InteractionEvent;

/**
 * Comparator of InteractionEvents
 * 
 * @author Gail Murphy
 */
public class InteractionEventComparator implements Comparator<InteractionEvent> {

	public int compare(InteractionEvent arg0, InteractionEvent arg1) {
		if (arg0.equals(arg1)) {
			return 0;
		}
		if (arg0.getDate().before(arg1.getDate())) {
			return -1;
		}
		return 1;
	}
}
