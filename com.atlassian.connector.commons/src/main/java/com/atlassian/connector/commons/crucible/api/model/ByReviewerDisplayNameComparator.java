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

package com.atlassian.connector.commons.crucible.api.model;

import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import java.util.Comparator;

public class ByReviewerDisplayNameComparator implements Comparator<Reviewer> {
	public int compare(Reviewer o1, Reviewer o2) {
		final String dn1 = o1.getDisplayName();
		final String dn2 = o2.getDisplayName();
		return dn1.compareTo(dn2);
		// if (dn1 != null && dn2 != null) {
		// return dn1.compareTo(dn2);
		// }
		// return o1.getUsername().compareTo(o2.getUsername());
	}
}