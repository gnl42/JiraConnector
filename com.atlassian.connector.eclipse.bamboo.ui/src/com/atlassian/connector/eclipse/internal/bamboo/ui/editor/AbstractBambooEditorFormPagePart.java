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

import com.atlassian.connector.eclipse.ui.editor.AbstractFormPagePart;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.jface.action.ToolBarManager;

/**
 * @author thomas
 */
public abstract class AbstractBambooEditorFormPagePart extends AbstractFormPagePart {

	public abstract void initialize(BambooBuildEditorPage editor, BambooBuild build);

	public abstract BambooBuildEditorPage getBuildEditor();

	protected void fillToolBar(ToolBarManager toolBarManager) {
	}
}
