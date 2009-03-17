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
import com.atlassian.theplugin.commons.bamboo.BuildDetails;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;

/**
 * A form part that needs to be aware of the build that it is displaying
 * 
 * @author Thomas Ehrnhoefer
 */
public abstract class AbstractBambooEditorFormPart extends AbstractFormPagePart {

	protected static final int DEFAULT_HEIGHT = 125;

	protected static final int SECTION_FULL_WIDTH = 600;

	protected static final int SECTION_HALF_WIDTH = 300;

	protected static final RGB SUCCESS_BACKGROUND_MAIN = new RGB(51, 153, 51);

	protected static final RGB SUCCESS_BACKGROUND_SEPARATOR = new RGB(46, 137, 46);

	protected static final RGB SUCCESS_BACKGROUND_TITLE = new RGB(81, 168, 81);

	protected static final RGB FAILED_BACKGROUND_MAIN = new RGB(204, 51, 51);

	protected static final RGB FAILED_BACKGROUND_SEPARATOR = new RGB(183, 46, 46);

	protected static final RGB FAILED_BACKGROUND_TITLE = new RGB(212, 81, 81);

	protected BambooBuild bambooBuild;

	protected TaskRepository repository;

	protected Control control;

	private BambooBuildEditorPage editor;

	private String partName = "";

	protected String buildLog;

	protected BuildDetails buildDetails;

	protected FormToolkit toolkit;

	protected Section section;

	protected Composite mainComposite;

	public AbstractBambooEditorFormPart() {
		this("");
	}

	public AbstractBambooEditorFormPart(String partName) {
		this.partName = partName;
	}

	public void initialize(BambooBuildEditorPage editor, BambooBuild bambooBuild, TaskRepository repository,
			String buildLog, BuildDetails buildDetails) {
		this.bambooBuild = bambooBuild;
		this.repository = repository;
		this.editor = editor;
		this.buildLog = buildLog;
		this.buildDetails = buildDetails;
	}

	public BambooBuildEditorPage getBuildEditor() {
		return editor;
	}

	protected Text createReadOnlyText(FormToolkit toolkit, Composite composite, String value, String labelString,
			boolean isMultiline, boolean border) {

		if (labelString != null) {
			Label label = createLabelControl(toolkit, composite, labelString);
			if (isMultiline) {
				GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.TOP).applyTo(label);
			}
		}
		int style = SWT.FLAT | SWT.READ_ONLY;
		if (isMultiline) {
			style |= SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL;
		}
		if (border) {
			style |= SWT.BORDER;
		}
		Text text = new Text(composite, style);
		text.setFont(EditorUtil.TEXT_FONT);
		text.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
		text.setText(value);
		toolkit.adapt(text, true, true);

		return text;
	}

	protected Text createReadOnlyText(FormToolkit toolkit, Composite composite, String value, String labelString,
			boolean isMultiline) {
		return createReadOnlyText(toolkit, composite, value, labelString, isMultiline, false);
	}

	protected Label createLabelControl(FormToolkit toolkit, Composite composite, String labelString) {
		Label labelControl = toolkit.createLabel(composite, labelString);
		labelControl.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		return labelControl;
	}

	protected Label createLabelControl(FormToolkit toolkit, Composite composite, Image image) {
		Label labelControl = toolkit.createLabel(composite, null);
		labelControl.setImage(image);
		return labelControl;
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

	public void buildInfoRetrievalDone(boolean success) {

	}

	public void setBuildDetails(BuildDetails buildDetails) {
		this.buildDetails = buildDetails;
	}

	public void setBuildLog(String buildLog) {
		this.buildLog = buildLog;
	}

	/**
	 * Creates a composite with three hyperlinks, the middleone being the actual link.
	 * 
	 * @param parent
	 * @param toolkit
	 * @param pre
	 * @param linkTxt
	 * @param post
	 * @param hyperlinkAdapter
	 * @return The actual hyperlink the adapter gets registered at
	 */
	protected Hyperlink createLinks(Composite parent, FormToolkit toolkit, String pre, String linkTxt, String post,
			HyperlinkAdapter hyperlinkAdapter) {
		Composite linksComposite = toolkit.createComposite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		layout.makeColumnsEqualWidth = false;
		linksComposite.setLayout(layout);

		Hyperlink link = toolkit.createImageHyperlink(linksComposite, SWT.NONE);
		link.setUnderlined(false);
		link.setEnabled(false);
		if (pre == null) {
			link.setVisible(false);
		} else {
			link.setText(pre);
			link.setVisible(true);
		}

		link = toolkit.createImageHyperlink(linksComposite, SWT.NONE);
		if (linkTxt == null) {
			link.setVisible(false);
		} else {
			link.setText(linkTxt);
			if (hyperlinkAdapter != null) {
				link.addHyperlinkListener(hyperlinkAdapter);
			}
			link.setVisible(true);
		}

		link = toolkit.createImageHyperlink(linksComposite, SWT.NONE);
		link.setUnderlined(false);
		link.setEnabled(false);
		Hyperlink postLink = toolkit.createImageHyperlink(linksComposite, SWT.NONE);
		postLink.setUnderlined(false);
		postLink.setEnabled(false);

		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(linksComposite);

		return link;
	}

	protected void reinitMainComposite() {
		for (Control child : mainComposite.getChildren()) {
			child.dispose();
		}
		mainComposite.setMenu(null);
	}

	/**
	 * Creates a section and main composite with 1 column
	 * 
	 * @param parent
	 * @param toolkit
	 * @param hColSpan
	 * @param style
	 */
	protected void createSectionAndComposite(Composite parent, FormToolkit toolkit, int hColSpan, int style) {
		section = toolkit.createSection(parent, style);
		section.setText(partName);
		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				getBuildEditor().reflow();
			}
		});
		GridDataFactory.fillDefaults()
				.grab(true, false)
				.span(hColSpan, 1)
				.hint(SECTION_FULL_WIDTH, -1)
				.applyTo(section);
		mainComposite = toolkit.createComposite(section, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		mainComposite.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(mainComposite);
	}
}
