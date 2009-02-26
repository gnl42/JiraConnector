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

import com.atlassian.connector.eclipse.internal.crucible.core.VersionedCommentDateComparator;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.AddGeneralCommentToFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.actions.CompareVersionedVirtualFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.actions.OpenVersionedVirtualFileAction;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A UI part to represent a CrucibleFileInfo and its associated comments
 * 
 * @author Shawn Minto
 */
public class CrucibleFilePart extends ExpandablePart {

	private final CrucibleFileInfo crucibleFile;

	private final Review review;

	public CrucibleFilePart(CrucibleFileInfo file, Review review, CrucibleReviewEditorPage editor) {
		super(editor);
		this.crucibleFile = file;
		this.review = review;
	}

	@Override
	public boolean canExpand() {
		return crucibleFile.getVersionedComments().size() != 0;
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
		Collections.sort(versionedComments, new VersionedCommentDateComparator());

		for (VersionedComment comment : versionedComments) {
			CommentPart fileComposite = new VersionedCommentPart(comment, review, crucibleFile, crucibleEditor);
			addChildPart(fileComposite);
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
		return null;
	}

	@Override
	protected void createCustomAnnotations(Composite toolbarComposite, FormToolkit toolkit) {

		ImageHyperlink textHyperlink = toolkit.createImageHyperlink(toolbarComposite, SWT.NONE);
		textHyperlink.setText("[Rev:");
		textHyperlink.setEnabled(false);
		textHyperlink.setUnderlined(false);

		VersionedVirtualFile oldFileDescriptor = crucibleFile.getOldFileDescriptor();
		VersionedVirtualFile newFileDescriptor = crucibleFile.getFileDescriptor();
		if (oldFileDescriptor != null && oldFileDescriptor.getUrl() != null && oldFileDescriptor.getUrl().length() > 0
				&& oldFileDescriptor.getRevision() != null && oldFileDescriptor.getRevision().length() > 0) {
			OpenVersionedVirtualFileAction openOldAction = new OpenVersionedVirtualFileAction(
					getCrucibleEditor().getTask(), new CrucibleFile(crucibleFile, true), review);
			openOldAction.setText(oldFileDescriptor.getRevision());
			openOldAction.setToolTipText("Open Revision " + oldFileDescriptor.getRevision());
			createActionHyperlink(toolbarComposite, toolkit, openOldAction);
		}

		if (crucibleFile.getOldFileDescriptor() != null && crucibleFile.getOldFileDescriptor().getRevision() != null
				&& crucibleFile.getOldFileDescriptor().getRevision().length() > 0) {

			if (crucibleFile.getFileDescriptor() != null && crucibleFile.getFileDescriptor().getRevision() != null
					&& crucibleFile.getFileDescriptor().getRevision().length() > 0) {
				textHyperlink = toolkit.createImageHyperlink(toolbarComposite, SWT.NONE);
				textHyperlink.setText("-");
				textHyperlink.setEnabled(false);
				textHyperlink.setUnderlined(false);
			}
		}

		if (newFileDescriptor != null && newFileDescriptor.getUrl() != null && newFileDescriptor.getUrl().length() > 0
				&& newFileDescriptor.getRevision() != null && newFileDescriptor.getRevision().length() > 0) {
			OpenVersionedVirtualFileAction openNewAction = new OpenVersionedVirtualFileAction(
					getCrucibleEditor().getTask(), new CrucibleFile(crucibleFile, false), review);
			openNewAction.setText(newFileDescriptor.getRevision());
			openNewAction.setToolTipText("Open Revision " + newFileDescriptor.getRevision());
			createActionHyperlink(toolbarComposite, toolkit, openNewAction);
		}

		textHyperlink = toolkit.createImageHyperlink(toolbarComposite, SWT.NONE);
		textHyperlink.setText("]");
		textHyperlink.setEnabled(false);
		textHyperlink.setUnderlined(false);

		boolean hasNewFile = checkHasFile(newFileDescriptor);
		boolean hasOldFile = checkHasFile(oldFileDescriptor);
		if (getCrucibleEditor() != null) {

			if (hasNewFile) {
				if (hasOldFile) {

					textHyperlink = toolkit.createImageHyperlink(toolbarComposite, SWT.NONE);
					textHyperlink.setText(" ");
					textHyperlink.setEnabled(false);
					textHyperlink.setUnderlined(false);

					CompareVersionedVirtualFileAction compareAction = new CompareVersionedVirtualFileAction(
							crucibleFile, review);
					compareAction.setToolTipText("Open Compare " + newFileDescriptor.getRevision() + " - "
							+ oldFileDescriptor.getRevision());
					compareAction.setText("Compare");
					// TODO set the image descriptor
					createActionHyperlink(toolbarComposite, toolkit, compareAction);
				}
			}
		}

	}

	@Override
	protected ImageDescriptor getAnnotationImage() {
		return null;
	}

	@Override
	protected List<IReviewAction> getToolbarActions(boolean isExpanded) {
		List<IReviewAction> actions = new ArrayList<IReviewAction>();
		AddGeneralCommentToFileAction addFileCommentAction = new AddGeneralCommentToFileAction(new CrucibleFile(
				crucibleFile, false), review);
		addFileCommentAction.setImageDescriptor(CrucibleImages.ADD_COMMENT);
		actions.add(addFileCommentAction);
		return actions;
	}

	private boolean checkHasFile(VersionedVirtualFile fileDescriptor) {
		if (fileDescriptor != null && fileDescriptor.getUrl() != null && fileDescriptor.getUrl().length() > 0
				&& fileDescriptor.getRevision() != null && fileDescriptor.getRevision().length() > 0) {
			return true;
		}
		return false;
	}

	public boolean isCrucibleFile(CrucibleFileInfo crucibleFileToReveal) {
		return crucibleFileToReveal.getPermId().equals(crucibleFile.getPermId());
	}

	public void selectAndReveal(VersionedComment commentToReveal) {
		if (!getSection().isExpanded()) {
			EditorUtil.toggleExpandableComposite(true, getSection());
		}

		for (ExpandablePart part : getChildrenParts()) {
			if (part instanceof VersionedCommentPart) {
				if (((VersionedCommentPart) part).isComment(commentToReveal)) {
					part.setExpanded(true);
					EditorUtil.ensureVisible(part.getSection());

					crucibleEditor.setHighlightedPart(part);

					return;
				}
			}
		}
	}

}
