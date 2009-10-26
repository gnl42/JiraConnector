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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.ReviewWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.ReviewWizard.Type;
import com.atlassian.connector.eclipse.ui.forms.ReflowRespectingSection;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonFormUtil;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The form part that displays all of the review files and their comments
 * 
 * @author Shawn Minto
 */
public class CrucibleReviewFilesPart extends AbstractCrucibleEditorFormPart {

	private CrucibleReviewEditorPage crucibleEditor;

	private Review crucibleReview;

	private Section filesSection;

	private List<CrucibleFilePart> parts;

	private Composite parentComposite;

	@Override
	public void initialize(CrucibleReviewEditorPage editor, Review review, boolean isNewReview) {
		this.crucibleEditor = editor;
		this.crucibleReview = review;
		parts = new ArrayList<CrucibleFilePart>();
	}

	@Override
	public Collection<? extends ExpandablePart<?, ?>> getExpandableParts() {
		return parts;
	}

	@Override
	public CrucibleReviewEditorPage getReviewEditor() {
		return crucibleEditor;
	}

	@Override
	public Control createControl(final Composite parent, final FormToolkit toolkit) {
		int style = ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED;
		filesSection = new ReflowRespectingSection(toolkit, parent, style, crucibleEditor);
		filesSection.setText(getSectionTitle());
		filesSection.clientVerticalSpacing = 0;
		GridDataFactory.fillDefaults().grab(true, false).applyTo(filesSection);

		setSection(toolkit, filesSection);

		if (filesSection.isExpanded()) {
			Composite composite = createCommentViewers(parent, toolkit);
			filesSection.setClient(composite);
		} else {
			filesSection.addExpansionListener(new ExpansionAdapter() {
				@Override
				public void expansionStateChanged(ExpansionEvent e) {
					if (filesSection.getClient() == null) {
						try {
							crucibleEditor.setReflow(false);
							Composite composite = createCommentViewers(parent, toolkit);
							filesSection.setClient(composite);
						} finally {
							crucibleEditor.setReflow(true);
						}
						crucibleEditor.reflow(false);
					}
				}
			});
		}

		return filesSection;
	}

