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

package com.atlassian.connector.eclipse.internal.crucible.ui;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleConstants;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.CrucibleCustomFilterPage;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.CrucibleNamedFilterPage;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import junit.framework.TestCase;

/**
 * @author Shawn Minto
 */
public class RepositoryConnectorUiTest extends TestCase {

	public void testGetQueryWizard() {
		CrucibleRepositoryConnectorUi connectorUi = new CrucibleRepositoryConnectorUi();

		TaskRepository taskRepository = new TaskRepository(CrucibleCorePlugin.CONNECTOR_KIND, "https://testRepo");
		IRepositoryQuery query = null;
		IWizard wizard = connectorUi.getQueryWizard(taskRepository, query);
		assertNotNull(wizard);
		assertEquals(1, wizard.getPageCount());
		assertTrue(wizard.getPages()[0] instanceof CrucibleNamedFilterPage);

		query = new RepositoryQuery(CrucibleCorePlugin.CONNECTOR_KIND, "test-handle");
		query.setSummary("test Summary");
		query.setAttribute(CrucibleConstants.KEY_FILTER_ID, "randomFilter:");

		wizard = connectorUi.getQueryWizard(taskRepository, query);
		assertNotNull(wizard);
		assertEquals(1, wizard.getPageCount());
		assertTrue(wizard.getPages()[0] instanceof CrucibleNamedFilterPage);

		query = new RepositoryQuery(CrucibleCorePlugin.CONNECTOR_KIND, "test-handle");
		query.setSummary("test Summary");

		wizard = connectorUi.getQueryWizard(taskRepository, query);
		assertNotNull(wizard);
		assertEquals(1, wizard.getPageCount());
		assertTrue(wizard.getPages()[0] instanceof CrucibleCustomFilterPage);

	}
}
