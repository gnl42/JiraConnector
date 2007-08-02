/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.editor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.mylyn.tasks.ui.AbstractDuplicateDetector;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.ui.editors.AbstractNewRepositoryTaskEditor;
import org.eclipse.mylyn.tasks.ui.search.SearchHitCollector;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * @author Steffen Pingel
 */
public class NewJiraTaskEditor extends AbstractNewRepositoryTaskEditor {

	public NewJiraTaskEditor(FormEditor editor) {
		super(editor);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);

		setExpandAttributeSection(true);
	}
	
	/**
	 * This method is duplicated in JiraTaskEditor for now.
	 */
	@Override
	public SearchHitCollector getDuplicateSearchCollector(String name) {
		String duplicateDetectorName = "default".equals(name) ? "Stack Trace" : name;
		Set<AbstractDuplicateDetector> detectors = getDuplicateSearchCollectorsList();

		for (AbstractDuplicateDetector detector : detectors) {
			if (duplicateDetectorName.equals(detector.getName())) {
				return detector.getSearchHitCollector(repository, taskData);
			}
		}
		return null;
	}

	/**
	 * This method is duplicated in JiraTaskEditor for now.
	 */
	@Override
	protected Set<AbstractDuplicateDetector> getDuplicateSearchCollectorsList() {
		Set<AbstractDuplicateDetector> detectors = new HashSet<AbstractDuplicateDetector>();
		for (AbstractDuplicateDetector detector : TasksUiPlugin.getDefault().getDuplicateSearchCollectorsList()) {
			if (detector.getKind() == null || detector.getKind().equals(getConnector().getConnectorKind())) {
				detectors.add(detector);
			}
		}
		return detectors;
	}
	
}
