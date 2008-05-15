/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.editor;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.mylyn.internal.tasks.core.deprecated.RepositoryTaskAttribute;
import org.eclipse.mylyn.internal.tasks.ui.deprecated.AbstractNewRepositoryTaskEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Steffen Pingel
 */
@SuppressWarnings( { "restriction", "deprecation" })
@Deprecated
public class NewJiraTaskEditor extends AbstractNewRepositoryTaskEditor {

	public NewJiraTaskEditor(FormEditor editor) {
		super(editor);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);

		setExpandAttributeSection(true);
	}

	@Override
	protected void createPeopleLayout(Composite composite) {
		FormToolkit toolkit = getManagedForm().getToolkit();
		Section peopleSection = createSection(composite, getSectionLabel(SECTION_NAME.PEOPLE_SECTION));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(peopleSection);
		Composite peopleComposite = toolkit.createComposite(peopleSection);
		GridLayout layout = new GridLayout(2, false);
		layout.marginRight = 5;
		peopleComposite.setLayout(layout);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(peopleComposite);

		Label label = toolkit.createLabel(peopleComposite, "Assign to:");
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
		Composite textFieldComposite = toolkit.createComposite(peopleComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(textFieldComposite);
		GridLayout textLayout = new GridLayout();
		textFieldComposite.setLayout(textLayout);

		RepositoryTaskAttribute attribute = taskData.getAttribute(RepositoryTaskAttribute.USER_ASSIGNED);

		Text textField = createTextField(textFieldComposite, attribute, SWT.FLAT);
		toolkit.paintBordersFor(textFieldComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(textField);
		peopleSection.setClient(peopleComposite);

		ContentAssistCommandAdapter adapter = applyContentAssist(textField, createContentProposalProvider(attribute));

		ILabelProvider propsalLabelProvider = createProposalLabelProvider(attribute);
		if (propsalLabelProvider != null) {
			adapter.setLabelProvider(propsalLabelProvider);
		}
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

		toolkit.paintBordersFor(peopleComposite);
	}

}