	private String getSectionTitle() {
		String title = "Review Files";
		try {
			return NLS.bind("{0}   ({1} files, {2} comments)", new Object[] { title, crucibleReview.getFiles().size(),
					crucibleReview.getNumberOfVersionedComments() });

		} catch (ValueNotYetInitialized e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return title;

	}

	private Composite createCommentViewers(Composite parent, FormToolkit toolkit) {
		parentComposite = toolkit.createComposite(filesSection);
		parentComposite.setLayout(GridLayoutFactory.fillDefaults().create());

		updateControl(crucibleReview, parent, toolkit, false);
		return parentComposite;
	}

	@Override
	protected void fillToolBar(ToolBarManager barManager) {
		Set<CrucibleAction> actions;
		try {
			actions = crucibleReview.getActions();
		} catch (ValueNotYetInitialized e) {
			actions = new HashSet<CrucibleAction>();
		}
		if (actions.contains(CrucibleAction.MODIFY_FILES)) {
			Action addChangesetAction = new Action() {
				@Override
				public void run() {
					ReviewWizard wizard = new ReviewWizard(crucibleReview, Type.ADD_CHANGESET);
					wizard.setWindowTitle("Add Changeset");
					WizardDialog wd = new WizardDialog(WorkbenchUtil.getShell(), wizard);
					wd.setBlockOnOpen(true);
					wd.open();
				}
			};
			addChangesetAction.setText("Add changesets...");
			addChangesetAction.setToolTipText("Add changesets to the Review.");
			addChangesetAction.setImageDescriptor(CrucibleImages.ADD_CHANGESET);
			barManager.add(addChangesetAction);

			Action addPatchAction = new Action() {
				@Override
				public void run() {
					ReviewWizard wizard = new ReviewWizard(crucibleReview, Type.ADD_PATCH);
					wizard.setWindowTitle("Add Patch");
					WizardDialog wd = new WizardDialog(WorkbenchUtil.getShell(), wizard);
					wd.setBlockOnOpen(true);
					wd.open();
				}
			};
			addPatchAction.setText("Add Patch...");
			addPatchAction.setToolTipText("Add a patch to the Review");
			addPatchAction.setImageDescriptor(CrucibleImages.ADD_PATCH);
			barManager.add(addPatchAction);

			Action addWorkspacePatchAction = new Action() {
				@Override
				public void run() {
					ReviewWizard wizard = new ReviewWizard(crucibleReview, Type.ADD_WORKSPACE_PATCH);
					wizard.setWindowTitle("Add Workspace Changes");
					WizardDialog wd = new WizardDialog(WorkbenchUtil.getShell(), wizard);
					wd.setBlockOnOpen(true);
					wd.open();
				}
			};
			addWorkspacePatchAction.setText("Add Workspace Changes...");
			addWorkspacePatchAction.setToolTipText("Add a patch from workspace changes to the Review");
			addWorkspacePatchAction.setImageDescriptor(CrucibleImages.ADD_PATCH);
			barManager.add(addWorkspacePatchAction);

			barManager.add(new Separator());
		}

		super.fillToolBar(barManager);
	}

	public void selectAndReveal(CrucibleFileInfo crucibleFile, VersionedComment comment, boolean reveal) {
		if (!filesSection.isExpanded()) {
			CommonFormUtil.setExpanded(filesSection, true);
		}

		for (CrucibleFilePart part : parts) {
			if (part.isCrucibleFile(crucibleFile)) {
				part.selectAndReveal(comment, reveal);
			}
		}
	}

	@Override
	public void updateControl(Review review, Composite parent, FormToolkit toolkit) {
		updateControl(review, parent, toolkit, true);
	}

	public void updateControl(Review review, Composite parent, FormToolkit toolkit, boolean shouldHighlight) {
		this.crucibleReview = review;
		filesSection.setText(getSectionTitle());

		if (parentComposite == null) {
			if (filesSection.getClient() == null) {
				try {
					crucibleEditor.setReflow(false);
					Composite composite = createCommentViewers(parent, toolkit);
					filesSection.setClient(composite);
				} finally {
					crucibleEditor.setReflow(true);
				}
				crucibleEditor.reflow(false);
			}
			return;
		}

		parentComposite.setMenu(null);

		try {
			List<CrucibleFileInfo> files = new ArrayList<CrucibleFileInfo>(crucibleReview.getFiles());
			Collections.sort(files, new Comparator<CrucibleFileInfo>() {

				public int compare(CrucibleFileInfo o1, CrucibleFileInfo o2) {
					if (o1 != null && o2 != null) {
						return o1.getFileDescriptor().getUrl().compareTo(o2.getFileDescriptor().getUrl());
					}
					return 0;
				}

			});

			// The following code is almost duplicated in the crucible general comments part
			List<CrucibleFilePart> newParts = new ArrayList<CrucibleFilePart>();

			Control prevControl = null;

			for (int i = 0; i < files.size(); i++) {
				CrucibleFileInfo file = files.get(i);

				CrucibleFilePart oldPart = findPart(file);

				if (oldPart != null) {
					Control commentControl = oldPart.update(parentComposite, toolkit, file, crucibleReview);
					if (commentControl != null) {

						GridDataFactory.fillDefaults().grab(true, false).applyTo(commentControl);

						if (prevControl != null) {
							commentControl.moveBelow(prevControl);
						} else if (parentComposite.getChildren().length > 0) {
							commentControl.moveAbove(parentComposite.getChildren()[0]);
						}
						prevControl = commentControl;
					}

					newParts.add(oldPart);
				} else {

					CrucibleFilePart part = new CrucibleFilePart(file, crucibleReview, crucibleEditor);
					newParts.add(part);
					Control fileControl = part.createControl(parentComposite, toolkit);
					//GridDataFactory.fillDefaults().grab(true, false).applyTo(fileControl);
					GridData gd = GridDataFactory.fillDefaults().grab(true, false).create();
					if (!part.canExpand()) {
						gd.horizontalIndent = 15;
					}
					fileControl.setLayoutData(gd);

					if (shouldHighlight) {
						part.setIncomming(true);
						part.decorate();
					}

					if (prevControl != null) {
						fileControl.moveBelow(prevControl);
					} else if (parentComposite.getChildren().length > 0) {
						fileControl.moveAbove(parentComposite.getChildren()[0]);
					}
					prevControl = fileControl;
				}
			}

			List<CrucibleFilePart> toRemove = new ArrayList<CrucibleFilePart>();

			for (CrucibleFilePart part : parts) {
				if (!newParts.contains(part)) {
					toRemove.add(part);
				}
			}

			for (CrucibleFilePart part : toRemove) {
				part.dispose();
			}

			parts.clear();
			parts.addAll(newParts);

		} catch (ValueNotYetInitialized e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}

	}

	private CrucibleFilePart findPart(CrucibleFileInfo file) {

		for (CrucibleFilePart part : parts) {
			if (part.isCrucibleFile(file)) {
				return part;
			}
		}

		return null;
	}

}
