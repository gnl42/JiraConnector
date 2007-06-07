/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui.editor;

import org.eclipse.mylar.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylar.internal.jira.ui.JiraCustomQuery;
import org.eclipse.mylar.tasks.ui.TaskFactory;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.mylar.tasks.ui.editors.AbstractNewRepositoryTaskEditor;
import org.eclipse.mylar.tasks.ui.search.SearchHitCollector;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * @author Steffen Pingel
 */
public class NewJiraTaskEditor extends AbstractNewRepositoryTaskEditor {

	public NewJiraTaskEditor(FormEditor editor) {
		super(editor);
	}

	@Override
	public SearchHitCollector getDuplicateSearchCollector(String searchString) {
		ContentFilter contentFilter = new ContentFilter(searchString, false, true, false, true);

		FilterDefinition filter = new FilterDefinition();
		filter.setContentFilter(contentFilter);
		JiraCustomQuery query = new JiraCustomQuery(repository.getUrl(), filter, repository.getCharacterEncoding());

		SearchHitCollector collector = new SearchHitCollector(TasksUiPlugin.getTaskListManager().getTaskList(),
				repository, query, new TaskFactory(repository));
		return collector;
	}

}
