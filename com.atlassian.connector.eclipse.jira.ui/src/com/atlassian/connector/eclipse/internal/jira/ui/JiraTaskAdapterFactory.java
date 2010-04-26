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

package com.atlassian.connector.eclipse.internal.jira.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.mylyn.tasks.core.ITask;

import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;

/**
 * @author Jacek Jaroczynski
 */
@SuppressWarnings("restriction")
public class JiraTaskAdapterFactory implements IAdapterFactory {

	private static final class JiraTask implements IJiraTask {
		private final ITask task;

		public JiraTask(ITask task) {
			this.task = task;
		}

		public ITask getTask() {
			return task;
		}

		public Object getAdapter(Class adapter) {
			if (!ITask.class.equals(adapter)) {
				return null;
			}
			return task;
		}
	}

	public Object getAdapter(Object adaptableObject, Class adapterType) {

		if (!adapterType.equals(IJiraTask.class)) {
			return null;
		}

		if (adaptableObject instanceof ITask) {
			ITask task = (ITask) adaptableObject;
			if (task.getConnectorKind().equals(JiraCorePlugin.CONNECTOR_KIND)) {
				return new JiraTask((ITask) adaptableObject);
			}
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { IJiraTask.class };
	}
}
