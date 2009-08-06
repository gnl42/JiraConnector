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

import java.util.Collection;

/**
 * @author Mik Kersten
 * @author Leah Findlater
 */
public class StudyParameters {

	private Collection<UsageCollector> usageCollectors;

	public void setUsageCollectors(Collection<UsageCollector> usageCollectors) {
		this.usageCollectors = usageCollectors;
	}

	public Collection<UsageCollector> getUsageCollectors() {
		return usageCollectors;
	}
}
