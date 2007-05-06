/*******************************************************************************
 * Copyright (c) 2004 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylar.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylar.internal.jira.core.model.Attachment;
import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.service.JiraException;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylar.tasks.core.IAttachmentHandler;
import org.eclipse.mylar.tasks.core.MylarStatus;
import org.eclipse.mylar.tasks.core.RepositoryAttachment;
import org.eclipse.mylar.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylar.tasks.core.TaskRepository;

/**
 * @author Steffen Pingel
 */
public class JiraAttachmentHandler implements IAttachmentHandler {

	public final static String CONTEXT_ATTACHEMNT_FILENAME = "mylar-context.zip";

	public JiraAttachmentHandler() {
	}

	public void downloadAttachment(TaskRepository repository, RepositoryAttachment attachment, File file) throws CoreException {
		String id = attachment.getAttributeValue(RepositoryTaskAttribute.ATTACHMENT_ID);
		if (id == null) {
			throw new CoreException(new Status(IStatus.ERROR, JiraUiPlugin.PLUGIN_ID, MylarStatus.INTERNAL_ERROR, "Attachment download from " + repository.getUrl() + " failed, missing attachment id.", null));
		}
		String key = attachment.getTaskId();
		if (key == null) {
			throw new CoreException(new Status(IStatus.ERROR, JiraUiPlugin.PLUGIN_ID, MylarStatus.INTERNAL_ERROR, "Attachment download from " + repository.getUrl() + " failed, missing attachment key.", null));
		}
		
		JiraServer server = JiraServerFacade.getDefault().getJiraServer(repository);
		try {
			Issue issue = server.getIssueByKey(key);
			Attachment jiraAttachment = issue.getAttachmentById(id);
			server.retrieveFile(issue, jiraAttachment, file);
		} catch (JiraException e) {
			throw new CoreException(JiraCorePlugin.toStatus(e));
		}
	}

	public void uploadAttachment(TaskRepository repository, AbstractRepositoryTask task, String comment, String description, File file, String contentType, boolean isPatch) throws CoreException {
		String filename	= file.getName(); 
		if (AbstractRepositoryConnector.MYLAR_CONTEXT_DESCRIPTION.equals(description)) {
			filename = CONTEXT_ATTACHEMNT_FILENAME;
		}
		
		JiraServer server = JiraServerFacade.getDefault().getJiraServer(repository);
		try {
			Issue issue = server.getIssueByKey(task.getTaskKey());
			server.attachFile(issue, comment, filename, file, contentType);
		} catch (JiraException e) {
			throw new CoreException(JiraCorePlugin.toStatus(e));
		}
	}

	public boolean canDownloadAttachment(TaskRepository repository, AbstractRepositoryTask task) {
		return true;
	}

	public boolean canUploadAttachment(TaskRepository repository, AbstractRepositoryTask task) {
		return true;
	}

	public boolean canDeprecate(TaskRepository repository, RepositoryAttachment attachment) {		
		return false;
	}

	public void updateAttachment(TaskRepository repository, RepositoryAttachment attachment) throws CoreException {
		throw new UnsupportedOperationException();
	}

	public byte[] getAttachmentData(TaskRepository repository, RepositoryAttachment attachment) throws CoreException {
		String id = attachment.getAttributeValue(RepositoryTaskAttribute.ATTACHMENT_ID);
		if (id == null) {
			throw new CoreException(new Status(IStatus.ERROR, JiraUiPlugin.PLUGIN_ID, MylarStatus.INTERNAL_ERROR, "Attachment download from " + repository.getUrl() + " failed, missing attachment id.", null));
		}
		String key = attachment.getTaskId();
		if (key == null) {
			throw new CoreException(new Status(IStatus.ERROR, JiraUiPlugin.PLUGIN_ID, MylarStatus.INTERNAL_ERROR, "Attachment download from " + repository.getUrl() + " failed, missing attachment key.", null));
		}
		
		JiraServer server = JiraServerFacade.getDefault().getJiraServer(repository);
		try {
			Issue issue = server.getIssueByKey(key);
			Attachment jiraAttachment = issue.getAttachmentById(id);
			return server.retrieveFile(issue, jiraAttachment);
		} catch (JiraException e) {
			throw new CoreException(JiraCorePlugin.toStatus(e));
		}
	}
	
}
