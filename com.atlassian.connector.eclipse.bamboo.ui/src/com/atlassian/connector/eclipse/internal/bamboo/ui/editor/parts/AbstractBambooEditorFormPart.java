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

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * A form part that needs to be aware of the build that it is displaying
 * 
 * @author Thomas Ehrnhoefer
 */
public abstract class AbstractBambooEditorFormPart extends AbstractFormPagePart {

	protected BambooBuild bambooBuild;

	protected TaskRepository repository;

	protected Control control;

	private BambooBuildEditorPage editor;

	private String partName = "";

	public AbstractBambooEditorFormPart() {
		this("");
	}

	public AbstractBambooEditorFormPart(String partName) {
		this.partName = partName;
	}

	public void initialize(BambooBuildEditorPage editor, BambooBuild bambooBuild, TaskRepository repository) {
		this.bambooBuild = bambooBuild;
		this.repository = repository;
		this.editor = editor;
	}

	public BambooBuildEditorPage getBuildEditor() {
		return editor;
	}

	protected Text createReadOnlyText(FormToolkit toolkit, Composite composite, String value, String labelString,
			boolean isMultiline) {

		if (labelString != null) {
			createLabelControl(toolkit, composite, labelString);
		}
		int style = SWT.FLAT | SWT.READ_ONLY;
		if (isMultiline) {
			style |= SWT.MULTI | SWT.WRAP;
		}
		Text text = new Text(composite, style);
		text.setFont(EditorUtil.TEXT_FONT);
		text.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
		text.setText(value);
		toolkit.adapt(text, true, true);

		return text;
	}

	private Label createLabelControl(FormToolkit toolkit, Composite composite, String labelString) {
		Label labelControl = toolkit.createLabel(composite, labelString);
		labelControl.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		return labelControl;
	}

	protected Label createLabelControl(FormToolkit toolkit, Composite composite, Image image) {
		Label labelControl = toolkit.createLabel(composite, null);
		labelControl.setImage(image);
		return labelControl;
	}

	protected Section createSection(Composite parent, FormToolkit toolkit, int style) {
		Section section = toolkit.createSection(parent, style);
		section.setText(partName);
		return section;
	}

	public void setPartName(String partName) {
		this.partName = partName;
	}

	protected void setSection(FormToolkit toolkit, Section section) {
		if (section.getTextClient() == null) {
			ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
			fillToolBar(toolBarManager);

			if (toolBarManager.getSize() > 0) {
				Composite toolbarComposite = toolkit.createComposite(section);
				toolbarComposite.setBackground(null);
				RowLayout rowLayout = new RowLayout();
				rowLayout.marginTop = 0;
				rowLayout.marginBottom = 0;
				toolbarComposite.setLayout(rowLayout);

				toolBarManager.createControl(toolbarComposite);
				section.setTextClient(toolbarComposite);
			}
		}
		control = section;
	}

	protected void fillToolBar(ToolBarManager toolBarManager) {
	}

}
