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

import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooImages;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooUiPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorBusyIndicator;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.IBusyEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;

/**
 * Bamboo Build Rich Editor
 * 
 * @author Thomas Ehrnhoefer
 */
public class BambooEditor extends SharedHeaderFormEditor {

	public static final String ID = "com.atlassian.connector.eclipse.bamboo.ui.editors.build"; //$NON-NLS-1$

	private Composite editorParent;

	private EditorBusyIndicator editorBusyIndicator;

	private MenuManager menuManager;

	@Override
	protected Composite createPageContainer(Composite parent) {
		this.editorParent = parent;
		return super.createPageContainer(parent);
	}

	Composite getEditorParent() {
		return editorParent;
	}

	private void initialize() {
		editorBusyIndicator = new EditorBusyIndicator(new IBusyEditor() {
			public Image getTitleImage() {
				return BambooEditor.this.getTitleImage();
			}

			public void setTitleImage(Image image) {
				BambooEditor.this.setTitleImage(image);
			}
		});

		menuManager = new MenuManager();
		Menu menu = menuManager.createContextMenu(getContainer());
		getContainer().setMenu(menu);
		getEditorSite().registerContextMenu(menuManager, getEditorSite().getSelectionProvider(), false);

		// install context menu on form heading and title
		getHeaderForm().getForm().setMenu(menu);
		Composite head = getHeaderForm().getForm().getForm().getHead();
		if (head != null) {
			EditorUtil.setMenu(head, menu);
		}
	}

	@Override
	protected void addPages() {
		initialize();

		try {
			IFormPage page = new BambooBuildEditorPage(this, "Bamboo");
			int index = addPage(page);
			setPageImage(index, CommonImages.getImage(BambooImages.BAMBOO));
			setPageText(index, "Bamboo");
		} catch (PartInitException e) {
			StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
					"Could not create Bamboo Build editor.", e));
		}

		//for header toolbar: see TaskEditor.updateHeaderToolBar()
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// ignore

	}

	@Override
	public void doSaveAs() {
		// ignore

	}

	@Override
	public boolean isSaveAsAllowed() {
		// ignore
		return false;
	}

}
