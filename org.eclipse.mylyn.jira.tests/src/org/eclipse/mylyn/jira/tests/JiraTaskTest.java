/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.jira.tests;

import junit.framework.TestCase;

import org.eclipse.mylar.internal.jira.core.model.Priority;
import org.eclipse.mylar.internal.jira.ui.JiraRepositoryConnector;
import org.eclipse.mylar.tasks.core.Task.PriorityLevel;

/**
 * @author Mik Kersten
 */
public class JiraTaskTest extends TestCase {

	public void testPriorityMapping() {
		Priority priority = new Priority();
		priority.setId("1");
		assertEquals(PriorityLevel.P1, JiraRepositoryConnector.getMylarPriority(priority));
	}
	
}
