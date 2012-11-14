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

package com.atlassian.connector.eclipse.internal.jira.core;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.UnsubmittedTaskAttachment;

import com.atlassian.connector.eclipse.internal.jira.core.model.Attachment;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;

/**
 * @author Steffen Pingel
 */
public class JiraTaskAttachmentHandler extends AbstractTaskAttachmentHandler {

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

	private InputStream downloadAttachment(TaskRepository repository, ITask task, String attachmentId,
			IProgressMonitor monitor) throws CoreException {
		JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
		try {
			JiraIssue issue = client.getIssueByKey(task.getTaskKey(), monitor);
			Attachment jiraAttachment = issue.getAttachmentById(attachmentId);
			if (jiraAttachment == null) {
				throw new CoreException(new Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, "Attachment with id \"" //$NON-NLS-1$
						+ attachmentId + "\" for JIRA issue \"" + task.getTaskKey() + "\" not found")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return client.getAttachment(issue, jiraAttachment, monitor);
		} catch (JiraException e) {
			throw new CoreException(JiraCorePlugin.toStatus(repository, e));
		}
	}

	@Override
	public InputStream getContent(TaskRepository repository, ITask task, TaskAttribute attachmentAttribute,
			IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask(Messages.JiraTaskAttachmentHandler_Getting_attachment, IProgressMonitor.UNKNOWN);
			TaskAttachmentMapper attachment = TaskAttachmentMapper.createFrom(attachmentAttribute);
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
			return downloadAttachment(repository, task, attachment.getAttachmentId(), monitor);
//			downloadAttachment(repository, task, attachment.getAttachmentId(), out, monitor);
//			return new ByteArrayInputStream(out.toByteArray());
		} finally {
			monitor.done();
		}
	}

	@Override
	public void postContent(TaskRepository repository, ITask task, AbstractTaskAttachmentSource source, String comment,
			TaskAttribute attachmentAttribute, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			UnsubmittedTaskAttachment taskAttachment = new UnsubmittedTaskAttachment(source, attachmentAttribute);

			monitor.beginTask(Messages.JiraTaskAttachmentHandler_Sending_attachment, IProgressMonitor.UNKNOWN);
			String filename = source.getName();
			if (attachmentAttribute != null) {
				TaskAttachmentMapper mapper = TaskAttachmentMapper.createFrom(attachmentAttribute);
				if (mapper.getFileName() != null) {
					filename = mapper.getFileName();
				}
			}

			InputStream is = source.createInputStream(monitor);
			JiraClient server = JiraClientFactory.getDefault().getJiraClient(repository);
			try {
				JiraIssue issue = server.getIssueByKey(task.getTaskKey(), monitor);
				server.addAttachment(issue, comment, filename, IOUtils.toByteArray(is), monitor);
			} catch (JiraException e) {
				throw new CoreException(JiraCorePlugin.toStatus(repository, e));
			} catch (IOException e) {
				throw new CoreException(JiraCorePlugin.toStatus(repository, e));
			} finally {
				IOUtils.closeQuietly(is);
			}
		} finally {
			monitor.done();
		}
	}
}
