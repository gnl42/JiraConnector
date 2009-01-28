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

package com.atlassian.connector.eclipse.internal.crucible.ui.editor;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient.RemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.core.client.model.IReviewCacheListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.PermIdBean;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PlatformUI;
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

	private final IReviewCacheListener reviewCacheListener = new IReviewCacheListener() {

		public void reviewAdded(String repositoryUrl, String taskId, Review addedReview) {
			// ignore
		}

		public void reviewUpdated(String repositoryUrl, String taskId, Review updatedReview,
				List<CrucibleNotification> differences) {

			if (getTask() != null) {
				ITask task = getTask();
				if (task.getRepositoryUrl().equals(repositoryUrl) && task.getTaskId().equals(taskId)) {
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							getEditor().setMessage("Review has incoming changes", IMessageProvider.WARNING,
									new HyperlinkAdapter() {
										@Override
										public void linkActivated(HyperlinkEvent e) {
											downloadReviewAndRefresh(0, false);
										}
									});
						}
					});
				}
			}
		}

	};

	private static final int OPEN_DOWNLOAD_DELAY = 0;

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
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);

		CrucibleCorePlugin.getDefault().getReviewCache().addCacheChangedListener(reviewCacheListener);
	}

	@Override
	public void dispose() {
		CrucibleCorePlugin.getDefault().getReviewCache().removeCacheChangedListener(reviewCacheListener);
		super.dispose();
		editorComposite = null;
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
					downloadReviewAndRefresh(OPEN_DOWNLOAD_DELAY, false);
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

	TaskRepository getTaskRepository() {
		return getEditor().getTaskEditorInput().getTaskRepository();
	}

	public ITask getTask() {
		return getEditor().getTaskEditorInput().getTask();
	}

	private void downloadReviewAndRefresh(long delay, final boolean force) {

		CrucibleReviewChangeJob job = new CrucibleReviewChangeJob("Retrieving Crucible Review "
				+ getTask().getTaskKey(), getTaskRepository()) {
			@Override
			protected IStatus execute(CrucibleClient client, IProgressMonitor monitor) throws CoreException {

				// TODO clean this up
				Review cachedReview = CrucibleCorePlugin.getDefault().getReviewCache().getWorkingCopyReview(
						getTask().getRepositoryUrl(), getTask().getTaskId());

				if (cachedReview == null || force) {

					review = client.getReview(getTaskRepository(), getTask().getTaskId(), true, monitor);
					return new Status(IStatus.OK, CrucibleUiPlugin.PLUGIN_ID, null);
				} else {
					review = cachedReview;
					return new Status(IStatus.OK, CrucibleUiPlugin.PLUGIN_ID, null);
				}
			}
		};
		schedule(job, delay);

	}

	private void createFormContent() {
		if (editorComposite == null) {
			return;
		}

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
			EditorUtil.setMenu(editorComposite, editorComposite.getMenu());
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

		Menu menu = editorComposite.getMenu();
		// preserve context menu
		EditorUtil.setMenu(editorComposite, null);

		// remove all of the old widgets so that we can redraw the editor
		for (Control child : editorComposite.getChildren()) {
			child.dispose();
		}

		editorComposite.setMenu(menu);

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
							CrucibleReviewChangeJob job = new CrucibleReviewChangeJob("Summarizing Crucible Review "
									+ getTask().getTaskKey(), getTaskRepository()) {
								@Override
								protected IStatus execute(CrucibleClient client, IProgressMonitor monitor)
										throws CoreException {
									client.execute(new RemoteOperation<Review>(monitor) {
										@Override
										public Review run(CrucibleServerFacade server, CrucibleServerCfg serverCfg,
												IProgressMonitor monitor) throws CrucibleLoginException,
												RemoteApiException, ServerPasswordNotProvidedException {

											String permId = CrucibleUtil.getPermIdFromTaskId(getTask().getTaskId());
											return server.summarizeReview(serverCfg, new PermIdBean(permId));

										}
									});
									review = client.getReview(getTaskRepository(), getTask().getTaskId(), true, monitor);

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
							CrucibleReviewChangeJob job = new CrucibleReviewChangeJob("Reopen Crucible Review "
									+ getTask().getTaskKey(), getTaskRepository()) {
								@Override
								protected IStatus execute(CrucibleClient client, IProgressMonitor monitor)
										throws CoreException {
									client.execute(new RemoteOperation<Review>(monitor) {
										@Override
										public Review run(CrucibleServerFacade server, CrucibleServerCfg serverCfg,
												IProgressMonitor monitor) throws CrucibleLoginException,
												RemoteApiException, ServerPasswordNotProvidedException {
											String permId = CrucibleUtil.getPermIdFromTaskId(getTask().getTaskId());
											return server.reopenReview(serverCfg, new PermIdBean(permId));
										}
									});
									review = client.getReview(getTaskRepository(), getTask().getTaskId(), true, monitor);
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
							CrucibleReviewChangeJob job = new CrucibleReviewChangeJob("Abandon Crucible Review "
									+ getTask().getTaskKey(), getTaskRepository()) {
								@Override
								protected IStatus execute(CrucibleClient client, IProgressMonitor monitor)
										throws CoreException {
									client.execute(new RemoteOperation<Review>(monitor) {
										@Override
										public Review run(CrucibleServerFacade server, CrucibleServerCfg serverCfg,
												IProgressMonitor monitor) throws CrucibleLoginException,
												RemoteApiException, ServerPasswordNotProvidedException {
											String permId = CrucibleUtil.getPermIdFromTaskId(getTask().getTaskId());
											return server.abandonReview(serverCfg, new PermIdBean(permId));
										}
									});
									review = client.getReview(getTaskRepository(), getTask().getTaskId(), true, monitor);
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
				downloadReviewAndRefresh(0L, true);
			}
		};
		refreshAction.setImageDescriptor(CommonImages.REFRESH);
		refreshAction.setToolTipText("Refresh");
		manager.add(refreshAction);
	}

	private void schedule(final CrucibleReviewChangeJob job, long delay) {
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setBusy(false);

						// TODO setup the image descriptor properly too 

						IStatus status = job.getStatus();
						if (editorComposite != null) {
							if (status == null || review == null) {
								// TODO use the status?
								getEditor().setMessage("Unable to retrieve Review.  Click to try again.",
										IMessageProvider.WARNING, new HyperlinkAdapter() {
											@Override
											public void linkActivated(HyperlinkEvent e) {
												downloadReviewAndRefresh(0, true);
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
					}
				});
			}
		});
		job.schedule(delay);
		setBusy(true);
	};

}
