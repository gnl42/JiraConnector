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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiConstants;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewType;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import java.util.Collection;

/**
 * The form part that displays the general comments for the review
 * 
 * @author Shawn Minto
 * @author Pawel Niewiadomski
 */
public class EmptyReviewFilesPart extends AbstractCrucibleEditorFormPart {

	private CrucibleReviewEditorPage crucibleEditor;

	private Review crucibleReview;

	private Section commentsSection;

	private Composite parentComposite;

	@Override
	public void initialize(CrucibleReviewEditorPage editor, Review review, boolean isNewReview) {
		this.crucibleEditor = editor;
		this.crucibleReview = review;
	}

	@Override
	public CrucibleReviewEditorPage getReviewEditor() {
		return crucibleEditor;
	}

	@Override
	public Control createControl(final Composite parent, final FormToolkit toolkit) {
		int style = ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED | ExpandableComposite.TWISTIE;
		commentsSection = toolkit.createSection(parent, style);
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
		return NLS.bind("Review files ({0} files, {1} comments)", new Object[] { crucibleReview.getFiles().size(),
				crucibleReview.getNumberOfVersionedComments() });
	}

	private Composite createCommentViewers(Composite parent, FormToolkit toolkit) {
		parentComposite = toolkit.createComposite(commentsSection);
		parentComposite.setLayout(GridLayoutFactory.fillDefaults().create());

		if (crucibleReview.getType() == ReviewType.REVIEW) {
			Label t = toolkit.createLabel(
					parentComposite,
					"You need to activate this task to see review files and comments. "
							+ "You will be automatically switched to Crucible Review Perspective. "
							+ "Don't worry though when you deactivate the task you'll be right back in this perspective.",
					SWT.WRAP);

			GridDataFactory.fillDefaults().grab(true, false).hint(500, SWT.DEFAULT).applyTo(t);

			Link link = new Link(parentComposite, SWT.NONE);
			link.setText("<a>Show review files</a>");
			link.setToolTipText("Activates this review (if not already active) "
					+ "and focuses on Review Explorer view populated with review files.");
			link.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					TasksUi.getTaskActivityManager().activateTask(crucibleEditor.getTask());
					try {
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
								CrucibleUiConstants.REVIEW_EXPLORER_VIEW);
					} catch (PartInitException e1) {
						StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
								"Failed to show Review Explorer View", e1));
					}
				}
			});
		} else {
			Label t = toolkit.createLabel(parentComposite,
					"If you want to see a content of the snippet review you need to go to web.", SWT.WRAP);

			GridDataFactory.fillDefaults().grab(true, false).hint(500, SWT.DEFAULT).applyTo(t);

			Link link = new Link(parentComposite, SWT.NONE);
			link.setText("<a>Open review in browser</a>");
			link.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					TasksUiUtil.openWithBrowser(CrucibleUiUtil.getCrucibleTaskRepository(crucibleReview),
							CrucibleUiUtil.getCrucibleTask(crucibleReview));
				}
			});
		}

		return parentComposite;
	}

	@Override
	protected void fillToolBar(ToolBarManager barManager) {
	}

	@Override
	public void updateControl(Review review, Composite parent, FormToolkit toolkit) {
		this.crucibleReview = review;

		commentsSection.setText(getSectionTitle());
	}

	@Override
	public Collection<? extends ExpandablePart<?, ?>> getExpandableParts() {
		return MiscUtil.buildArrayList();
	}
}
