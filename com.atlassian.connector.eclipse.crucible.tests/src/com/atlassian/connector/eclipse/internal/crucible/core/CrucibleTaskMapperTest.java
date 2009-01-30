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

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class CrucibleTaskMapperTest extends TestCase {

	private TaskRepository taskRepository;

	private StubTaskAttributeMapper mapper;

	@Override
	protected void setUp() throws Exception {
		taskRepository = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, "http://crucible.atlassian.com");
		mapper = new StubTaskAttributeMapper(taskRepository);
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSetTaskKey() {
		TaskData taskData = new TaskData(mapper, taskRepository.getConnectorKind(), taskRepository.getRepositoryUrl(),
				"task1");
		CrucibleTaskMapper taskMapper = new CrucibleTaskMapper(taskData, true);
		String key = taskMapper.getTaskKey();
		assertNull(key);

		taskMapper.setTaskKey("key");
		key = taskMapper.getTaskKey();

		assertEquals("key", key);

		key = taskMapper.getValue(TaskAttribute.TASK_KEY);
		assertEquals("key", key);

		taskData = new TaskData(mapper, taskRepository.getConnectorKind(), taskRepository.getRepositoryUrl(), "task2");
		taskData.getAttributeMapper().setValue(new TaskAttribute(taskData.getRoot(), TaskAttribute.TASK_KEY), "key");
		taskMapper = new CrucibleTaskMapper(taskData);
		key = taskMapper.getTaskKey();
		assertEquals("key", key);

		taskData = new TaskData(mapper, taskRepository.getConnectorKind(), taskRepository.getRepositoryUrl(), "task3");
		taskMapper = new CrucibleTaskMapper(taskData);
		key = taskMapper.getTaskKey();
		assertNull(key);

		taskMapper.setTaskKey("key");
		key = taskMapper.getTaskKey();
		key = taskMapper.getValue(TaskAttribute.TASK_KEY);
		assertNull(key);

		taskData = new TaskData(mapper, taskRepository.getConnectorKind(), taskRepository.getRepositoryUrl(), "task4");
		taskMapper = new CrucibleTaskMapper(taskData, false);
		key = taskMapper.getTaskKey();
		assertNull(key);

		taskMapper.setTaskKey("key");
		key = taskMapper.getTaskKey();
		key = taskMapper.getValue(TaskAttribute.TASK_KEY);
		assertNull(key);
	}

	private class StubTaskAttributeMapper extends TaskAttributeMapper {

		public StubTaskAttributeMapper(TaskRepository taskRepository) {
			super(taskRepository);
		}

		private final Map<String, String> attributeMap = new HashMap<String, String>();

		@Override
		public String mapToRepositoryKey(TaskAttribute parent, String key) {
			String mappedKey = attributeMap.get(key);
			return (mappedKey != null) ? mappedKey : key;
		}

	}
}
