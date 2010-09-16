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

package com.atlassian.connector.eclipse.team.ui;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;

import junit.framework.TestCase;

@SuppressWarnings("restriction")
public final class TeamUiUtilsTests extends TestCase {
	private TestJavaProject project;

	private TestProject nonJavaProject;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		project = new TestJavaProject(this.getClass().getSimpleName());
		nonJavaProject = new TestProject(this.getClass().getSimpleName() + "nonJava");
	}

	@Override
	protected void tearDown() throws Exception {
		cleanupWorkspace();
		super.tearDown();
	}

	private void cleanupWorkspace() throws CoreException {
		ResourceTestUtil.deleteProject(project.getProject());
		ResourceTestUtil.deleteProject(nonJavaProject.getProject());

		waitForAutoBuild();
	}

	public void testGetSupportedTeamConnectors() {
		// @todo wseliga restore it
//		TestUtil.assertHasOnlyElements(TeamUiUtils.getSupportedTeamConnectors(), "Subversive", "Subclipse",
//				"Team API (partial support)", "CVS (FishEye only)");
	}

	public static void waitForAutoBuild() {
		boolean wasInterrupted = false;
		do {
			try {
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
				wasInterrupted = false;
			} catch (OperationCanceledException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);
	}

}
