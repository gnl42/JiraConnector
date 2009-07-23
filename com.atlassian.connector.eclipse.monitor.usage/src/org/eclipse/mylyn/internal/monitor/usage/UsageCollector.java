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

package org.eclipse.mylyn.internal.monitor.usage;

import java.util.ArrayList;
import java.util.Collection;

public final class UsageCollector {

	private final String uploadUrl;

	private final Collection<String> eventFilters;

	UsageCollector(final String uploadUrl, final Collection<String> eventFilters) {
		this.uploadUrl = uploadUrl;
		this.eventFilters = new ArrayList<String>();
		this.eventFilters.addAll(eventFilters);
	}

	public String getUploadUrl() {
		return uploadUrl;
	}

	public Collection<String> getEventFilters() {
		return eventFilters;
	}

}
