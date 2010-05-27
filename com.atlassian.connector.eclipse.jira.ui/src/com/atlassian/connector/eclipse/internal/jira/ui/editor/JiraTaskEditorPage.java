/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Atlassian - UI improvements, adding new features
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.ui.editor;

import com.atlassian.connector.eclipse.internal.jira.core.IJiraConstants;
import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.ui.actions.StartWorkEditorToolbarAction;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorAttributePart;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorDescriptionPart;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Steffen Pingel
 * @author Wojciech Seliga
 */
public class JiraTaskEditorPage extends AbstractTaskEditorPage {

	public JiraTaskEditorPage(TaskEditor editor) {
		super(editor, JiraCorePlugin.CONNECTOR_KIND);
		setNeedsPrivateSection(false);
		setNeedsSubmitButton(true);
	}

	@Override
	protected Set<TaskEditorPartDescriptor> createPartDescriptors() {
		Set<TaskEditorPartDescriptor> parts = super.createPartDescriptors();

		// replace summary part
		Iterator<TaskEditorPartDescriptor> iter = parts.iterator();
		while (iter.hasNext()) {
			TaskEditorPartDescriptor part = iter.next();
			if (part.getId().equals(ID_PART_SUMMARY)) {
				parts.remove(part);

				// add JIRA specific summary part (with votes number)
				parts.add(new TaskEditorPartDescriptor(ID_PART_SUMMARY) {
					@Override
					public AbstractTaskEditorPart createPart() {
						return new JiraTaskEditorSummaryPart();
					}
				}.setPath(part.getPath()));

				break;
			}
		}

		// remove comments part
		iter = parts.iterator();
		while (iter.hasNext()) {
			TaskEditorPartDescriptor part = iter.next();
			if (part.getId().equals(ID_PART_COMMENTS)) {
				parts.remove(part);

				// add JIRA specific comments part (with visibility restriction info for each comment)
				parts.add(new TaskEditorPartDescriptor(ID_PART_COMMENTS) {
					@Override
					public AbstractTaskEditorPart createPart() {
						return new JiraCommentPartCopy();
//						return new JiraCommentPart();
					}
				}.setPath(part.getPath()));

				break;
			}
		}

		// remove standard new comment part 
		iter = parts.iterator();
		while (iter.hasNext()) {
			TaskEditorPartDescriptor part = iter.next();
			if (part.getId().equals(ID_PART_NEW_COMMENT)) {
				parts.remove(part);

				// add JIRA specific comment part (with visibility restriction combo)
				parts.add(new TaskEditorPartDescriptor(ID_PART_NEW_COMMENT) {
					@Override
					public AbstractTaskEditorPart createPart() {
						return new JiraNewCommentPart(getModel());
					}
				}.setPath(part.getPath()));

				break;
			}
		}

		// remove planning part - it's now in a separate tab
		removePart(parts, ID_PART_PLANNING);

		removePart(parts, ID_PART_ATTRIBUTES);

		parts.add(new TaskEditorPartDescriptor(ID_PART_ATTRIBUTES) {
			@Override
			public AbstractTaskEditorPart createPart() {
				return new TaskEditorAttributePart() {
					@Override
					protected boolean shouldExpandOnCreate() {
						return true;
					}
				};
			}
		}.setPath(PATH_ATTRIBUTES));

		// move Description just below Attributes and expand it always
		removePart(parts, ID_PART_DESCRIPTION);
		parts.add(new TaskEditorPartDescriptor(ID_PART_DESCRIPTION) {
			@Override
			public AbstractTaskEditorPart createPart() {
				TaskEditorDescriptionPart part = new TaskEditorDescriptionPart();
				part.setExpandVertically(true);
				part.setSectionStyle(ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
				return part;
			}
		}.setPath(PATH_ATTRIBUTES));

		// and worklog at the very end
		if (getModel().getTaskData().getRoot().getAttribute(IJiraConstants.ATTRIBUTE_WORKLOG_NOT_SUPPORTED) == null) {
			parts.add(new TaskEditorPartDescriptor("com.atlassian.connnector.eclipse.jira.worklog") { //$NON-NLS-1$
				@Override
				public AbstractTaskEditorPart createPart() {
					return new WorkLogPart();
				}
			}.setPath(PATH_COMMENTS));
		}

		return parts;
	}

	private void removePart(Set<TaskEditorPartDescriptor> parts, String partId) {
		Iterator<TaskEditorPartDescriptor> iter;
		iter = parts.iterator();
		while (iter.hasNext()) {
			TaskEditorPartDescriptor part = iter.next();
			if (part.getId().equals(partId)) {
				parts.remove(part);
				break;
			}
		}
	}

	@Override
	protected AttributeEditorFactory createAttributeEditorFactory() {
		return new JiraAttributeEditorFactory(getModel(), getTaskRepository(), getEditorSite());
	}

	@Override
	public void fillToolBar(IToolBarManager toolBarManager) {
		super.fillToolBar(toolBarManager);

		if (!getModel().getTaskData().isNew()) {
			StartWorkEditorToolbarAction startWorkAction = new StartWorkEditorToolbarAction(this);
//			startWorkAction.selectionChanged(new StructuredSelection(getTaskEditor()));
			toolBarManager.appendToGroup("repository", startWorkAction); //$NON-NLS-1$
		}
	}

}
