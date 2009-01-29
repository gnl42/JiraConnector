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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.HashMap;

/**
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

		Label summaryLabel = new Label(composite, SWT.NONE);
		summaryLabel.setText("Summarize the Review Outcomes (optional)");

		summaryText = new Text(composite, SWT.MULTI | SWT.V_SCROLL);
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
		java.util.List<Reviewer> openReviewers = getOpenReviewers();
		java.util.List<Reviewer> completedReviewers = getCompletedReviewers();

		Composite draftComp = new Composite(composite, SWT.NONE);
		draftComp.setLayout(new GridLayout(1, false));

		if (completedReviewers.size() > 0) {
			GridDataFactory.fillDefaults().grab(true, false).applyTo(
					new Label(draftComp, SWT.SEPARATOR | SWT.HORIZONTAL));
			Label completedReviewsLabel = new Label(draftComp, SWT.WRAP);
			completedReviewsLabel.setText(COMPLETED_REVIEWS_INFO);
			List completedReviewsList = new List(draftComp, SWT.SINGLE | SWT.BORDER);
			completedReviewsList.setEnabled(false);
			GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(completedReviewsList);
			for (Reviewer reviewer : completedReviewers) {
				completedReviewsList.add(reviewer.getDisplayName());
			}
		}

		if (openReviewers.size() > 0) {
			GridDataFactory.fillDefaults().grab(true, false).applyTo(
					new Label(draftComp, SWT.SEPARATOR | SWT.HORIZONTAL));
			Label openReviewsLabel = new Label(draftComp, SWT.WRAP);
			openReviewsLabel.setText(OPEN_REVIEWS_WARNING);
			List openReviewsList = new List(draftComp, SWT.SINGLE | SWT.BORDER);
			GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(openReviewsList);
			openReviewsList.setEnabled(false);
			for (Reviewer reviewer : openReviewers) {
				openReviewsList.add(reviewer.getDisplayName());
			}
			Label looseDraftsLabel = new Label(draftComp, SWT.WRAP);
			looseDraftsLabel.setText(OTHER_DRAFTS_WARNING);

			if (hasOthersDrafts) {
				HashMap<String, Integer> othersDrafts = getOthersDrafts();
				List othersDraftsList = new List(draftComp, SWT.SINGLE | SWT.BORDER);
				GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(othersDraftsList);
				othersDraftsList.setEnabled(false);
				for (String user : othersDrafts.keySet()) {
					if (user.equals(me)) {
						othersDraftsList.add(user + "(" + othersDrafts.get(user).toString() + ")", 0);
					} else {
						othersDraftsList.add(user + "(" + othersDrafts.get(user).toString() + ")");
					}
				}
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

	private HashMap<String, Integer> getOthersDrafts() {
		HashMap<String, Integer> othersDrafts = new HashMap<String, Integer>();
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
		return othersDrafts;
	}

	private void checkCommentForDraft(Comment comment, HashMap<String, Integer> othersDrafts) {
		if (comment.isDraft()) {
			addOthersDraft(othersDrafts, comment.getAuthor().getUserName());
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

	private void addOthersDraft(HashMap<String, Integer> othersDrafts, String user) {
		Integer i = othersDrafts.get(user);
		if (i == null) {
			i = new Integer(1);
		} else {
			i = new Integer(i.intValue() + 1);
		}
		othersDrafts.put(user, i);
	}

	private java.util.List<Reviewer> getOpenReviewers() {
		java.util.List<Reviewer> openReviewers = new ArrayList<Reviewer>();
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

	private java.util.List<Reviewer> getCompletedReviewers() {
		java.util.List<Reviewer> completedReviewers = new ArrayList<Reviewer>();
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
}
