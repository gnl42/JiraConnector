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

import java.util.HashMap;

/**
 * @author Thomas Ehrnhoefer
 */
public class SummarizeReviewPart {

	private static final String OTHER_DRAFTS_WARNING = "Warning - Other participants have draft comments. "
			+ "If you summarize now then their draft comments will be deleted. ";

	private static final String SUMMARIZE_LABEL = "Summarize the Review Outcomes (optional)";

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
		composite.setLayout(new GridLayout(2, false));

		Label summaryLabel = new Label(composite, SWT.NONE);
		summaryLabel.setText(SUMMARIZE_LABEL);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(summaryLabel);

		summaryText = new Text(composite, SWT.MULTI | SWT.V_SCROLL);
		GridData textGridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
		textGridData.horizontalSpan = 2;
		textGridData.heightHint = 120;
		textGridData.widthHint = 180;
		summaryText.setLayoutData(textGridData);

		boolean hasDrafts = checkForDrafts();
		boolean hasOthersDrafts = checkForOthersDrafts();

		if (hasDrafts) {
			Label draftComments = new Label(composite, SWT.NONE);
			draftComments.setText("There are draft comments in the review.");
			GridDataFactory.fillDefaults().span(2, 1).applyTo(draftComments);

			Composite draftComp = new Composite(composite, SWT.NONE);
			draftComp.setLayout(new GridLayout(1, false));
			GridDataFactory.fillDefaults().span(2, 1).applyTo(draftComp);

			if (hasOthersDrafts) {
				HashMap<String, Integer> othersDrafts = getOthersDrafts();
				List othersDraftsList = new List(draftComp, SWT.SINGLE | SWT.BORDER);
				othersDraftsList.setEnabled(false);
				for (String user : othersDrafts.keySet()) {
					if (user.equals(me)) {
						othersDraftsList.add(user + "(" + othersDrafts.get(user).toString() + ")", 0);
					} else {
						othersDraftsList.add(user + "(" + othersDrafts.get(user).toString() + ")");
					}
				}
				Label looseDrafts = new Label(draftComp, SWT.WRAP | SWT.BORDER);
				looseDrafts.setText(OTHER_DRAFTS_WARNING);
			}

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

		Button summarize = new Button(composite, SWT.PUSH);
		summarize.setText("Summarize and Close Review");
		summarize.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				summarizedText = summaryText.getText();
				notifyOK();
			}
		});

		Button cancel = new Button(composite, SWT.PUSH);
		cancel.setText("Cancel");
		cancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				notifyCancel();
			}
		});

		//CHECKSTYLE:MAGIC:ON
		return composite;
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

	private boolean checkForOthersDrafts() {
		try {
			int totalDrafts = review.getNumberOfGeneralCommentsDrafts() + review.getNumberOfVersionedCommentsDrafts();
			int myDrafts = review.getNumberOfGeneralCommentsDrafts(me) + review.getNumberOfVersionedCommentsDrafts(me);
			int drafts = review.getNumberOfGeneralCommentsDrafts("Steffen Pingel")
					+ review.getNumberOfVersionedCommentsDrafts("Steffen Pingel");
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
