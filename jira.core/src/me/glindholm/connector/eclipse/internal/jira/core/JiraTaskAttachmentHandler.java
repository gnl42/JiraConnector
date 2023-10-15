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

package me.glindholm.connector.eclipse.internal.jira.core;

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

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraAttachment;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssue;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;

/**
 * @author Steffen Pingel
 */
public class JiraTaskAttachmentHandler extends AbstractTaskAttachmentHandler {

    public JiraTaskAttachmentHandler() {
    }

    @Override
    public boolean canGetContent(final TaskRepository repository, final ITask task) {
        return true;
    }

    @Override
    public boolean canPostContent(final TaskRepository repository, final ITask task) {
        return true;
    }

    private InputStream downloadAttachment(final TaskRepository repository, final ITask task, final String attachmentId, final IProgressMonitor monitor)
            throws CoreException {
        final JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
        try {
            final JiraIssue issue = client.getIssueByKey(task.getTaskKey(), monitor);
            final JiraAttachment jiraAttachment = issue.getAttachmentById(attachmentId);
            if (jiraAttachment == null) {
                throw new CoreException(new Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, "Attachment with id \"" //$NON-NLS-1$
                        + attachmentId + "\" for JIRA issue \"" + task.getTaskKey() + "\" not found")); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return client.getAttachment(issue, jiraAttachment, monitor);
        } catch (final JiraException e) {
            throw new CoreException(JiraCorePlugin.toStatus(repository, e));
        }
    }

    @Override
    public InputStream getContent(final TaskRepository repository, final ITask task, final TaskAttribute attachmentAttribute, IProgressMonitor monitor)
            throws CoreException {
        monitor = Policy.monitorFor(monitor);
        try {
            monitor.beginTask(Messages.JiraTaskAttachmentHandler_Getting_attachment, IProgressMonitor.UNKNOWN);
            final TaskAttachmentMapper attachment = TaskAttachmentMapper.createFrom(attachmentAttribute);
            // ByteArrayOutputStream out = new ByteArrayOutputStream();
            return downloadAttachment(repository, task, attachment.getAttachmentId(), monitor);
            // downloadAttachment(repository, task, attachment.getAttachmentId(), out, monitor);
            // return new ByteArrayInputStream(out.toByteArray());
        } finally {
            monitor.done();
        }
    }

    @Override
    public void postContent(final TaskRepository repository, final ITask task, final AbstractTaskAttachmentSource source, final String comment,
            final TaskAttribute attachmentAttribute, IProgressMonitor monitor) throws CoreException {
        monitor = Policy.monitorFor(monitor);
        try {
            final UnsubmittedTaskAttachment taskAttachment = new UnsubmittedTaskAttachment(source, attachmentAttribute);

            monitor.beginTask(Messages.JiraTaskAttachmentHandler_Sending_attachment, IProgressMonitor.UNKNOWN);
            String filename = source.getName();
            if (attachmentAttribute != null) {
                final TaskAttachmentMapper mapper = TaskAttachmentMapper.createFrom(attachmentAttribute);
                if (mapper.getFileName() != null) {
                    filename = mapper.getFileName();
                }
            }

            final JiraClient server = JiraClientFactory.getDefault().getJiraClient(repository);
            try (InputStream is = source.createInputStream(monitor)) {
                final JiraIssue issue = server.getIssueByKey(task.getTaskKey(), monitor);
                server.addAttachment(issue, comment, filename, IOUtils.toByteArray(is), monitor);
            } catch (JiraException | IOException e) {
                throw new CoreException(JiraCorePlugin.toStatus(repository, e));
            }
        } finally {
            monitor.done();
        }
    }
}
