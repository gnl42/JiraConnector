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

package org.eclipse.mylyn.jira.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import junit.framework.AssertionFailedError;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.jira.core.model.CustomField;
import org.eclipse.mylyn.internal.jira.core.model.Issue;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.tasks.core.RepositoryOperation;

public class JiraTestUtils {

	public static String PROJECT1 = "PRONE";

	public static Resolution getFixedResolution(JiraClient server) throws JiraException {
		refreshDetails(server);

		Resolution[] resolutions = server.getResolutions();
		for (Resolution resolution : resolutions) {
			if (Resolution.FIXED_ID.equals(resolution.getId())) {
				return resolution;
			}
		}
		return resolutions[0];
	}

	public static String getOperation(JiraClient server, String issueKey, String name) throws JiraException {
		refreshDetails(server);

		RepositoryOperation[] operations = server.getAvailableOperations(issueKey);
		for (RepositoryOperation operation : operations) {
			if (operation.getOperationName().toLowerCase().startsWith(name)) {
				return operation.getKnobName();
			}
		}

		throw new AssertionFailedError("Unable to find operation " + name + " in " + Arrays.asList(operations));
	}

	public static String getCustomField(JiraClient server, String name) throws JiraException {
		refreshDetails(server);

		CustomField[] fields = server.getCustomAttributes();
		for (CustomField field : fields) {
			if (field.getName().toLowerCase().startsWith(name.toLowerCase())) {
				return field.getId();
			}
		}
		return null;
	}

	public static Issue createIssue(JiraClient server, String summary) throws JiraException {
		refreshDetails(server);

		Issue issue = new Issue();
		issue.setProject(getProject1(server));
		issue.setType(server.getIssueTypes()[0]);
		issue.setSummary(summary);
		issue.setAssignee(server.getUserName());

		return server.createIssue(issue);
	}

	public static void refreshDetails(JiraClient server) throws JiraException {
		if (!server.hasDetails()) {
			server.refreshDetails(new NullProgressMonitor());
		}
	}

	public static Project getProject1(JiraClient server) {
		Project project = server.getProjectByKey(PROJECT1);
		if (project == null) {
			throw new AssertionFailedError("Project '" + PROJECT1 + "' not found");
		}
		return project;
	}

	public static byte[] readFile(File file) throws IOException {
		if (file.length() > 10000000) {
			throw new IOException("File too big: " + file.getAbsolutePath() + ", size: " + file.length());
		}

		byte[] data = new byte[(int) file.length()];
		InputStream in = new FileInputStream(file);
		try {
			in.read(data);
		} finally {
			in.close();
		}
		return data;
	}

	public static void writeFile(File file, byte[] data) throws IOException {
		OutputStream out = new FileOutputStream(file);
		try {
			out.write(data);
		} finally {
			out.close();
		}
	}

}
