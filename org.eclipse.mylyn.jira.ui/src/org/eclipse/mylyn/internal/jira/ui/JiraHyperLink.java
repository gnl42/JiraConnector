/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.mylyn.internal.jira.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;

/**
 * @author Eugene Kuleshov
 */
public class JiraHyperLink implements IHyperlink {

	private final IRegion region;

	private final TaskRepository repository;

	private final String key;

	private final String taskUrl;

	public JiraHyperLink(IRegion nlsKeyRegion, TaskRepository repository, String key, String taskUrl) {
		this.region = nlsKeyRegion;
		this.repository = repository;
		this.key = key;
		this.taskUrl = taskUrl;
	}

	public IRegion getHyperlinkRegion() {
		return region;
	}

	public String getTypeLabel() {
		return null;
	}

	public String getHyperlinkText() {
		return "Open Task " + key;
	}

	public void open() {
		if (repository != null) {
			TasksUiUtil.openRepositoryTask(repository.getUrl(), key, taskUrl);
		} else {
			MessageDialog.openError(null, "Mylyn Jira Connector", "Could not determine repository for report");
		}
	}

}
