/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.theplugin.commons.crucible.api.model.changes;

import com.atlassian.theplugin.commons.util.MiscUtil;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.List;

public final class Changes {
	private final boolean olderChangeSetsExist;

	private final boolean newerChangeSetsExist;

	private final List<Change> changes;

	public Changes(boolean olderExist, boolean newestExist, @NotNull Collection<Change> changes) {
		olderChangeSetsExist = olderExist;
		newerChangeSetsExist = newestExist;
		this.changes = MiscUtil.buildArrayList(changes);
	}

	public boolean isOlderChangeSetsExist() {
		return olderChangeSetsExist;
	}

	public boolean isNewerChangeSetsExist() {
		return newerChangeSetsExist;
	}

	public List<Change> getChanges() {
		return changes;
	}
}
