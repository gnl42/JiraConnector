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

import com.atlassian.connector.eclipse.internal.crucible.core.client.model.CrucibleCachedUser;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleUserContentProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleUserLabelProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.dialogs.ReviewerSelectionDialog;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.connector.eclipse.ui.AtlassianImages;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.User;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Set;

/**
 * The form part that displays the details of the review
 * 
 * @author Shawn Minto
 */
public class CrucibleDetailsPart extends AbstractCrucibleEditorFormPart {

	private Review crucibleReview;

	private CrucibleReviewEditorPage crucibleEditor;

	private Composite parentComposite;

	private Composite reviewersComp;

	private Section reviewersSection;

	private Composite reviewersPart;

	private IAction setReviewersAction;

	@Override
	public void initialize(CrucibleReviewEditorPage editor, Review review) {
		this.crucibleReview = review;
		this.crucibleEditor = editor;
	}

	@Override
	public Collection<? extends ExpandablePart<?, ?>> getExpandableParts() {
		return null;
	}

	@Override
	public CrucibleReviewEditorPage getReviewEditor() {
		return crucibleEditor;
	}

	@Override
	public Control createControl(Composite parent, FormToolkit toolkit) {
		//CHECKSTYLE:MAGIC:OFF

		parentComposite = new Composite(parent, SWT.NONE);
		toolkit.adapt(parentComposite);
		parentComposite.setLayout(GridLayoutFactory.fillDefaults()
				.spacing(10, 10)
				.equalWidth(true)
				.numColumns(2)
				.create());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(parentComposite);

		updateControl(this.crucibleReview, parent, toolkit);

		return parentComposite;
	}

	private Text createReadOnlyText(FormToolkit toolkit, Composite parent, String value, String labelString,
			boolean isMultiline) {
		return createText(toolkit, parent, value, labelString, isMultiline, true);
	}

	private Text createText(FormToolkit toolkit, Composite parent, String value, String labelString,
			boolean isMultiline, boolean isReadOnly) {

		if (labelString != null) {
			createLabelControl(toolkit, parent, labelString);
		}
		int style = SWT.FLAT;
		if (isReadOnly) {
			style |= SWT.READ_ONLY;
		} else {
			style |= SWT.BORDER;
		}
		if (isMultiline) {
			style |= SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL;
		}
		Text text = new Text(parent, style);
		text.setFont(EditorUtil.TEXT_FONT);
		text.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
		text.setText(value);
		toolkit.adapt(text, true, true);

		return text;
	}

	private Label createLabelControl(FormToolkit toolkit, Composite parent, String labelString) {
		Label labelControl = toolkit.createLabel(parent, labelString);
		labelControl.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		return labelControl;
	}

	private Control createUserComboControl(FormToolkit toolkit, Composite parent, String labelString, User selection,
			boolean readOnly) {
		if (labelString != null) {
			createLabelControl(toolkit, parent, labelString);
		}

		Set<CrucibleCachedUser> users = CrucibleUiUtil.getCachedUsers(crucibleReview);

		Control control;
		if (readOnly) {
			Text text = new Text(parent, SWT.FLAT | SWT.READ_ONLY);
			text.setFont(JFaceResources.getTextFont());
			text.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
			text.setText(selection.getDisplayName());
			text.setData(selection);
			control = text;
			toolkit.adapt(text, true, true);
		} else {
			CCombo combo = new CCombo(parent, SWT.BORDER);
			ComboViewer comboViewer = new ComboViewer(combo);
			comboViewer.setLabelProvider(new CrucibleUserLabelProvider());
			comboViewer.setContentProvider(new CrucibleUserContentProvider());
			comboViewer.setInput(users);

			comboViewer.setSelection(new StructuredSelection(new CrucibleCachedUser(selection)));
			control = combo;
		}
		control.setBackground(toolkit.getColors().getBackground());
		control.setForeground(toolkit.getColors().getForeground());
		control.setFont(JFaceResources.getDefaultFont());
		control.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
		toolkit.adapt(control, true, true);
		return control;
	}

	@Override
	public void updateControl(Review review, Composite parent, final FormToolkit toolkit) {
		this.crucibleReview = review;
		if (parentComposite == null) {
			createControl(parent, toolkit);
		}

		for (Control c : parentComposite.getChildren()) {
			c.dispose();
			//TODO disposing not necessary, simply updating labels and a re-layout should be sufficient; low priority though
		}
		parentComposite.setMenu(null);

		boolean readOnlyFields = true;
		try {
			Set<CrucibleAction> actions = crucibleReview.getActions();
			if (actions != null && actions.contains(CrucibleAction.MODIFY_FILES)) {
				readOnlyFields = false;
			}
		} catch (ValueNotYetInitialized e) {
			StatusHandler.log(new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID,
					"Could not retrieve available Crucible actions", e));
		}

		Text nameText = createText(toolkit, parentComposite, crucibleReview.getName(), null, false, readOnlyFields);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(nameText);

