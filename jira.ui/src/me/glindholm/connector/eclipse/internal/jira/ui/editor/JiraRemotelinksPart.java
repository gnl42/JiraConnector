/*******************************************************************************
 * Copyright (c) 2004, 2014 Tasktop Technologies and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.ui.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import me.glindholm.connector.eclipse.internal.jira.core.JiraAttribute;

/**
 * Copied from BugzillaPeoplePart
 *
 * @author Rob Elves
 * @author George Lindholm
 */
public class JiraRemotelinksPart extends AbstractTaskEditorPart {

    private static final int COLUMN_MARGIN = 5;

    public JiraRemotelinksPart() {
        setPartName(Messages.Remotelinks);
    }

    protected List<TaskAttribute> getAttributes() {
        final TaskAttribute root = getTaskData().getRoot();
        final Map<String, TaskAttribute> allAttributes = root.getAttributes();
        final List<TaskAttribute> attributes = new ArrayList<>(allAttributes.size());

        final TaskAttribute remotelinks = getTaskData().getRoot().getMappedAttribute(JiraAttribute.REMOTELINKS.id());
        if (remotelinks != null && remotelinks.getAttributes() != null) {
            for (final TaskAttribute link : remotelinks.getAttributes().values()) {
                attributes.add(link);
            }
        }
        return attributes;
    }

    protected GridDataFactory createLayoutData(final AbstractAttributeEditor editor) {
        final LayoutHint layoutHint = editor.getLayoutHint();
        final GridDataFactory gridDataFactory = GridDataFactory.fillDefaults().indent(3, 0);// prevent clipping of decorators on Mac
        if (layoutHint != null && layoutHint.rowSpan == RowSpan.MULTIPLE) {
            gridDataFactory.grab(true, true).align(SWT.FILL, SWT.FILL).hint(130, 95);
        } else {
            gridDataFactory.grab(true, false).align(SWT.FILL, SWT.TOP);

        }
        return gridDataFactory;
    }

    protected void addAttribute(final Composite composite, final FormToolkit toolkit, final TaskAttribute attribute) {
        final AbstractAttributeEditor editor = createAttributeEditor(attribute);
        if (editor != null) {
            editor.createLabelControl(composite, toolkit);
            GridDataFactory.defaultsFor(editor.getLabelControl()).indent(COLUMN_MARGIN, 0).applyTo(editor.getLabelControl());
            editor.createControl(composite, toolkit);
            getTaskEditorPage().getAttributeEditorToolkit().adapt(editor);

            final GridDataFactory dataFactory = createLayoutData(editor);
            dataFactory.applyTo(editor.getControl());
        }
    }

    @Override
    public void createControl(final Composite parent, final FormToolkit toolkit) {
        final Section section = createSection(parent, toolkit, false);
        final Composite remotelinksComposite = toolkit.createComposite(section);
        final GridLayout layout = EditorUtil.createSectionClientLayout();
        layout.numColumns = 2;
        remotelinksComposite.setLayout(layout);

        final int links = createAttributeEditors(toolkit, remotelinksComposite);
        section.setText(Messages.Remotelinks + " (" + links + ")");

        toolkit.paintBordersFor(remotelinksComposite);
        section.setClient(remotelinksComposite);
        setSection(toolkit, section);
    }

    protected int createAttributeEditors(final FormToolkit toolkit, final Composite remotelinkComposite) {
        final List<TaskAttribute> attributes = getAttributes();
        for (final TaskAttribute attribute : attributes) {
            addAttribute(remotelinkComposite, toolkit, attribute);
        }
        return attributes.size();
    }

}
