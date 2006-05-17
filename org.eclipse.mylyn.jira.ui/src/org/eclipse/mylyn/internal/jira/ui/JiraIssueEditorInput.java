/*******************************************************************************
 * Copyright (c) 2005 Jira Dashboard project.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylar.internal.jira.ui;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylar.internal.jira.JiraTask;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class JiraIssueEditorInput implements IEditorInput {

	private final JiraTask task;

	public JiraIssueEditorInput(JiraTask task) {
		this.task = task;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName() {
		return task.getHandleIdentifier();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return task.getDescription();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof JiraIssueEditorInput)) {
			return false;
		}
		
		JiraIssueEditorInput that = (JiraIssueEditorInput) obj;
		return (this.task.getHandleIdentifier().equals(that.task.getHandleIdentifier()) &&
				this.task.getRepositoryUrl().equals(that.task.getRepositoryUrl()));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.task.getHandleIdentifier().hashCode() + this.task.getRepositoryUrl().hashCode();
	}
	
	/**
	 * @return the task
	 */
	public JiraTask getTask() {
		return this.task;
	}

}
