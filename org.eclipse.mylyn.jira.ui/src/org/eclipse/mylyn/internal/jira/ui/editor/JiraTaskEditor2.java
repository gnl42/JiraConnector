/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.editor;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.mylyn.internal.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.internal.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.internal.tasks.ui.editors.AttributeEditorToolkit;
import org.eclipse.mylyn.internal.tasks.ui.editors.AttributeManager;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * NOTE: This class is work in progress and currently not used.
 * 
 * @author Steffen Pingel
 */
@SuppressWarnings("restriction")
public class JiraTaskEditor2 extends AbstractTaskEditorPage {

	private AttributeEditorFactory attributeEditorFactory;

	private AttributeEditorToolkit attributeEditorToolkit;

	public JiraTaskEditor2(FormEditor editor) {
		super(editor);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		attributeEditorFactory = new AttributeEditorFactory(getAttributeManager());
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

			@Override
			public void adapt(AbstractAttributeEditor editor) {
				super.adapt(editor);
				
				if (getAttributeManager().hasIncomingChanges(editor.getTaskAttribute())) {
					editor.decorate(getColorIncoming());
				}
			}
			
			@Override
			public void configureContextMenuManager(MenuManager menuManager, TextViewer textViewer) {
				getParentEditor().configureContextMenuManager(menuManager, textViewer);
			}

		};

		super.createFormContent(managedForm);
	}

	@Override
	protected AttributeEditorFactory getAttributeEditorFactory() {
		return attributeEditorFactory;
	}

	@Override
	public AttributeEditorToolkit getAttributeEditorToolkit() {
		return attributeEditorToolkit;
	}

	@Override
	protected String getHistoryUrl() {
		RepositoryTaskData taskData = getAttributeManager().getTaskData();
			String taskKey = taskData.getTaskKey();
			String repositoryUrl = taskData.getRepositoryUrl();
			if (getConnector() != null && repositoryUrl != null && taskKey != null) {
				String url = getConnector().getTaskUrl(repositoryUrl, taskKey);
				if (url != null) {
					return url + "?page=history";
				}
			}

		return super.getHistoryUrl();
	}

	@Override
	protected AttributeManager createAttributeManager(TaskRepository taskRepository, String storageUrl, String storageId) {
		return new AttributeManager(taskRepository, storageUrl, storageId);
	}

}
