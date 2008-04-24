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

import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataCollector;

/**
 * @author Steffen Pingel
 */
public class TaskDataCollector extends AbstractTaskDataCollector {

	public List<RepositoryTaskData> results = new ArrayList<RepositoryTaskData>();

	@Override
	public void accept(RepositoryTaskData taskData) {
		results.add(taskData);
	}

}
