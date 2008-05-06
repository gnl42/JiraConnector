/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests;

import junit.framework.TestCase;

import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;

/**
 * @author Mik Kersten
 */
public class JiraTaskTest extends TestCase {

	public void testPriorityMapping() {
		Priority priority = new Priority();
		priority.setId("1");
		assertEquals(PriorityLevel.P1, JiraRepositoryConnector.getPriorityLevel(priority));
	}

}
