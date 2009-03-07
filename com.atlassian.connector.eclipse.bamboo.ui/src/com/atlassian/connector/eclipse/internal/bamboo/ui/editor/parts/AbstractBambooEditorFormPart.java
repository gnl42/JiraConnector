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

package com.atlassian.connector.eclipse.internal.bamboo.ui.editor.parts;

import com.atlassian.connector.eclipse.internal.bamboo.ui.editor.BambooBuildEditorPage;
import com.atlassian.connector.eclipse.ui.editor.AbstractFormPagePart;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.widgets.Control;

/**
 * A form part that needs to be aware of the build that it is displaying
 * 
 * @author Thomas Ehrnhoefer
 */
public abstract class AbstractBambooEditorFormPart extends AbstractFormPagePart {

	protected BambooBuild bambooBuild;

	protected TaskRepository repository;

	protected Control control;

	public AbstractBambooEditorFormPart(BambooBuild bambooBuild, TaskRepository repository) {
		this.bambooBuild = bambooBuild;
		this.repository = repository;
	}

	public abstract void initialize(BambooBuildEditorPage editor, BambooBuild bambooBuild);

	public abstract BambooBuildEditorPage getBuildEditor();

}
