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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorPeoplePart;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;

import me.glindholm.connector.eclipse.internal.jira.core.JiraAttribute;

/**
 * Copied from BugzillaPeoplePart
 *
 * @author Rob Elves
 * @author George Lindholm
 */
public class JiraPeoplePart extends TaskEditorPeoplePart {

    private static final int COLUMN_MARGIN = 5;

    public JiraPeoplePart() {
    }

    @Override
    protected Collection<TaskAttribute> getAttributes() {
        final Map<String, TaskAttribute> allAttributes = getTaskData().getRoot().getAttributes();
        final List<TaskAttribute> attributes = new ArrayList<>(allAttributes.size());
        attributes.add(getTaskData().getRoot().getMappedAttribute(TaskAttribute.USER_ASSIGNED));

        final TaskAttribute reporter = getTaskData().getRoot().getMappedAttribute(TaskAttribute.USER_REPORTER);
        attributes.add(reporter);

        final TaskAttribute watchers = getTaskData().getRoot().getMappedAttribute(JiraAttribute.WATCHERS.id());
        attributes.add(watchers);
        return attributes;
    }

    @Override
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
}
