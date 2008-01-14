/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.editor;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.mylyn.internal.tasks.ui.editors.AbstractAttributeEditorManager;
import org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.internal.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.internal.tasks.ui.editors.AttributeEditorToolkit;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.editors.RepositoryTaskEditorInput;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * NOTE: This class is work in progress and currently not used.
 * 
 * @author Steffen Pingel
 */
@SuppressWarnings("restriction")
public class JiraTaskEditor2 extends AbstractTaskEditorPage {

	@SuppressWarnings("restriction")
	private class JiraAttributeEditorManager extends AbstractAttributeEditorManager {

		public JiraAttributeEditorManager(RepositoryTaskEditorInput input) {
			super(input);
		}

		@Override
		public void addTextViewer(SourceViewer viewer) {
			JiraTaskEditor2.this.textViewers.add(viewer);

		}

		@Override
		public boolean attributeChanged(RepositoryTaskAttribute attribute) {
			return JiraTaskEditor2.this.attributeChanged(attribute);
		}

		@Override
		public void configureContextMenuManager(MenuManager menuManager) {
			// TODO EDITOR
		}

		@Override
		public Color getColorIncoming() {
			return JiraTaskEditor2.this.getColorIncoming();
		}

		@Override
		public TaskRepository getTaskRepository() {
			return JiraTaskEditor2.this.getTaskRepository();
		}

	}

	private AttributeEditorFactory attributeEditorFactory;

	private JiraAttributeEditorManager attributeEditorManager;

	private AttributeEditorToolkit attributeEditorToolkit;

	public JiraTaskEditor2(FormEditor editor) {
		super(editor);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		attributeEditorManager = new JiraAttributeEditorManager((RepositoryTaskEditorInput) getEditorInput());
		attributeEditorFactory = new AttributeEditorFactory(attributeEditorManager);
		attributeEditorToolkit = new AttributeEditorToolkit() {

//			@Override
//			protected boolean hasContentAssist(RepositoryOperation repositoryOperation) {
//				if ("assignee".equals(repositoryOperation.getInputName())) {
//					return true;
//				}
//				return super.hasContentAssist(repositoryOperation);
//			}
//		
//			@Override
//			protected boolean hasContentAssist(RepositoryTaskAttribute attribute) {
//				String key = attribute.getMetaDataValue(JiraAttributeFactory.TYPE_KEY);
//				// TODO need more robust detection
//				if (JiraFieldType.USERPICKER.getKey().equals(key)) {
//					return true;
//				}
//		
//				return super.hasContentAssist(attribute);
//			}

		};

		super.createFormContent(managedForm);
	}

	@Override
	protected AttributeEditorFactory getAttributeEditorFactory() {
		return attributeEditorFactory;
	}

	@Override
	protected AbstractAttributeEditorManager getAttributeEditorManager() {
		return attributeEditorManager;
	}

	@Override
	public AttributeEditorToolkit getAttributeEditorToolkit() {
		return attributeEditorToolkit;
	}

	@Override
	protected String getHistoryUrl() {
		if (taskData != null) {
			String taskId = taskData.getTaskKey();
			String repositoryUrl = taskData.getRepositoryUrl();
			if (getConnector() != null && repositoryUrl != null && taskId != null) {
				String url = getConnector().getTaskUrl(repositoryUrl, taskId);
				//AbstractTask task = TasksUiPlugin.getTaskListManager().getTaskList().getTask(repositoryUrl, taskId);
				if (url != null) {
					return url + "?page=history";
				}
			}
		}

		return super.getHistoryUrl();
	}

}
