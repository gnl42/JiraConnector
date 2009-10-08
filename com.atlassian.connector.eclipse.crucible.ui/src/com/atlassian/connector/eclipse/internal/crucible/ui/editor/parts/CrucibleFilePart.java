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

import com.atlassian.connector.eclipse.internal.crucible.IReviewChangeListenerAction;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.VersionedCommentDateComparator;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.AddGeneralCommentToFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.CompareUploadedVirtualFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.CompareVersionedVirtualFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.OpenUploadedVirtualFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.OpenVersionedVirtualFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.FileType;
import com.atlassian.theplugin.commons.crucible.api.model.RepositoryType;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonFormUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A UI part to represent a CrucibleFileInfo and its associated comments
 * 
 * @author Shawn Minto
 */
public class CrucibleFilePart extends ExpandablePart<VersionedComment, VersionedCommentPart> {

	private CrucibleFileInfo crucibleFile;

	private Composite composite;

	private IReviewChangeListenerAction compareAction;

	public CrucibleFilePart(CrucibleFileInfo file, Review review, CrucibleReviewEditorPage editor) {
		super(editor, review);
		this.crucibleFile = file;
	}

	@Override
	public boolean canExpand() {
		return canExpand(crucibleFile);
	}

	private boolean canExpand(CrucibleFileInfo file) {
		if (file != null) {
			return file.getVersionedComments().size() != 0;
		}
		return false;

	}

	@Override
	protected Composite createSectionContents(Section section, FormToolkit toolkit) {
		//CHECKSTYLE:MAGIC:OFF
		composite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout(1, false);
		layout.marginLeft = 15;
		composite.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);

		updateChildren(composite, toolkit, false, crucibleFile.getVersionedComments());
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
		textHyperlink.setText("[");
		textHyperlink.setEnabled(false);
		textHyperlink.setUnderlined(false);

		VersionedVirtualFile oldFileDescriptor = crucibleFile.getOldFileDescriptor();
		VersionedVirtualFile newFileDescriptor = crucibleFile.getFileDescriptor();

		boolean oldFileHasRevision = oldFileDescriptor != null && oldFileDescriptor.getRevision() != null
				&& oldFileDescriptor.getRevision().length() > 0;
		boolean oldFileHasUrl = oldFileDescriptor != null && oldFileDescriptor.getUrl() != null
				&& oldFileDescriptor.getUrl().length() > 0;

		boolean newFileHasRevision = newFileDescriptor != null && newFileDescriptor.getRevision() != null
				&& newFileDescriptor.getRevision().length() > 0;
		boolean newFileHasUrl = newFileDescriptor != null && newFileDescriptor.getUrl() != null
				&& newFileDescriptor.getUrl().length() > 0;

		FileType filetype = crucibleFile.getFileType();

