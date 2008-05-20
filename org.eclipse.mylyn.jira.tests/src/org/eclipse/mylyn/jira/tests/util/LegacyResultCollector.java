/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mylyn.internal.tasks.core.deprecated.LegacyTaskDataCollector;
import org.eclipse.mylyn.internal.tasks.core.deprecated.RepositoryTaskData;

/**
 * @author Steffen Pingel
 */
@SuppressWarnings("deprecation")
public class LegacyResultCollector extends LegacyTaskDataCollector {

	public List<RepositoryTaskData> results = new ArrayList<RepositoryTaskData>();

	@Override
	public void accept(RepositoryTaskData taskData) {
		results.add(taskData);
	}

}
