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

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleRemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleUserLabelProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.dialogs.ReviewerSelectionDialog;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewChangeJob;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import java.util.Collection;
import java.util.Set;

/**
 * The form part that displays the details of the review
 * 
 * @author Shawn Minto
 */
public class CrucibleParticipantsPart extends AbstractCrucibleEditorFormPart {

	private final class SetReviewersAction extends Action {

		private final JobChangeAdapter jobChangeAdapter;

		public SetReviewersAction() {
			this(null);
		}

		public SetReviewersAction(JobChangeAdapter jobChangeAdapter) {
			this.jobChangeAdapter = jobChangeAdapter;
		}

		@Override
		public void run() {
			Collection<User> allowedReviewers = CrucibleUiUtil.getAllowedReviewers(
					CrucibleUiUtil.getCrucibleTaskRepository(crucibleReview),
					crucibleReview.getProjectKey());

			if (allowedReviewers == null) {
				final TaskRepository taskRepository = CrucibleUiUtil.getCrucibleTaskRepository(crucibleReview);
				boolean isSuccess = CrucibleUiUtil.updateProjectDetailsCache(taskRepository, crucibleReview.getProjectKey(),
						new ProgressMonitorDialog(WorkbenchUtil.getShell()));

				if (!isSuccess) {
					MessageDialog.openError(WorkbenchUtil.getShell(), "Problem",
							"Cannot fetch project details with allowed reviewers.\n"
									+ "See Error Log for details");
					return;
				}

				allowedReviewers = CrucibleUiUtil.getAllowedReviewers(
						CrucibleUiUtil.getCrucibleTaskRepository(crucibleReview),
						crucibleReview.getProjectKey());
				if (allowedReviewers == null) {
					MessageDialog.openError(WorkbenchUtil.getShell(), "Problem",
							"Cannot determine allowed reviewers for this review");
					return;
				}
			}

			ReviewerSelectionDialog dialog = new ReviewerSelectionDialog(WorkbenchUtil.getShell(), crucibleReview,
					allowedReviewers);
			if (dialog.open() == Window.OK) {
				Set<User> reviewers = dialog.getSelectedReviewers();
				final Set<String> reviewerUserNames = CrucibleUiUtil.getUsernamesFromUsers(reviewers);

				boolean unchanged = reviewers.size() == crucibleReview.getReviewers().size()
						&& reviewers.containsAll(crucibleReview.getReviewers());
				if (unchanged) {
					changedAttributes.remove(ReviewAttributeType.REVIEWERS);
				} else {
					changedAttributes.put(ReviewAttributeType.REVIEWERS, reviewers);
				}
				final TaskRepository repository = CrucibleUiUtil.getCrucibleTaskRepository(crucibleReview);
				CrucibleReviewChangeJob job = new CrucibleReviewChangeJob("Set Reviewers", repository) {
					@Override
					protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {
						final MultiStatus status = new MultiStatus(CrucibleUiPlugin.PLUGIN_ID, IStatus.ERROR,
								"Failed to set reviewers", null);
						client.execute(new CrucibleRemoteOperation<Object>(monitor, repository) {
							@Override
							public Object run(CrucibleServerFacade2 server, ConnectionCfg serverCfg,
									IProgressMonitor monitor) throws CrucibleLoginException, RemoteApiException,
									ServerPasswordNotProvidedException {
								try {
									server.setReviewers(serverCfg, crucibleReview.getPermId(), reviewerUserNames);
								} catch (Exception e) {
									status.add(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
											"Failed to set reviewers.", e));
								}
								return null;
							}
						});
						crucibleReview = client.getReview(repository, CrucibleUtil.getTaskIdFromReview(crucibleReview),
								true, monitor);
						if (status.getChildren().length > 0) {
							StatusHandler.log(status);
							crucibleEditor.getEditor()
									.setMessage("Error while setting reviewers. See Error Log for details.",
											IMessageProvider.ERROR);
						}
						return Status.OK_STATUS;
					}
				};
				job.schedule();
				if (jobChangeAdapter != null) {
					job.addJobChangeListener(jobChangeAdapter);
				}
			}
		}
	}

	private Review crucibleReview;

	private CrucibleReviewEditorPage crucibleEditor;

	private Composite parentComposite;

	private Section reviewersSection;

	private IAction setReviewersAction;

	private boolean newReview;

	private Control reviewersPart;

	@Override
	public void initialize(CrucibleReviewEditorPage editor, Review review, boolean isNewReview) {
		this.crucibleReview = review;
		this.crucibleEditor = editor;
		this.newReview = isNewReview;
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
		// CHECKSTYLE:MAGIC:OFF

		parentComposite = new Composite(parent, SWT.NONE);
		toolkit.adapt(parentComposite);
		parentComposite.setLayout(GridLayoutFactory.fillDefaults()
				.spacing(10, 10)
				.equalWidth(true)
				.numColumns(1)
				.create());
		// CHECKSTYLE:MAGIC:ON
		GridDataFactory.fillDefaults().grab(true, false).applyTo(parentComposite);

		updateControl(this.crucibleReview, parent, toolkit);

		return parentComposite;
	}

	private Label createLabelControl(FormToolkit toolkit, Composite parent, String labelString) {
		Label labelControl = toolkit.createLabel(parent, labelString);
		labelControl.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		return labelControl;
	}

	// this was introduced by Thomas but it's not used as remote API does not allow to edit the author and moderator
	private Control createUserComboControl(FormToolkit toolkit, Composite parent, String labelString,
			final User selectedUser, boolean readOnly, final ReviewAttributeType reviewAttributeType) {
		if (labelString != null) {
			createLabelControl(toolkit, parent, labelString);
		}

		// @fixme allowed reviewers should be respected here
		Set<User> users = CrucibleUiUtil.getCachedUsers(crucibleReview);

		Control control;
		if (readOnly) {
			final Text text = new Text(parent, SWT.READ_ONLY);
			text.setFont(JFaceResources.getTextFont());
			text.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
			text.setText(selectedUser.getDisplayName());
			text.setData(selectedUser);
			text.setEditable(false);
			control = text;
			toolkit.adapt(text, true, true);
		} else {
			final CCombo combo = new CCombo(parent, SWT.BORDER);
			combo.setEditable(false);
			ComboViewer comboViewer = new ComboViewer(combo);
			comboViewer.setLabelProvider(new CrucibleUserLabelProvider());
			comboViewer.setContentProvider(new ArrayContentProvider());
			comboViewer.setInput(users);
			comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					ISelection selection = event.getSelection();
					if (selection instanceof IStructuredSelection) {
						User user = ((User) ((IStructuredSelection) selection).getFirstElement());
						if (user.getUsername().equals(selectedUser.getUsername())) {
							changedAttributes.remove(reviewAttributeType);
						} else {
							changedAttributes.put(reviewAttributeType, user);
						}
						crucibleEditor.attributesModified();
					}
				}
			});

			comboViewer.setSelection(new StructuredSelection(selectedUser));
			control = combo;
		}
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
			// TODO disposing not necessary, simply updating labels and a re-layout should be sufficient; low priority though
		}
		parentComposite.setMenu(null);

		reviewersSection = toolkit.createSection(parentComposite, ExpandableComposite.TWISTIE
				| ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(reviewersSection);
		reviewersSection.setText("Participants");

		setSection(toolkit, reviewersSection);

		final Composite participantsComp = toolkit.createComposite(reviewersSection);

		participantsComp.setLayout(GridLayoutFactory.fillDefaults().margins(2, 2).numColumns(2).create());

		final ImageRegistry imageRegistry = new ImageRegistry();
		participantsComp.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				imageRegistry.dispose();
			}
		});

		CrucibleParticipantUiUtil.createLabel(toolkit, participantsComp, "Author:");

		final Composite authorComposite = CrucibleParticipantUiUtil.createParticipantComposite(toolkit, participantsComp,
				crucibleReview.getAuthor(), false, false, imageRegistry);
		authorComposite.setMenu(parent.getMenu());

		if (crucibleReview.getModerator() != null) {
			CrucibleParticipantUiUtil.createLabel(toolkit, participantsComp, "Moderator:");
			final Composite moderatorComposite = CrucibleParticipantUiUtil.createParticipantComposite(toolkit,
					participantsComp, crucibleReview.getModerator(), false, false, imageRegistry);
			moderatorComposite.setMenu(parent.getMenu());
		}

		CrucibleParticipantUiUtil.createLabel(toolkit, participantsComp, "Reviewers:");
		final Set<Reviewer> reviewers = crucibleReview.getReviewers();
		reviewersPart = CrucibleParticipantUiUtil.createReviewersListComposite(toolkit, participantsComp, reviewers,
				imageRegistry, participantsComp.getMenu());

		reviewersSection.setClient(participantsComp);

		toolkit.paintBordersFor(parentComposite);
	}

	@Override
	public void dispose() {
		if (reviewersPart != null && !reviewersPart.isDisposed()) {
			if (reviewersPart instanceof Composite) {
				CommonUiUtil.setMenu((Composite) reviewersPart, null);
			}
			reviewersPart.dispose();
		}
		super.dispose();
	}

	@Override
	public void setFocus() {
		reviewersSection.setFocus();
	}

	@Override
	protected void fillToolBar(ToolBarManager toolBarManager) {
		boolean hasModifyFilesAction = false;
		Set<CrucibleAction> actions = crucibleReview.getActions();
		if (actions != null && actions.contains(CrucibleAction.MODIFY_FILES)) {
			hasModifyFilesAction = true;
		}

		if (hasModifyFilesAction) {
			setReviewersAction = new SetReviewersAction();
			setReviewersAction.setToolTipText("Add/Remove Reviewers");
			setReviewersAction.setImageDescriptor(CrucibleImages.SET_REVIEWERS);

			toolBarManager.add(setReviewersAction);
		}

	}

}
