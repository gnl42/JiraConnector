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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.EditCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.PostDraftCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.RemoveCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.ReplyToCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.connector.eclipse.internal.crucible.ui.views.ReviewContentProvider;
import com.atlassian.connector.eclipse.ui.forms.ReflowRespectingSection;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import java.text.DateFormat;
import java.util.Collection;
import java.util.List;

/**
 * The form part that displays the general comments for the review
 * 
 * @author Shawn Minto
 * @author Pawel Niewiadomski
 */
public class CrucibleGeneralCommentsPart extends AbstractCrucibleEditorFormPart {

	private static final String[] NO_GENERAL_COMMENTS = new String[] { "There are no general comments." };

	private CrucibleReviewEditorPage crucibleEditor;

	private Review crucibleReview;

	private Section commentsSection;

	private Composite parentComposite;

	private TreeViewer viewer;

	private ReplyToCommentAction replyToCommentAction;

	private EditCommentAction editCommentAction;

	private RemoveCommentAction removeCommentAction;

	private PostDraftCommentAction postDraftCommentAction;

	@Override
	public void initialize(CrucibleReviewEditorPage editor, Review review, boolean isNewReview) {
		this.crucibleEditor = editor;
		this.crucibleReview = review;
	}

	@Override
	public Collection<? extends ExpandablePart<?, ?>> getExpandableParts() {
		return MiscUtil.buildArrayList();
	}

	@Override
	public CrucibleReviewEditorPage getReviewEditor() {
		return crucibleEditor;
	}

	@Override
	protected void collapseAll() {
		if (viewer != null) {
			viewer.collapseAll();
		}
	}

	protected void expandAll() {
		if (viewer != null) {
			viewer.expandAll();
		}
	}

	@Override
	public Control createControl(final Composite parent, final FormToolkit toolkit) {
		int style = ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED;
		commentsSection = new ReflowRespectingSection(toolkit, parent, style, crucibleEditor);
		commentsSection.setText(getSectionTitle());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(commentsSection);

		setSection(toolkit, commentsSection);

		if (commentsSection.isExpanded()) {
			Composite composite = createCommentViewers(parent, toolkit);
			commentsSection.setClient(composite);
		} else {
			commentsSection.addExpansionListener(new ExpansionAdapter() {
				@Override
				public void expansionStateChanged(ExpansionEvent e) {
					if (commentsSection.getClient() == null) {
						try {
							crucibleEditor.setReflow(false);
							Composite composite = createCommentViewers(parent, toolkit);
							commentsSection.setClient(composite);
						} finally {
							crucibleEditor.setReflow(true);
						}
						crucibleEditor.reflow(false);
					}
				}
			});
		}

		return commentsSection;
	}

	private String getSectionTitle() {
		String title = "General Comments";
		try {
			return NLS.bind("{0} ({1} comments)", title, crucibleReview.getNumberOfGeneralComments());
		} catch (ValueNotYetInitialized e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return title;
	}

	private Composite createCommentViewers(Composite parent, FormToolkit toolkit) {
		parentComposite = toolkit.createComposite(commentsSection);
		parentComposite.setLayout(GridLayoutFactory.fillDefaults().create());

		viewer = new TreeViewer(parentComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getControl());
		viewer.setContentProvider(new ReviewContentProvider());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Comment) {
					Comment comment = (Comment) element;
					String headerText = comment.getAuthor().getDisplayName() + "   ";
					headerText += DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(
							comment.getCreateDate());

					if (comment.isDefectApproved()) {
						headerText += " Approved Defect";
					} else if (comment.isDefectRaised()) {
						headerText += " Defect";
					}

					if (comment.isDraft()) {
						headerText += " Draft";
					}
					headerText += "\n\n";
					headerText += comment.getMessage();
					return headerText;
				}
				return super.getText(element);
			}
		});

		viewer.addSelectionChangedListener(replyToCommentAction);
		viewer.addSelectionChangedListener(editCommentAction);
		viewer.addSelectionChangedListener(removeCommentAction);
		viewer.addSelectionChangedListener(postDraftCommentAction);

		try {
			List<GeneralComment> comments = crucibleReview.getGeneralComments();
			viewer.setInput(comments.size() == 0 ? NO_GENERAL_COMMENTS : comments);
		} catch (ValueNotYetInitialized e) {
		}
		return parentComposite;
	}

	private void createActions() {
		replyToCommentAction = new ReplyToCommentAction() {
			@Override
			protected Review getActiveReview() {
				return crucibleReview;
			}
		};

		editCommentAction = new EditCommentAction() {
			@Override
			protected Review getActiveReview() {
				return crucibleReview;
			};
		};

		removeCommentAction = new RemoveCommentAction() {
			@Override
			public Review getActiveReview() {
				return crucibleReview;
			}
		};

		postDraftCommentAction = new PostDraftCommentAction() {
			@Override
			public Review getActiveReview() {
				return crucibleReview;
			}
		};
	}

	@Override
	protected void fillToolBar(ToolBarManager mgr) {
		createActions();

		mgr.add(replyToCommentAction);
		mgr.add(editCommentAction);
		mgr.add(removeCommentAction);
		mgr.add(postDraftCommentAction);

		super.fillToolBar(mgr);
	}

	@Override
	public void updateControl(Review review, Composite parent, FormToolkit toolkit) {
		this.crucibleReview = review;

		commentsSection.setText(getSectionTitle());

		try {
			List<GeneralComment> comments = crucibleReview.getGeneralComments();
			viewer.setInput(comments.size() == 0 ? NO_GENERAL_COMMENTS : comments);
		} catch (ValueNotYetInitialized e) {
		}
	}

}
