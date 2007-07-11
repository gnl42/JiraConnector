/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.editor;

import org.eclipse.mylyn.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.ui.JiraCustomQuery;
import org.eclipse.mylyn.tasks.ui.TaskFactory;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.ui.editors.AbstractNewRepositoryTaskEditor;
import org.eclipse.mylyn.tasks.ui.search.SearchHitCollector;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * @author Steffen Pingel
 */
public class NewJiraTaskEditor extends AbstractNewRepositoryTaskEditor {

	public NewJiraTaskEditor(FormEditor editor) {
		super(editor);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);

		setExpandAttributeSection(true);
	}
	
	@Override
	public SearchHitCollector getDuplicateSearchCollector(String name) {
		String searchString = AbstractNewRepositoryTaskEditor.getStackTraceFromDescription(taskData.getDescription());
		ContentFilter contentFilter = new ContentFilter(searchString, false, true, false, true);

		FilterDefinition filter = new FilterDefinition();
		filter.setContentFilter(contentFilter);
		JiraCustomQuery query = new JiraCustomQuery(repository.getUrl(), filter, repository.getCharacterEncoding());

		SearchHitCollector collector = new SearchHitCollector(TasksUiPlugin.getTaskListManager().getTaskList(),
				repository, query, new TaskFactory(repository, false, false));
		return collector;
	}

}
