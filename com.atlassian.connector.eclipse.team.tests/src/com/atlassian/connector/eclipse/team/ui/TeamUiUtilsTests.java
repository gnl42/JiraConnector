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

import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.mylyn.java.tests.AbstractJavaContextTest;
import org.eclipse.mylyn.java.tests.AllJavaTests;
import org.eclipse.mylyn.java.tests.TestJavaProject;
import org.eclipse.mylyn.java.tests.TestProject;
import org.eclipse.mylyn.resources.tests.ResourceTestUtil;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

@SuppressWarnings("restriction")
public final class TeamUiUtilsTests extends TestCase {
	private final AllJavaTests test = new AllJavaTests();

	private TestJavaProject project;

	private TestProject nonJavaProject;

	public void testFindResourceForPath() throws CoreException, UnsupportedEncodingException {
		final IPackageFragment p1 = project.createPackage("p1");
		final IType type1 = project.createType(p1, "Type1.java", "public class Type1 { }");
		final IType type2 = project.createType(p1, "Type2.java", "public class Type2 { }");
		final IFolder metaInfFolder = nonJavaProject.getProject().getFolder("META-INF");
		metaInfFolder.create(true, true, null);
		final IFile manifestFile = metaInfFolder.getFile("MANIFEST.MF");
		ByteArrayInputStream in = new ByteArrayInputStream("ABC".getBytes("UTF-8"));
		manifestFile.create(in, true, null);

		TeamUiResourceManager teamResourceManager = AtlassianTeamUiPlugin.getDefault().getTeamResourceManager();
		final ITeamUiResourceConnector mockConnector = Mockito.mock(ITeamUiResourceConnector.class);
		when(mockConnector.canHandleFile(Matchers.<IFile> anyObject())).thenReturn(true);
		when(mockConnector.isEnabled()).thenReturn(true);
		ScmRepository javaProjectScmRepository = new ScmRepository("http://localhost/svn2/trunk",
				"http://localhost/svn2", "svnrepo2", mockConnector);
		ScmRepository nonjavaProjectScmRepository = new ScmRepository("http://localhost/svn/trunk",
				"http://localhost/svn", "svnrepo", mockConnector);
		when(mockConnector.getApplicableRepository(project.getProject())).thenReturn(javaProjectScmRepository);
		when(mockConnector.getApplicableRepository(nonJavaProject.getProject())).thenReturn(nonjavaProjectScmRepository);

		teamResourceManager.addTeamConnector(mockConnector);

		assertNull(TeamUiUtils.findResourceForPath("http://localhost/svn", "trunk/src/a/b/A.java", null));
		assertEquals(manifestFile, TeamUiUtils.findResourceForPath("http://localhost/svn",
				"trunk/non-java-project/META-INF/MANIFEST.MF", null));

		assertNull(TeamUiUtils.findResourceForPath("http://localhost2/svn",
				"trunk/non-java-project/META-INF/MANIFEST.MF", null));

		assertEquals(manifestFile, TeamUiUtils.findResourceForPath("http://localhost/svn",
				"trunk/non-java-project/META-INF/MANIFEST.MF", null));

		assertEquals(null, TeamUiUtils.findResourceForPath("http://localhost/svn2",
				"trunk/some-java-project/p1/Type1.java", null));
		assertEquals(type1.getResource(), TeamUiUtils.findResourceForPath("http://localhost/svn2",
				"trunk/some-java-project/src/p1/Type1.java", null));
		assertEquals(type2.getResource(), TeamUiUtils.findResourceForPath("http://localhost/svn2",
				"trunk/some-java-project/src/p1/Type2.java", null));
		assertEquals(type1.getResource(), TeamUiUtils.findResourceForPath("http://localhost/svn2",
				"branches/r1-1/some-java-project/src/p1/Type1.java", null));

		assertEquals(null, TeamUiUtils.findResourceForPath("http://localhost/svn",
				"trunk/some-java-project/src/p1/Type2.java", null));

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cleanupWorkspace();
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

		AbstractJavaContextTest.waitForAutoBuild();
	}

	public void testGetSupportedTeamConnectors() {
		// @todo wseliga restore it
//		TestUtil.assertHasOnlyElements(TeamUiUtils.getSupportedTeamConnectors(), "Subversive", "Subclipse",
//				"Team API (partial support)", "CVS (FishEye only)");
	}

}
