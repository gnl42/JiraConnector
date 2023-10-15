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

package me.glindholm.connector.eclipse.internal.jira.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.mylyn.tasks.core.ITask;

import me.glindholm.connector.eclipse.internal.jira.core.JiraCorePlugin;

/**
 * @author Jacek Jaroczynski
 */
public class JiraTaskAdapterFactory implements IAdapterFactory {

    private static final class JiraTask implements IJiraTask {
        private final ITask task;

        public JiraTask(final ITask task) {
            this.task = task;
        }

        @Override
        public ITask getTask() {
            return task;
        }

        @Override
        public Object getAdapter(final Class adapter) {
            if (!ITask.class.equals(adapter)) {
                return null;
            }
            return task;
        }
    }

    @Override
    public Object getAdapter(final Object adaptableObject, final Class adapterType) {

        if (!adapterType.equals(IJiraTask.class)) {
            return null;
        }

        if (adaptableObject instanceof final ITask task) {
            if (JiraCorePlugin.CONNECTOR_KIND.equals(task.getConnectorKind())) {
                return new JiraTask((ITask) adaptableObject);
            }
        }
        return null;
    }

    @Override
    public Class[] getAdapterList() {
        return new Class[] { IJiraTask.class };
    }
}
