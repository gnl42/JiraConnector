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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.jira.core.model.Attachment;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttachment;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

/**
 * @author Steffen Pingel
 */
public class JiraTaskAttachmentHandler extends AbstractTaskAttachmentHandler {

	public class AttachmentPartSource implements PartSource {

		private final AbstractTaskAttachmentSource attachment;

		public AttachmentPartSource(AbstractTaskAttachmentSource attachment) {
			this.attachment = attachment;
		}

		public InputStream createInputStream() throws IOException {
			try {
				return attachment.createInputStream(null);
			} catch (CoreException e) {
				StatusHandler.log(new Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, "Error attaching file", e));
				throw new IOException("Failed to create source stream");
			}
		}

		public String getFileName() {
			return attachment.getName();
		}

		public long getLength() {
			return attachment.getLength();
		}

	}

	public JiraTaskAttachmentHandler() {
	}

	@Override
	public boolean canGetContent(TaskRepository repository, AbstractTask task) {
		return true;
	}

	@Override
	public boolean canPostContent(TaskRepository repository, AbstractTask task) {
		return true;
	}

	private void downloadAttachment(TaskRepository repository, AbstractTask task, String attachmentId,
			OutputStream out, IProgressMonitor monitor) throws CoreException {
		JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
		try {
			JiraIssue issue = client.getIssueByKey(task.getTaskKey(), monitor);
			Attachment jiraAttachment = issue.getAttachmentById(attachmentId);
			client.getAttachment(issue, jiraAttachment, out, monitor);
		} catch (JiraException e) {
			throw new CoreException(JiraCorePlugin.toStatus(repository, e));
		}
	}

	@Override
	public InputStream getContent(TaskRepository repository, AbstractTask task, TaskAttribute attachmentAttribute,
			IProgressMonitor monitor) throws CoreException {
		TaskAttachment attachment = TaskAttachment.createFrom(attachmentAttribute);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		downloadAttachment(repository, task, attachment.getAttachmentId(), out, monitor);
		return new ByteArrayInputStream(out.toByteArray());
	}

	@Override
	public void postContent(TaskRepository repository, AbstractTask task, AbstractTaskAttachmentSource source,
			String comment, TaskAttribute attachmentAttribute, IProgressMonitor monitor) throws CoreException {
		JiraClient server = JiraClientFactory.getDefault().getJiraClient(repository);
		try {
			JiraIssue issue = server.getIssueByKey(task.getTaskKey(), monitor);
			server.addAttachment(issue, comment, new AttachmentPartSource(source), source.getContentType(), monitor);
		} catch (JiraException e) {
			throw new CoreException(JiraCorePlugin.toStatus(repository, e));
		}
	}

}
