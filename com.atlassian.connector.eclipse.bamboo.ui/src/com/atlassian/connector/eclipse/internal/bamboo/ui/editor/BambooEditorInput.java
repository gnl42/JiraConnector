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

import com.atlassian.connector.eclipse.internal.bamboo.ui.EclipseBambooBuild;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * EditorInput for Bamboo Build
 * 
 * @author Thomas Ehrnhoefer
 */
public class BambooEditorInput implements IEditorInput {

	private final EclipseBambooBuild bambooBuild;

	private static final int MAX_LABEL_LENGTH = 60;

	public BambooEditorInput(EclipseBambooBuild bambooBuild) {
		Assert.isNotNull(bambooBuild.getTaskRepository());
		Assert.isNotNull(bambooBuild);
		this.bambooBuild = bambooBuild;
	}

	public boolean exists() {
		return bambooBuild != null;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		if (bambooBuild != null) {
			String number = "";
			try {
				number = String.valueOf(bambooBuild.getBuild().getNumber());
			} catch (UnsupportedOperationException e) {
				//ignore
			}
			return truncate(bambooBuild.getBuild().getPlanName() + "-" + number);
		}
		return truncate("Bamboo Build");
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		if (bambooBuild != null) {
			String number = "";
			try {
				number = String.valueOf(bambooBuild.getBuild().getNumber());
			} catch (UnsupportedOperationException e) {
				//ignore
			}
			return truncate(bambooBuild.getBuild().getPlanKey() + "-" + number);
		}
		return truncate("Bamboo Build");
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter == IEditorInput.class) {
			return this;
		}
		return null;
	}

	public EclipseBambooBuild getBambooBuild() {
		return bambooBuild;
	}

	public TaskRepository getRepository() {
		return bambooBuild.getTaskRepository();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getRepository().hashCode();
		result = prime * result + bambooBuild.getBuild().getPlanKey().hashCode();
		result = prime * result + bambooBuild.getBuild().getNumber();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		//if same repository, build plan and build number, input is equals
		if (getRepository().equals(((BambooEditorInput) obj).getRepository())) {
			if (this.bambooBuild.getBuild().getPlanKey().equals(
					((BambooEditorInput) obj).getBambooBuild().getBuild().getPlanKey())) {
				return this.bambooBuild.getBuild().getNumber() == ((BambooEditorInput) obj).getBambooBuild()
						.getBuild()
						.getNumber();
			}
		}

		return super.equals(obj);
	}

	private String truncate(String description) {
		if (description == null || description.length() <= MAX_LABEL_LENGTH) {
			return description;
		} else {
			return description.substring(0, MAX_LABEL_LENGTH) + "...";
		}
	}
}
