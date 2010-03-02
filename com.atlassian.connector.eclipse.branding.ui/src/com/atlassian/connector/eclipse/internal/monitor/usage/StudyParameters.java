/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Leah Findlater - improvements
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.monitor.usage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Mik Kersten
 * @author Leah Findlater
 */
public class StudyParameters {

	private final String name;

	private final String uploadUrl;

	private final Collection<String> eventFilters;

	private final String detailsUrl;

	public StudyParameters(final String name, final String uploadUrl, final String detailsUrl,
			final String[] eventFilters) {
		this.name = name;
		this.uploadUrl = uploadUrl;
		this.eventFilters = new ArrayList<String>();
		this.eventFilters.addAll(Arrays.asList(eventFilters));
		this.detailsUrl = detailsUrl;
	}

	public String getUploadUrl() {
		return uploadUrl;
	}

	public Collection<String> getEventFilters() {
		return eventFilters;
	}

	public String getDetailsUrl() {
		return detailsUrl;
	}

	public String getName() {
		return name;
	}

}