		Composite statusComp = toolkit.createComposite(parentComposite);
		statusComp.setLayout(GridLayoutFactory.fillDefaults().numColumns(4).spacing(10, 0).create());
		Text stateText = createReadOnlyText(toolkit, statusComp, crucibleReview.getState().getDisplayName(), "State: ",
				false);
		GridDataFactory.fillDefaults().applyTo(stateText);

		Text openSinceText = createReadOnlyText(toolkit, statusComp, DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.SHORT).format(crucibleReview.getCreateDate()), "Open Since: ", false);
		GridDataFactory.fillDefaults().applyTo(openSinceText);

		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(statusComp);

		reviewersSection = toolkit.createSection(parentComposite, ExpandableComposite.TWISTIE
				| ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
		reviewersSection.setText("Participants");
		final Composite participantsComp = toolkit.createComposite(reviewersSection);

		Control authorControl = createUserComboControl(toolkit, participantsComp, "Author: ",
				crucibleReview.getAuthor(), readOnlyFields);
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.TOP).applyTo(authorControl);

		Control moderatorControl = createUserComboControl(toolkit, participantsComp, "Moderator: ",
				crucibleReview.getModerator(), readOnlyFields);
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.TOP).applyTo(moderatorControl);

		Composite reviewersPartComp = toolkit.createComposite(participantsComp);
		reviewersPartComp.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).spacing(15, 0).create());
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).align(SWT.FILL, SWT.TOP).applyTo(reviewersPartComp);

		createReviewersPart(toolkit, reviewersPartComp, null);

		GridDataFactory.fillDefaults().grab(true, false).applyTo(reviewersSection);
		participantsComp.setLayout(GridLayoutFactory.fillDefaults().margins(2, 2).numColumns(2).create());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(participantsComp);
		reviewersSection.setClient(participantsComp);

		Section objectivesSection = toolkit.createSection(parentComposite, ExpandableComposite.TWISTIE
				| ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
		objectivesSection.setText("Statement of Objectives");
		Composite objectivesComp = toolkit.createComposite(objectivesSection);
		Text descriptionText = createText(toolkit, objectivesComp, crucibleReview.getDescription(), null, true,
				readOnlyFields);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(objectivesSection);
		objectivesComp.setLayout(GridLayoutFactory.fillDefaults().margins(2, 2).numColumns(1).create());
		GridDataFactory.fillDefaults().grab(true, true).hint(250, 80).applyTo(descriptionText);
		objectivesSection.setClient(objectivesComp);
		//CHECKSTYLE:MAGIC:ON

		toolkit.paintBordersFor(parentComposite);
	}

	private void disposeReviewersPart() {
		if (reviewersPart != null && !reviewersPart.isDisposed()) {
			EditorUtil.setMenu(reviewersPart, null);
			reviewersPart.dispose();
		}
	}

	private void createReviewersPart(final FormToolkit toolkit, final Composite parent, Set<Reviewer> reviewers) {
		if (reviewersComp == null || reviewersComp.isDisposed()) {
			reviewersComp = toolkit.createComposite(parent);
			reviewersComp.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).spacing(15, 0).create());
			GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.TOP).applyTo(reviewersComp);
		}
		try {
			if (reviewers == null) {
				reviewers = crucibleReview.getReviewers();
			}
			CrucibleReviewersPart crucibleReviewersPart = new CrucibleReviewersPart(reviewers);
			crucibleReviewersPart.setMenu(parent.getMenu());
			reviewersPart = crucibleReviewersPart.createControl(toolkit, reviewersComp);

			if (setReviewersAction == null) {
				addReviewersAction(toolkit, parent);
			}

		} catch (ValueNotYetInitialized e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
	}

	private void addReviewersAction(final FormToolkit toolkit, final Composite parent) {
		setReviewersAction = new Action() {
			@Override
			public void run() {
				ReviewerSelectionDialog dialog = new ReviewerSelectionDialog(parent.getShell(), crucibleReview,
						CrucibleUiUtil.getCachedUsers(crucibleReview));
				if (dialog.open() == Window.OK) {
					Set<Reviewer> reviewers = dialog.getSelectedReviewers();
					disposeReviewersPart();
					createReviewersPart(toolkit, parent, reviewers);
					crucibleEditor.reflow(true);
				}
			}
		};
		setReviewersAction.setToolTipText("Add/Remove Reviewers");
		setReviewersAction.setImageDescriptor(CrucibleImages.SET_REVIEWERS);

		ImageHyperlink hyperlink = toolkit.createImageHyperlink(parent, SWT.NONE);
		if (setReviewersAction.getImageDescriptor() != null) {
			hyperlink.setImage(AtlassianImages.getImage(setReviewersAction.getImageDescriptor()));
		} else {
			hyperlink.setText(setReviewersAction.getText());
		}
		hyperlink.setToolTipText(setReviewersAction.getToolTipText());
		hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				setReviewersAction.run();
			}
		});
	}
}
