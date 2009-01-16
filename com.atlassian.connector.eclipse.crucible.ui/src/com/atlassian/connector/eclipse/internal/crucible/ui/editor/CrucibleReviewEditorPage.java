/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.ui.editor;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskFormPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import java.util.ArrayList;
import java.util.List;

/**
 * The rich editor for crucible reviews
 * 
 * @author Shawn Minto
 */
public class CrucibleReviewEditorPage extends TaskFormPage {

	/**
	 * Causes the form page to reflow on resize.
	 */
	private final class ParentResizeHandler implements Listener {
		private static final int REFLOW_TIMER_DELAY = 300;

		private int generation;

		public void handleEvent(Event event) {
			++generation;

			Display.getCurrent().timerExec(REFLOW_TIMER_DELAY, new Runnable() {
				private final int scheduledGeneration = generation;

				public void run() {
					if (getManagedForm().getForm().isDisposed()) {
						return;
					}

					// only reflow if this is the latest generation to prevent
					// unnecessary reflows while the form is being resized
					if (scheduledGeneration == generation) {
						getManagedForm().reflow(true);
					}
				}
			});
		}
	}

	private static final int OPEN_DOWNLOAD_DELAY = 200;

	private static final int VERTICAL_BAR_WIDTH = 15;

	private static final String CRUCIBLE_EDITOR_PAGE_ID = "com.atlassian.connector.eclipse.crucible.review.editor";

	private FormToolkit toolkit;

	private ScrolledForm form;

	private boolean reflow;

	protected Review review;

	private Composite editorComposite;

	private final List<AbstractCrucibleEditorFormPart> parts;

	public CrucibleReviewEditorPage(FormEditor editor, String title) {
		super(editor, CRUCIBLE_EDITOR_PAGE_ID, title);
		parts = new ArrayList<AbstractCrucibleEditorFormPart>();
	}

	@Override
	public TaskEditor getEditor() {
		return (TaskEditor) super.getEditor();
	}

	@Override
	protected void createFormContent(final IManagedForm managedForm) {

		form = managedForm.getForm();

		toolkit = managedForm.getToolkit();

		EditorUtil.disableScrollingOnFocus(form);

		try {
			setReflow(false);

			editorComposite = form.getBody();
			// TODO consider using TableWrapLayout, it makes resizing much faster
			GridLayout editorLayout = new GridLayout();
			editorComposite.setLayout(editorLayout);
			editorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

			editorComposite.setMenu(getEditor().getMenu());

			Display.getCurrent().asyncExec(new Runnable() {

				public void run() {
					downloadReviewAndRefresh(OPEN_DOWNLOAD_DELAY);
				}

			});

		} finally {
			setReflow(true);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.addListener(SWT.Resize, new ParentResizeHandler());
		super.createPartControl(parent);
	}

	private TaskRepository getTaskRepository() {
		return getEditor().getTaskEditorInput().getTaskRepository();
	}

	private ITask getTask() {
		return getEditor().getTaskEditorInput().getTask();
	}

	private void downloadReviewAndRefresh(long delay) {
		CrucibleReviewJob job = new CrucibleReviewJob("Retrieving Crucible Review " + getTask().getTaskKey()) {
			@Override
			protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {
				review = client.getCrucibleReview(getTask().getTaskId(), monitor);
				return new Status(IStatus.OK, CrucibleUiPlugin.PLUGIN_ID, null);
			}
		};
		schedule(job, delay);
	}

	private void createFormContent() {
		assert (editorComposite != null);
		assert (review != null);
		try {

			setReflow(false);
			clearFormContent();

			createFormParts();

			for (AbstractCrucibleEditorFormPart part : parts) {
				getManagedForm().addPart(part);
				part.initialize(this, review);
				part.createControl(editorComposite, toolkit);
			}

		} finally {
			setReflow(true);
			reflow();
		}

	}

	private void createFormParts() {
		parts.add(new CrucibleDetailsPart());
		parts.add(new CrucibleGeneralCommentsPart());
		parts.add(new CrucibleReviewFilesPart());
	}

	private void clearFormContent() {

		for (AbstractCrucibleEditorFormPart part : parts) {
			getManagedForm().removePart(part);
		}

		parts.clear();

		// preserve context menu
		EditorUtil.setMenu(editorComposite, null);

		// remove all of the old widgets so that we can redraw the editor
		for (Control child : editorComposite.getChildren()) {
			child.dispose();
		}

		// FIXME dispose parts
	}

	private void setBusy(boolean busy) {
		getEditor().showBusy(busy);
	}

	public void setReflow(boolean reflow) {
		this.reflow = reflow;
		form.setRedraw(reflow);
	}

	/**
	 * Force a re-layout of entire form.
	 */
	public void reflow() {
		if (reflow) {
			// help the layout managers: ensure that the form width always matches
			// the parent client area width.
			Rectangle parentClientArea = form.getParent().getClientArea();
			Point formSize = form.getSize();
			if (formSize.x != parentClientArea.width) {
				ScrollBar verticalBar = form.getVerticalBar();
				int verticalBarWidth = verticalBar != null ? verticalBar.getSize().x : VERTICAL_BAR_WIDTH;
				form.setSize(parentClientArea.width - verticalBarWidth, formSize.y);
			}

			form.layout(true, false);
			form.reflow(true);
		}
	}

	@Override
	protected void fillToolBar(IToolBarManager manager) {
		if (review != null) {
			try {
				List<com.atlassian.theplugin.commons.crucible.api.model.Action> transitions = review.getTransitions();
				if (transitions.contains(com.atlassian.theplugin.commons.crucible.api.model.Action.SUMMARIZE)) {
					Action summarizeAction = new Action() {
						@Override
						public void run() {
							CrucibleReviewJob job = new CrucibleReviewJob("Summarizing Crucible Review "
									+ getTask().getTaskKey()) {
								@Override
								protected IStatus execute(CrucibleClient client, IProgressMonitor monitor)
										throws CoreException {
									client.summarizeReview(getTask().getTaskId(), monitor);
									review = client.getCrucibleReview(getTask().getTaskId(), monitor);
									return new Status(IStatus.OK, CrucibleUiPlugin.PLUGIN_ID, "Review was summarized.");
								}
							};
							schedule(job, 0L);
						}
					};
					//summarizeAction.setImageDescriptor(CrucibleImages.SUMMARIZE);
					summarizeAction.setText("Summarize");
					summarizeAction.setToolTipText("Summarize review");
					manager.add(summarizeAction);
				}
				if (transitions.contains(com.atlassian.theplugin.commons.crucible.api.model.Action.REOPEN)) {
					Action abandonAction = new Action() {
						@Override
						public void run() {
							CrucibleReviewJob job = new CrucibleReviewJob("Reopen Crucible Review "
									+ getTask().getTaskKey()) {
								@Override
								protected IStatus execute(CrucibleClient client, IProgressMonitor monitor)
										throws CoreException {
									client.reopenReview(getTask().getTaskId(), monitor);
									review = client.getCrucibleReview(getTask().getTaskId(), monitor);
									return new Status(IStatus.OK, CrucibleUiPlugin.PLUGIN_ID, "Review was reopened.");
								}
							};
							schedule(job, 0L);
						}
					};
					abandonAction.setText("Reopen");
					//abandonAction.setImageDescriptor(CrucibleImages.REOPEN);
					abandonAction.setToolTipText("Reopen review");
					manager.add(abandonAction);
				}
				if (transitions.contains(com.atlassian.theplugin.commons.crucible.api.model.Action.ABANDON)) {
					Action abandonAction = new Action() {
						@Override
						public void run() {
							CrucibleReviewJob job = new CrucibleReviewJob("Abandon Crucible Review "
									+ getTask().getTaskKey()) {
								@Override
								protected IStatus execute(CrucibleClient client, IProgressMonitor monitor)
										throws CoreException {
									client.abondonReview(getTask().getTaskId(), monitor);
									review = client.getCrucibleReview(getTask().getTaskId(), monitor);
									return new Status(IStatus.OK, CrucibleUiPlugin.PLUGIN_ID, "Review was abandoned.");
								}
							};
							schedule(job, 0L);
						}
					};
					abandonAction.setText("Abandon");
					//abandonAction.setImageDescriptor(CrucibleImages.ABANDON);
					abandonAction.setToolTipText("Abandon review");
					manager.add(abandonAction);
				}
			} catch (ValueNotYetInitialized e) {
				StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, "Unexpected error", e));
			}
		}

