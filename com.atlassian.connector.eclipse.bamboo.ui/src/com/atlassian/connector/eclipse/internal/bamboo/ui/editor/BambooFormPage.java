/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.bamboo.ui.editor;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;

/**
 * @author Thomas Ehrnhoefer
 */
public abstract class BambooFormPage extends FormPage {

	public BambooFormPage(String id, String title) {
		super(id, title);
	}

	public BambooFormPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	protected void fillToolBar(IToolBarManager toolBarManager) {
	}
}
