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

package com.atlassian.connector.eclipse.internal.crucible.core;

import org.eclipse.mylyn.internal.tasks.core.TaskTask;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import junit.framework.TestCase;

/**
 * @author Shawn Minto
 */
public class CrucibleRepositoryConnectorTest extends TestCase {

	public void testHasTaskChanged() {
		TaskRepository repo = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, "http://crucible.atlassian.com");
		CrucibleRepositoryConnector connector = new CrucibleRepositoryConnector();
		ITask task1 = new TaskTask(CrucibleCorePlugin.CONNECTOR_KIND, repo.getUrl(), "task1");
		TaskData taskData1 = new TaskData(new TaskAttributeMapper(repo), CrucibleCorePlugin.CONNECTOR_KIND,
				repo.getUrl(), "task1");

		TaskAttribute taskAttributeHCTK = new TaskAttribute(taskData1.getRoot(),
				CrucibleConstants.HAS_CHANGED_TASKDATA_KEY);

		taskData1.getAttributeMapper().setValue(taskAttributeHCTK, "true");
		assertTrue(connector.hasTaskChanged(repo, task1, taskData1));
		taskData1.getAttributeMapper().setValue(taskAttributeHCTK, "false");
		assertFalse(connector.hasTaskChanged(repo, task1, taskData1));

		TaskAttribute taskAttributeCHCK = new TaskAttribute(taskData1.getRoot(),
				CrucibleConstants.CHANGED_HASH_CODE_KEY);

		taskData1.getAttributeMapper().setValue(taskAttributeCHCK, String.valueOf(123456789));
		assertTrue(connector.hasTaskChanged(repo, task1, taskData1));

		taskData1.getAttributeMapper().setValue(taskAttributeCHCK, String.valueOf(123456789));
		task1.setAttribute(taskAttributeCHCK.getId(), String.valueOf(123456789));
		assertFalse(connector.hasTaskChanged(repo, task1, taskData1));

		task1.setAttribute(taskAttributeCHCK.getId(), String.valueOf(987654321));
		assertTrue(connector.hasTaskChanged(repo, task1, taskData1));

		taskData1.getAttributeMapper().setValue(taskAttributeCHCK, String.valueOf(987654321));
		assertFalse(connector.hasTaskChanged(repo, task1, taskData1));
	}

	public void testUpdateTaskFromTaskData() {
		TaskRepository repo = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, "http://crucible.atlassian.com");
		CrucibleRepositoryConnector connector = new CrucibleRepositoryConnector();
		ITask task1 = new TaskTask(CrucibleCorePlugin.CONNECTOR_KIND, repo.getUrl(), "task1");
		TaskData taskData1 = new TaskData(new TaskAttributeMapper(repo), CrucibleCorePlugin.CONNECTOR_KIND,
				repo.getUrl(), "task1");

		connector.updateTaskFromTaskData(repo, task1, taskData1);
		assertNull(task1.getAttribute(CrucibleConstants.CHANGED_HASH_CODE_KEY));

		TaskAttribute taskAttributeHCTK = new TaskAttribute(taskData1.getRoot(),
				CrucibleConstants.HAS_CHANGED_TASKDATA_KEY);

		taskData1.getAttributeMapper().setValue(taskAttributeHCTK, "true");
		connector.updateTaskFromTaskData(repo, task1, taskData1);
		assertNull(task1.getAttribute(CrucibleConstants.CHANGED_HASH_CODE_KEY));

		TaskAttribute taskAttributeCHCK = new TaskAttribute(taskData1.getRoot(),
				CrucibleConstants.CHANGED_HASH_CODE_KEY);

		taskData1.getAttributeMapper().setValue(taskAttributeCHCK, String.valueOf(123456789));
		connector.updateTaskFromTaskData(repo, task1, taskData1);
		assertEquals(task1.getAttribute(CrucibleConstants.CHANGED_HASH_CODE_KEY), "123456789");
	}
}