		Action refreshAction = new Action() {
			@Override
			public void run() {
				downloadReviewAndRefresh(0L);
			}
		};
		refreshAction.setImageDescriptor(CommonImages.REFRESH);
		refreshAction.setToolTipText("Refresh");
		manager.add(refreshAction);
	}

	private void schedule(final CrucibleReviewJob job, long delay) {
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setBusy(false);

						// TODO setup the image descriptor properly too 

						IStatus status = job.getStatus();
						if (status == null || review == null) {
							// TODO use the status?
							getEditor().setMessage("Unable to retrieve Review.  Click to try again.",
									IMessageProvider.WARNING, new HyperlinkAdapter() {
										@Override
										public void linkActivated(HyperlinkEvent e) {
											downloadReviewAndRefresh(0);
										}
									});
						} else if (status.isOK()) {
							// TODO use the status?
							getEditor().setMessage(status.getMessage(), IMessageProvider.NONE, null);
							createFormContent();
							getEditor().updateHeaderToolBar();
							TasksUiPlugin.getTaskDataManager().setTaskRead(getTask(), true);
						} else {
							// TODO improve the message?
							getEditor().setMessage(status.getMessage(), IMessageProvider.ERROR, null);
						}
					}
				});
			}
		});
		job.schedule(delay);
		setBusy(true);
	}

	private abstract class CrucibleReviewJob extends Job {

		private IStatus status;

		protected CrucibleReviewJob(String name) {
			super(name);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			setStatus(Status.CANCEL_STATUS);
			CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
			CrucibleClient client = connector.getClientManager().getClient(getTaskRepository());
			if (client == null) {
				return new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
						"Unable to get client, please try to refresh");
			}
			try {
				IStatus result = execute(client, monitor);
				setStatus(result);
			} catch (CoreException e) {
				setStatus(e.getStatus());
			}
			return Status.OK_STATUS;
		}

		protected abstract IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException;

		public IStatus getStatus() {
			return status;
		}

		protected void setStatus(IStatus status) {
			this.status = status;
		}

	};

}
