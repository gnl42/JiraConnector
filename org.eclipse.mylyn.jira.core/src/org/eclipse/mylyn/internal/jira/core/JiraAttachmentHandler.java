/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.jira.core.model.Attachment;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.tasks.core.deprecated.AbstractAttachmentHandler;
import org.eclipse.mylyn.internal.tasks.core.deprecated.ITaskAttachment;
import org.eclipse.mylyn.internal.tasks.core.deprecated.RepositoryAttachment;
import org.eclipse.mylyn.internal.tasks.core.deprecated.RepositoryTaskAttribute;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Steffen Pingel
 */
public class JiraAttachmentHandler extends AbstractAttachmentHandler {

	public JiraAttachmentHandler() {
	}

	@Override
	public void downloadAttachment(TaskRepository repository, RepositoryAttachment attachment, OutputStream out,
			IProgressMonitor monitor) throws CoreException {
		String id = attachment.getAttributeValue(RepositoryTaskAttribute.ATTACHMENT_ID);
		if (id == null) {
			throw new CoreException(new Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN,
					RepositoryStatus.ERROR_INTERNAL, "Attachment download from " + repository.getRepositoryUrl()
							+ " failed, missing attachment id.", null));
		}
		String key = attachment.getTaskId();
		if (key == null) {
			throw new CoreException(new Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN,
					RepositoryStatus.ERROR_INTERNAL, "Attachment download from " + repository.getRepositoryUrl()
							+ " failed, missing attachment key.", null));
		}

		JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
		try {
			JiraIssue issue = client.getIssueByKey(key, monitor);
			Attachment jiraAttachment = issue.getAttachmentById(id);
			client.getAttachment(issue, jiraAttachment, out, monitor);
		} catch (JiraException e) {
			throw new CoreException(JiraCorePlugin.toStatus(repository, e));
		}
	}

	@Override
	public InputStream getAttachmentAsStream(TaskRepository repository, RepositoryAttachment attachment,
			IProgressMonitor monitor) throws CoreException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		downloadAttachment(repository, attachment, out, monitor);
		return new ByteArrayInputStream(out.toByteArray());
	}

	@Override
	public void uploadAttachment(TaskRepository repository, ITask task, ITaskAttachment attachment,
			String comment, IProgressMonitor monitor) throws CoreException {
		JiraClient server = JiraClientFactory.getDefault().getJiraClient(repository);
		try {
			JiraIssue issue = server.getIssueByKey(task.getTaskKey(), monitor);
			server.addAttachment(issue, comment, new AttachmentPartSource(attachment), attachment.getContentType(),
					monitor);
		} catch (JiraException e) {
			throw new CoreException(JiraCorePlugin.toStatus(repository, e));
		}
	}

	@Override
	public boolean canDownloadAttachment(TaskRepository repository, ITask task) {
		return true;
	}

	@Override
	public boolean canUploadAttachment(TaskRepository repository, ITask task) {
		return true;
	}

	@Override
	public boolean canDeprecate(TaskRepository repository, RepositoryAttachment attachment) {
		return false;
	}

	@Override
	public void updateAttachment(TaskRepository repository, RepositoryAttachment attachment) throws CoreException {
		throw new UnsupportedOperationException();
	}
}
