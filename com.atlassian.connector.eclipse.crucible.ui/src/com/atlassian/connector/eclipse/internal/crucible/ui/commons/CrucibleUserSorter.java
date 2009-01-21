/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.ui.commons;

import com.atlassian.connector.eclipse.internal.crucible.core.client.model.CrucibleCachedUser;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Sorter for cached users
 * 
 * @author Shawn Minto
 */
public class CrucibleUserSorter extends ViewerSorter {

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		// TODO add a special case for the Any user
		if (e1 instanceof CrucibleCachedUser && e2 instanceof CrucibleCachedUser) {
			CrucibleCachedUser u1 = (CrucibleCachedUser) e1;
			CrucibleCachedUser u2 = (CrucibleCachedUser) e2;
			return super.compare(viewer, u1, u2);
		}
		return super.compare(viewer, e1, e2);
	}
}
