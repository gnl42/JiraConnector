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

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Part for summarizing a review
 * 
 * @author Thomas Ehrnhoefer
 */
public class SummarizeReviewPart {

	private static final String OTHER_DRAFTS_WARNING = "Warning - Other participants' draft comments will be discarded.";

	private static final String OPEN_REVIEWS_WARNING = "The following reviewers have not yet finished this review: ";

	private static final String COMPLETED_REVIEWS_INFO = "The following people have completed this review:";

	public interface ISummarizeReviewPartListener {
		void cancelSummarizeReview();

		void summarizeReview();
	}

	private final Review review;

	private boolean discardDrafts = true;

	private ISummarizeReviewPartListener listener;

	private final String me;

	private String summarizedText;

	private Text summaryText;

	public SummarizeReviewPart(Review review, String userName) {
		super();
		this.review = review;
		me = userName;
	}

	public Composite createControl(Composite parent) {
		//CHECKSTYLE:MAGIC:OFF

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		new Label(composite, SWT.NONE).setText("Summarize the Review Outcomes (optional)");

		summaryText = new Text(composite, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		GridData textGridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
		textGridData.heightHint = 120;
		textGridData.widthHint = 200;
		summaryText.setLayoutData(textGridData);

		handleOpenReviewsAndDrafts(composite);

		Composite buttonComp = new Composite(composite, SWT.NONE);
		buttonComp.setLayout(new GridLayout(2, false));
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(buttonComp);
		Button summarizeButton = new Button(buttonComp, SWT.PUSH);
		summarizeButton.setText("Summarize and Close Review");
		summarizeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				summarizedText = summaryText.getText();
				notifyOK();
			}
		});
		Button cancelButton = new Button(buttonComp, SWT.PUSH);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				notifyCancel();
			}
		});
		//CHECKSTYLE:MAGIC:ON
		return composite;
	}

	private void handleOpenReviewsAndDrafts(Composite composite) {
		boolean hasDrafts = checkForDrafts();
		boolean hasOthersDrafts = checkForOthersDrafts();
		Set<Reviewer> openReviewers = getOpenReviewers();
		Set<Reviewer> completedReviewers = getCompletedReviewers();

		Composite draftComp = new Composite(composite, SWT.NONE);
		draftComp.setLayout(new GridLayout(1, false));

		if (completedReviewers.size() > 0) {
			GridDataFactory.fillDefaults().grab(true, false).applyTo(
					new Label(draftComp, SWT.SEPARATOR | SWT.HORIZONTAL));
			new CrucibleReviewersPart(completedReviewers).createControl(null, draftComp, COMPLETED_REVIEWS_INFO);
		}

		if (openReviewers.size() > 0) {
			GridDataFactory.fillDefaults().grab(true, false).applyTo(
					new Label(draftComp, SWT.SEPARATOR | SWT.HORIZONTAL));
			new CrucibleReviewersPart(openReviewers).createControl(null, draftComp, OPEN_REVIEWS_WARNING);
			Label labelControl = new Label(draftComp, SWT.WRAP);
			labelControl.setText(OTHER_DRAFTS_WARNING);
			if (hasOthersDrafts) {
				Set<Reviewer> othersDrafts = getOthersDrafts();
				new CrucibleReviewersPart(othersDrafts).createControl(null, draftComp, "Reviewers with draft comments:");
			}
		}

		if (hasDrafts) {
			GridDataFactory.fillDefaults().grab(true, false).applyTo(
					new Label(draftComp, SWT.SEPARATOR | SWT.HORIZONTAL));
			Label draftComments = new Label(draftComp, SWT.NONE);
			draftComments.setText("You have open drafts in this review. Please choose an action:");
			GridDataFactory.fillDefaults().span(2, 1).applyTo(draftComments);

			Button deleteDrafts = new Button(draftComp, SWT.RADIO);
			deleteDrafts.setText("Discard Drafts");
			deleteDrafts.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					discardDrafts = true;
				}
			});
			Button postDrafts = new Button(draftComp, SWT.RADIO);
			postDrafts.setText("Post Drafts");
			postDrafts.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					discardDrafts = false;
				}
			});
			deleteDrafts.setSelection(discardDrafts);
		}
	}

	private Set<Reviewer> getOthersDrafts() {
		Set<Reviewer> othersDrafts = new LinkedHashSet<Reviewer>();
		try {
			for (GeneralComment comment : review.getGeneralComments()) {
				checkCommentForDraft(comment, othersDrafts);
			}
		} catch (ValueNotYetInitialized e) {
			//
		}
		try {
			for (CrucibleFileInfo file : review.getFiles()) {
				for (VersionedComment comment : file.getVersionedComments()) {
					checkCommentForDraft(comment, othersDrafts);
				}
			}
		} catch (ValueNotYetInitialized e) {
			// 
		}
		if (othersDrafts.contains(null)) {
			othersDrafts.remove(null);
		}
		return othersDrafts;
	}

	private void checkCommentForDraft(Comment comment, Set<Reviewer> othersDrafts) {
		if (comment.isDraft()) {
			othersDrafts.add(getReviewer(comment.getAuthor()));
		}
		if (!comment.isReply()) {
			if (comment instanceof VersionedComment) {
				for (VersionedComment versionedComment : ((VersionedComment) comment).getReplies()) {
					checkCommentForDraft(versionedComment, othersDrafts);
				}
			}
			if (comment instanceof GeneralComment) {
				for (GeneralComment generalComment : ((GeneralComment) comment).getReplies()) {
					checkCommentForDraft(generalComment, othersDrafts);
				}
			}
		}
	}

	private Reviewer getReviewer(User author) {
		try {
			for (Reviewer reviewer : review.getReviewers()) {
				if (reviewer.getUserName().equals(author.getUserName())) {
					return reviewer;
				}
			}
		} catch (ValueNotYetInitialized e) {
			// 
		}
		return null;
	}

	private Set<Reviewer> getOpenReviewers() {
		Set<Reviewer> openReviewers = new LinkedHashSet<Reviewer>();
		try {
			for (Reviewer reviewer : review.getReviewers()) {
				if (!reviewer.isCompleted()) {
					openReviewers.add(reviewer);
				}
			}
		} catch (ValueNotYetInitialized e) {
			//
		}
		return openReviewers;
	}

	private Set<Reviewer> getCompletedReviewers() {
		Set<Reviewer> completedReviewers = new LinkedHashSet<Reviewer>();
		try {
			for (Reviewer reviewer : review.getReviewers()) {
				if (reviewer.isCompleted()) {
					completedReviewers.add(reviewer);
				}
			}
		} catch (ValueNotYetInitialized e) {
			//
		}
		return completedReviewers;
	}

	/**
	 * Wont work yet since API does not seem to make other's drafts available
	 * 
	 * @return
	 */
	private boolean checkForOthersDrafts() {
		try {
			int totalDrafts = review.getNumberOfGeneralCommentsDrafts() + review.getNumberOfVersionedCommentsDrafts();
			int myDrafts = review.getNumberOfGeneralCommentsDrafts(me) + review.getNumberOfVersionedCommentsDrafts(me);
			if (totalDrafts > myDrafts) {
				return true;
			}
		} catch (ValueNotYetInitialized e) {
			return false;
		}
		return false;
	}

	private void notifyCancel() {
		if (listener != null) {
			listener.cancelSummarizeReview();
		}
	}

	private void notifyOK() {
		if (listener != null) {
			listener.summarizeReview();
		}
	}

	private boolean checkForDrafts() {
		try {
			if ((review.getNumberOfGeneralCommentsDrafts() + review.getNumberOfVersionedCommentsDrafts()) > 0) {
				return true;
			}
		} catch (ValueNotYetInitialized e) {
			return false;
		}
		return false;
	}

	public void setListener(ISummarizeReviewPartListener listener) {
		this.listener = listener;
	}

	public boolean isDiscardDrafts() {
		return discardDrafts;
	}

	public String getSummarizeText() {
		return summarizedText == null ? "" : summarizedText;
	}

	public void dispose() {
		//ignore
	}

}