		//if repository type is uploaded or patch, display alternative for now since we cannot open the file yet
		if (crucibleFile.getRepositoryType() == RepositoryType.PATCH) {
			textHyperlink = toolkit.createImageHyperlink(toolbarComposite, SWT.NONE);
			textHyperlink.setText("Part of a Patch");
			textHyperlink.setEnabled(false);
			textHyperlink.setUnderlined(false);
			textHyperlink.setToolTipText("Opening of files from a patch not supported."
					+ " Please see studio.atlassian.com for updates.");
		} else if (crucibleFile.getRepositoryType() == RepositoryType.UPLOAD) {
			textHyperlink = toolkit.createImageHyperlink(toolbarComposite, SWT.NONE);
			textHyperlink.setText("Pre-commit");
			textHyperlink.setEnabled(false);
			textHyperlink.setUnderlined(false);
			textHyperlink.setToolTipText("Opening of uploaded files not supported."
					+ " Please see studio.atlassian.com for updates.");

			//TODO jj handle directories etc.
			if (crucibleFile.getCommitType() == CommitType.Deleted || filetype != FileType.File) {
				// TODO jj handle deleted files, check for [--item deleted--] string defined in AbstractTeamConnector to detect if the file was deleted and do not display 'compare' but only when editor with annotations is ready
				// it will be hard to do that here as we have no file content yet
			} else {
				if (oldFileHasUrl && oldFileHasRevision) {
					OpenUploadedVirtualFileAction openOldAction = new OpenUploadedVirtualFileAction(
							getCrucibleEditor().getTask(), new CrucibleFile(crucibleFile, true), oldFileDescriptor,
							crucibleReview, null, toolbarComposite.getShell(), getCrucibleEditor().getSite()
									.getWorkbenchWindow()
									.getActivePage());
					openOldAction.setText(oldFileDescriptor.getRevision());
					openOldAction.setToolTipText("Open Revision " + oldFileDescriptor.getRevision());
					createActionHyperlink(toolbarComposite, toolkit, openOldAction);
				}
				if (oldFileHasRevision) {
					if (newFileHasRevision) {
						textHyperlink = toolkit.createImageHyperlink(toolbarComposite, SWT.NONE);
						textHyperlink.setText("-");
						textHyperlink.setEnabled(false);
						textHyperlink.setUnderlined(false);
					}
				}

				if (newFileHasUrl && newFileHasRevision && crucibleFile.getCommitType() != CommitType.Deleted) {
					OpenUploadedVirtualFileAction openNewAction = new OpenUploadedVirtualFileAction(
							getCrucibleEditor().getTask(), new CrucibleFile(crucibleFile, false), newFileDescriptor,
							crucibleReview, null, toolbarComposite.getShell(), getCrucibleEditor().getSite()
									.getWorkbenchWindow()
									.getActivePage());
					openNewAction.setText(newFileDescriptor.getRevision());
					openNewAction.setToolTipText("Open Revision " + newFileDescriptor.getRevision());
					createActionHyperlink(toolbarComposite, toolkit, openNewAction);
				}
			}

		} else {
			//if file is deleted or not a file, do not include any revisions 
			//   (we need a local resource to retrieve the old revision from SVN, which we do not have)
			if (crucibleFile.getCommitType() == CommitType.Deleted || filetype != FileType.File) {
				textHyperlink = toolkit.createImageHyperlink(toolbarComposite, SWT.NONE);
				textHyperlink.setText("Rev: N/A ");
				textHyperlink.setEnabled(false);
				textHyperlink.setUnderlined(false);
			} else {
				if (oldFileHasUrl && oldFileHasRevision) {
					OpenVersionedVirtualFileAction openOldAction = new OpenVersionedVirtualFileAction(
							getCrucibleEditor().getTask(), new CrucibleFile(crucibleFile, true), crucibleReview);
					openOldAction.setText("Rev: " + oldFileDescriptor.getRevision());
					openOldAction.setToolTipText("Open Revision " + oldFileDescriptor.getRevision());
					createActionHyperlink(toolbarComposite, toolkit, openOldAction);
				}
				if (oldFileHasRevision) {
					if (newFileHasRevision) {
						textHyperlink = toolkit.createImageHyperlink(toolbarComposite, SWT.NONE);
						textHyperlink.setText("-");
						textHyperlink.setEnabled(false);
						textHyperlink.setUnderlined(false);
					}
				}

				if (newFileHasUrl && newFileHasRevision && crucibleFile.getCommitType() != CommitType.Deleted) {
					OpenVersionedVirtualFileAction openNewAction = new OpenVersionedVirtualFileAction(
							getCrucibleEditor().getTask(), new CrucibleFile(crucibleFile, false), crucibleReview);
					openNewAction.setText(newFileDescriptor.getRevision());
					openNewAction.setToolTipText("Open Revision " + newFileDescriptor.getRevision());
					createActionHyperlink(toolbarComposite, toolkit, openNewAction);
				}
			}
		}
		textHyperlink = toolkit.createImageHyperlink(toolbarComposite, SWT.NONE);
		textHyperlink.setText("]");
		textHyperlink.setEnabled(false);
		textHyperlink.setUnderlined(false);

