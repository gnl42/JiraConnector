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

package com.atlassian.theplugin.commons.fisheye.api.model.changeset;

import com.atlassian.theplugin.commons.util.MiscUtil;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public final class ChangesetIdList {

	private final List<String> csids;

	public ChangesetIdList(@NotNull List<String> csids) {
		this.csids = MiscUtil.buildArrayList(csids);
	}

	public List<String> getCsids() {
		return csids;
	}

}
