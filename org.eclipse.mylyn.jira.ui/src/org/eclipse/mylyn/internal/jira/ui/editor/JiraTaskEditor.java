/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui.editor;

import org.eclipse.mylar.internal.tasks.ui.editors.AbstractRepositoryTaskEditor;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * @author Mik Kersten
 */
public class JiraTaskEditor extends AbstractRepositoryTaskEditor {

	public JiraTaskEditor(FormEditor editor) {
		super(editor);
	}
	
	@Override
	protected void addAttachContextButton(Composite buttonComposite, ITask task) {
		// disabled
	}

	@Override
	protected void addSelfToCC(Composite composite) {
		// disabled
	}

	@Override
	protected void createCustomAttributeLayout(Composite composite) {
		// ignore
	}

	@Override
	protected void submitToRepository() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void validateInput() {
		// TODO Auto-generated method stub

	}

}
