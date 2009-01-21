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

package com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts;

import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.actions.OpenVersionedVirtualFileAction;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A UI part to represent a CrucibleFileInfo and its associated comments
 * 
 * @author Shawn Minto
 */
public class CrucibleFilePart extends ExpandablePart {

	private final CrucibleFileInfo crucibleFile;

	public CrucibleFilePart(CrucibleFileInfo file, CrucibleReviewEditorPage editor) {
		super(editor);
		this.crucibleFile = file;
	}

	@Override
	protected Composite createSectionContents(Section section, FormToolkit toolkit) {
		//CHECKSTYLE:MAGIC:OFF
		Composite composite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout(1, false);
		layout.marginLeft = 15;
		composite.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);

		List<VersionedComment> versionedComments = new ArrayList<VersionedComment>(crucibleFile.getVersionedComments());
		Collections.sort(versionedComments, new Comparator<VersionedComment>() {

			public int compare(VersionedComment o1, VersionedComment o2) {
				if (o1 != null && o2 != null) {
					Integer start1 = o1.getToStartLine();
					Integer start2 = o2.getToStartLine();
					//TODO add second level sorting by date
					return start1.compareTo(start2);
				}
				return 0;
			}

		});

		for (VersionedComment comment : versionedComments) {
			CommentPart fileComposite = new VersionedCommentPart(comment, crucibleEditor);
			Control fileControl = fileComposite.createControl(composite, toolkit);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(fileControl);
		}
		//CHECKSTYLE:MAGIC:ON
		return composite;
	}

	@Override
	protected String getSectionHeaderText() {
		return crucibleFile.getFileDescriptor().getUrl();
	}

	@Override
	protected String getAnnotationText() {
		String revisionString = "[Rev:";
		if (crucibleFile.getOldFileDescriptor() != null && crucibleFile.getOldFileDescriptor().getRevision() != null
				&& crucibleFile.getOldFileDescriptor().getRevision().length() > 0) {
			revisionString += crucibleFile.getOldFileDescriptor().getRevision();

			if (crucibleFile.getFileDescriptor() != null && crucibleFile.getFileDescriptor().getRevision() != null
					&& crucibleFile.getFileDescriptor().getRevision().length() > 0) {
				revisionString += "-";
			}
		}
		if (crucibleFile.getFileDescriptor() != null && crucibleFile.getFileDescriptor().getRevision() != null
				&& crucibleFile.getFileDescriptor().getRevision().length() > 0) {
			revisionString += crucibleFile.getFileDescriptor().getRevision() + "]";
		}
		return revisionString;
	}

	@Override
	protected ImageDescriptor getAnnotationImage() {
		return null;
	}

	@Override
	protected List<IAction> getToolbarActions(boolean isExpanded) {
		List<IAction> actions = new ArrayList<IAction>();
		VersionedVirtualFile oldFileDescriptor = crucibleFile.getOldFileDescriptor();
		VersionedVirtualFile newFileDescriptor = crucibleFile.getFileDescriptor();
		if (oldFileDescriptor != null && oldFileDescriptor.getUrl() != null && oldFileDescriptor.getUrl().length() > 0
				&& oldFileDescriptor.getRevision() != null && oldFileDescriptor.getRevision().length() > 0) {
			OpenVersionedVirtualFileAction openOldAction = new OpenVersionedVirtualFileAction(oldFileDescriptor);
			openOldAction.setText("Open Old");
			openOldAction.setToolTipText("Open Revision " + oldFileDescriptor.getRevision());
			// TODO set the image descriptor
			actions.add(openOldAction);
		}

		if (newFileDescriptor != null && newFileDescriptor.getUrl() != null && newFileDescriptor.getUrl().length() > 0
				&& newFileDescriptor.getRevision() != null && newFileDescriptor.getRevision().length() > 0) {
			OpenVersionedVirtualFileAction openNewAction = new OpenVersionedVirtualFileAction(newFileDescriptor);
			openNewAction.setText("Open New");
			openNewAction.setToolTipText("Open Revision " + newFileDescriptor.getRevision());
			// TODO set the image descriptor
			actions.add(openNewAction);
		}
		return actions;
	}
}
