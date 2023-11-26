/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.ui.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;

/**
 * @author Steffen Pingel
 */
public class JiraTaskEditorAttributePart extends JiraAbstractTaskEditorAttributeSection {

    public JiraTaskEditorAttributePart() {
        setPartName(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.TaskEditorAttributePart_Attributes);
        setNeedsRefresh(true);
    }

    @Override
    protected Collection<TaskAttribute> getAttributes() {
        final Map<String, TaskAttribute> allAttributes = getTaskData().getRoot().getAttributes();
        final List<TaskAttribute> attributes = new ArrayList<>(allAttributes.size());
        for (final TaskAttribute attribute : allAttributes.values()) {
            final TaskAttributeMetaData properties = attribute.getMetaData();
            if (TaskAttribute.KIND_DEFAULT.equals(properties.getKind())) {
                attributes.add(attribute);
            }
        }
        return attributes;
    }

    @Override
    protected List<TaskAttribute> getOverlayAttributes() {
        final TaskAttribute product = getModel().getTaskData().getRoot().getMappedAttribute(TaskAttribute.PRODUCT);
        final List<TaskAttribute> attributes = new ArrayList<>(2);
        if (product != null) {
            attributes.add(product);
        }
        final TaskAttribute component = getModel().getTaskData().getRoot().getMappedAttribute(TaskAttribute.COMPONENT);
        if (component != null) {
            attributes.add(component);
        }
        return attributes;
    }

}