		boolean hasNewFile = checkHasFile(newFileDescriptor);
		boolean hasOldFile = checkHasFile(oldFileDescriptor);
		if (getCrucibleEditor() != null) {

			boolean showCompare = hasNewFile && hasOldFile;
			boolean isSCM = crucibleFile.getRepositoryType() == RepositoryType.SCM;
			boolean isUploaded = crucibleFile.getRepositoryType() == RepositoryType.UPLOAD;
			if ((isSCM || isUploaded) && showCompare && crucibleFile.getCommitType() != CommitType.Deleted
					&& filetype == FileType.File) {
				textHyperlink = toolkit.createImageHyperlink(toolbarComposite, SWT.NONE);
				textHyperlink.setText(" ");
				textHyperlink.setEnabled(false);
				textHyperlink.setUnderlined(false);

				if (newFileDescriptor != null && oldFileDescriptor != null) {

					if (isSCM) {
						compareAction = new CompareVersionedVirtualFileAction(crucibleFile, crucibleReview);
					} else {
						compareAction = new CompareUploadedVirtualFileAction(crucibleFile, null, crucibleReview,
								toolbarComposite.getShell());
					}
					compareAction.setToolTipText("Open Compare " + newFileDescriptor.getRevision() + " - "
							+ oldFileDescriptor.getRevision());
					compareAction.setText("Compare");
					// TODO set the image descriptor
					createActionHyperlink(toolbarComposite, toolkit, compareAction);
				}
			} else if (filetype == FileType.Directory) {
				toolkit.createLabel(toolbarComposite, " Directory");
			} else if (filetype == FileType.Unknown) {
				toolkit.createLabel(toolbarComposite, " Unknown Type");
			} else {
				toolkit.createLabel(toolbarComposite, " " + crucibleFile.getCommitType().name());
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
		if (CrucibleUtil.canAddCommentToReview(crucibleReview)) {
			AddGeneralCommentToFileAction addFileCommentAction = new AddGeneralCommentToFileAction();
			addFileCommentAction.setCrucibleFile(new CrucibleFile(crucibleFile, false));
			addFileCommentAction.setReview(crucibleReview);
			addFileCommentAction.setImageDescriptor(CrucibleImages.ADD_COMMENT);
			actions.add(addFileCommentAction);
		}
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

	public void selectAndReveal(VersionedComment commentToReveal, boolean reveal) {
		if (!getSection().isExpanded()) {
			CommonFormUtil.setExpanded(getSection(), true);
		}

		for (ExpandablePart<?, ?> part : getChildrenParts()) {
			if (part instanceof VersionedCommentPart) {
				if (((VersionedCommentPart) part).represents(commentToReveal)) {
					part.setExpanded(true);
					if (reveal) {
						CommonFormUtil.ensureVisible(part.getSection());
					}

					crucibleEditor.setHighlightedPart(part);

					return;
				}
			}
		}
	}

	public Control update(Composite parentComposite, FormToolkit toolkit, CrucibleFileInfo file, Review crucibleReview) {
		this.crucibleReview = crucibleReview;
		if (compareAction != null) {
			this.compareAction.updateReview(crucibleReview, file);
		}
		// TODO update the text 
		if (!CrucibleUtil.areCrucibleFilesDeepEqual(file, crucibleFile)) {
			boolean oldCanExpand = !canExpand(this.crucibleFile);
			boolean newCanExpand = !canExpand(file);
			this.crucibleFile = file;

			Control createControl = createOrUpdateControl(parentComposite, toolkit, oldCanExpand != newCanExpand);

			getSection().layout(true, true);

			update();

			return createControl;

		}
		return getSection();
	}

	// TODO handle changed highlighting properly

	private Control createOrUpdateControl(Composite parentComposite, FormToolkit toolkit, boolean needsRecreation) {
		if (getSection() == null) {

			Control createControl = createControl(parentComposite, toolkit);

			return createControl;
		} else if (needsRecreation) {
			getSection().dispose();
			Control createControl = createControl(parentComposite, toolkit);

			return createControl;
		} else {
			updateChildren(composite, toolkit, true, crucibleFile.getVersionedComments());
			return getSection();
		}

	}

	public void dispose() {
		getSection().dispose();
	}

	@Override
	protected VersionedCommentPart createChildPart(VersionedComment comment, Review crucibleReview2,
			CrucibleReviewEditorPage crucibleEditor2) {

		return new VersionedCommentPart(comment, crucibleReview2, crucibleFile, crucibleEditor2);
	}

	@Override
	protected Comparator<VersionedComment> getComparator() {
		return new VersionedCommentDateComparator();
	}

	// we are not a child of an expandable part, so these 3 methods dont matter
	@Override
	protected boolean represents(VersionedComment comment) {
		return false;
	}

	@Override
	protected boolean shouldHighlight(VersionedComment comment, CrucibleReviewEditorPage crucibleEditor2) {
		return false;
	}

	@Override
	protected Control update(Composite parentComposite, FormToolkit toolkit, VersionedComment newComment,
			Review newReview) {
		return null;
	}
}
