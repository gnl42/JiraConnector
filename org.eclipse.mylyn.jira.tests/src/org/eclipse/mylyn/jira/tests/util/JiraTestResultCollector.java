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

package org.eclipse.mylyn.jira.tests.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;

/**
 * @author Steffen Pingel
 */
public class JiraTestResultCollector extends TaskDataCollector {

	public List<TaskData> results = new ArrayList<TaskData>();

	@Override
	public void accept(TaskData taskData) {
		results.add(taskData);
	}

}
