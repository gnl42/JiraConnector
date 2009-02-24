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

package com.atlassian.connector.eclipse.internal.bamboo.core;

import org.eclipse.core.runtime.Status;

import junit.framework.TestCase;

public class BambooRepositoryConnectorTest extends TestCase {

	private BambooRepositoryConnector connector;

	@Override
	protected void setUp() throws Exception {
		this.connector = new BambooRepositoryConnector();
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCanCreateNewTask() {
		assertFalse(connector.canCreateNewTask(null));
	}

	public void testCanCreateTaskFromKey() {
		assertFalse(connector.canCreateNewTask(null));
	}

	public void testCanQuery() {
		assertFalse(connector.canCreateNewTask(null));
	}

	public void testCanSynchronizeTask() {
		assertFalse(connector.canCreateNewTask(null));
	}

	public void testGetConnectorKind() {
		assertEquals(BambooCorePlugin.CONNECTOR_KIND, connector.getConnectorKind());
	}

	public void testGetLabel() {
		assertEquals("Bamboo", connector.getLabel());
	}

	public void testGetRepositoryUrlFromTaskUrl() {
		assertNull(connector.getRepositoryUrlFromTaskUrl(null));
	}

	public void testGetTaskData() throws Exception {
		assertNull(connector.getTaskData(null, null, null));
	}

	public void testGetTaskIdFromTaskUrl() {
		assertNull(connector.getTaskIdFromTaskUrl(null));
	}

	public void testGetTaskUrl() {
		assertNull(connector.getTaskUrl(null, null));
	}

	public void testHasTaskChanged() {
		assertFalse(connector.hasTaskChanged(null, null, null));
	}

	public void testPerformQuery() {
		assertTrue(Status.OK_STATUS == connector.performQuery(null, null, null, null, null));
	}

	public void testGetClientManager() {
		BambooClientManager clientManager = connector.getClientManager();
		assertNotNull(clientManager);
		BambooClientManager clientManager2 = connector.getClientManager();
		assertEquals(clientManager, clientManager2);
	}

	public void testGetRepositoryConfigurationCacheFile() {
		assertEquals(BambooCorePlugin.getDefault().getRepositoryConfigurationCacheFile(),
				connector.getRepositoryConfigurationCacheFile());
	}

}
