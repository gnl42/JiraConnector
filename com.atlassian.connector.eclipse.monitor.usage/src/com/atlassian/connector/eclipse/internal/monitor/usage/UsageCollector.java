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

package com.atlassian.connector.eclipse.internal.monitor.usage;

import java.util.ArrayList;
import java.util.Collection;

public final class UsageCollector {

	private final String bundle;

	private final String uploadUrl;

	private final Collection<String> eventFilters;

	private final String detailsUrl;

	UsageCollector(final String bundle, final String uploadUrl, final String detailsUrl,
			final Collection<String> eventFilters) {
		this.bundle = bundle;
		this.uploadUrl = uploadUrl;
		this.eventFilters = new ArrayList<String>();
		this.eventFilters.addAll(eventFilters);
		this.detailsUrl = detailsUrl;
	}

	public String getUploadUrl() {
		return uploadUrl;
	}

	public Collection<String> getEventFilters() {
		return eventFilters;
	}

	public String getBundle() {
		return bundle;
	}

	public String getDetailsUrl() {
		return detailsUrl;
	}

}
