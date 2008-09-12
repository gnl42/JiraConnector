/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
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
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.internal.jira.core.model.Attachment;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

/**
 * @author Steffen Pingel
 */
public class JiraTaskAttachmentHandler extends AbstractTaskAttachmentHandler {

	public class AttachmentPartSource implements PartSource {

		private final AbstractTaskAttachmentSource attachment;

		private final String filename;

		public AttachmentPartSource(AbstractTaskAttachmentSource attachment, String filename) {
			this.attachment = attachment;
			this.filename = filename;
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
			return filename;
		}

		public long getLength() {
			return attachment.getLength();
		}

	}

	public JiraTaskAttachmentHandler() {
	}

	@Override
	public boolean canGetContent(TaskRepository repository, ITask task) {
		return true;
	}

	@Override
	public boolean canPostContent(TaskRepository repository, ITask task) {
		return true;
	}

	private void downloadAttachment(TaskRepository repository, ITask task, String attachmentId, OutputStream out,
			IProgressMonitor monitor) throws CoreException {
		JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
		try {
			JiraIssue issue = client.getIssueByKey(task.getTaskKey(), monitor);
			Attachment jiraAttachment = issue.getAttachmentById(attachmentId);
			if (jiraAttachment == null) {
				throw new CoreException(new Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, "Attachment with id \""
						+ attachmentId + "\" for JIRA issue \"" + task.getTaskKey() + "\" not found"));
			}
			client.getAttachment(issue, jiraAttachment, out, monitor);
		} catch (JiraException e) {
			throw new CoreException(JiraCorePlugin.toStatus(repository, e));
		}
	}

	@Override
	public InputStream getContent(TaskRepository repository, ITask task, TaskAttribute attachmentAttribute,
			IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask("Getting attachment", IProgressMonitor.UNKNOWN);
			TaskAttachmentMapper attachment = TaskAttachmentMapper.createFrom(attachmentAttribute);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			downloadAttachment(repository, task, attachment.getAttachmentId(), out, monitor);
			return new ByteArrayInputStream(out.toByteArray());
		} finally {
			monitor.done();
		}
	}

	@Override
	public void postContent(TaskRepository repository, ITask task, AbstractTaskAttachmentSource source, String comment,
			TaskAttribute attachmentAttribute, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask("Sending attachment", IProgressMonitor.UNKNOWN);
			String contentType = source.getContentType();
			String filename = source.getName();
			if (attachmentAttribute != null) {
				TaskAttachmentMapper mapper = TaskAttachmentMapper.createFrom(attachmentAttribute);
				if (mapper.getContentType() != null) {
					contentType = mapper.getContentType();
				}
				if (mapper.getFileName() != null) {
					filename = mapper.getFileName();
				}
			}
			JiraClient server = JiraClientFactory.getDefault().getJiraClient(repository);
			try {
				JiraIssue issue = server.getIssueByKey(task.getTaskKey(), monitor);
				server.addAttachment(issue, comment, new AttachmentPartSource(source, filename), contentType, monitor);
			} catch (JiraException e) {
				throw new CoreException(JiraCorePlugin.toStatus(repository, e));
			}
		} finally {
			monitor.done();
		}
	}

}
