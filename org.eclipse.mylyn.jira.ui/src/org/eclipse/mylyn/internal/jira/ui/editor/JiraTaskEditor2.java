/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.editor;

import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.internal.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.internal.tasks.ui.editors.AttributeEditorToolkit;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * NOTE: This class is work in progress and currently not used.
 * 
 * @author Steffen Pingel
 */
@SuppressWarnings("restriction")
public class JiraTaskEditor2 extends AbstractTaskEditorPage {

	private AttributeEditorFactory attributeEditorFactory;

	private AttributeEditorToolkit attributeEditorToolkit;

	public JiraTaskEditor2(TaskEditor editor) {
		super(editor, JiraCorePlugin.CONNECTOR_KIND);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		attributeEditorFactory = new AttributeEditorFactory(getAttributeManager(), getTaskRepository());
		IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
		attributeEditorToolkit = new AttributeEditorToolkit(handlerService, getEditorSite().getActionBarContributor());

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

}
