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

package com.atlassian.connector.eclipse.internal.crucible.ui.views;

import com.atlassian.connector.commons.misc.IntRanges;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleConstants;
import com.atlassian.connector.eclipse.internal.crucible.ui.ActiveReviewManager;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.ActiveReviewManager.IReviewActivationListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.AvatarImages.AvatarSize;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.EditActiveTaskAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.EditCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.PostDraftCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.RemoveCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.ReplyToCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.ToggleCommentsLeaveUnreadAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.MarkCommentsReadJob;
import com.atlassian.connector.eclipse.internal.crucible.ui.util.CommentUiUtil;
import com.atlassian.connector.eclipse.ui.PartListenerAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.Comment.ReadState;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;
import com.atlassian.theplugin.commons.util.MiscUtil;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonFonts;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.jetbrains.annotations.Nullable;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Map;

@SuppressWarnings("restriction")
public class CommentView extends ViewPart implements ISelectionChangedListener, ISelectionListener,
		IReviewActivationListener {

	private static final String NO_COMMENT_SELECTED = "No comment was selected in Crucible Review Explorer.";

	private EditCommentAction editCommentAction;

	private ReplyToCommentAction replyToCommentAction;

	private RemoveCommentAction removeCommentAction;

	private PostDraftCommentAction postDraftCommentAction;

	private Object currentSelection;

	private Label author;

	private Label authorAvatar;

	private Label date;

	private Label defect;

	private FormToolkit toolkit;

	private StackLayout stackLayout;

	private Composite linkComposite;

	private Composite detailsComposite;

	private Composite stackComposite;

	private Label draft;

	private Label defectClassification;

	private Label defectRank;

	private Composite header;

	private Label draftIcon;

	private Label defectIcon;

	private Label readState;

	private EditActiveTaskAction openEditorAction;

	private final Collection<IReviewActivationListener> reviewActivationListeners = MiscUtil.buildHashSet();

	public static final String ID = "com.atlassian.connector.eclipse.crucible.ui.commentView";

	private final IPartListener2 linkWithReviewExplorerListener = new PartListenerAdapter() {
		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
			if (partRef.getPart(true) instanceof ReviewExplorerView) {
				((ReviewExplorerView) partRef.getPart(true)).getViewer().addSelectionChangedListener(CommentView.this);
			}
		}
	};

	private final IPartListener2 markCommentAsReadListener = new PartListenerAdapter() {
		public void partHidden(IWorkbenchPartReference partRef) {
			if (partRef.getPart(true) instanceof CommentView) {
				cancelMarkCommentAsReadJob();
			}
		};

		public void partVisible(IWorkbenchPartReference partRef) {
			if (partRef.getPart(true) instanceof CommentView) {
				if (currentSelection instanceof Comment) {
					runMarkCommentAsReadJob((Comment) currentSelection);
				}
			}
		};
	};

	private Label revisions;

	private RichTextEditor editor;

	private ITask task;

	private Job markAsReadJob;

	private Composite scrolledComposite;

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);

		final ActiveReviewManager mgr = CrucibleUiPlugin.getDefault().getActiveReviewManager();
		if (mgr.isReviewActive()) {
			reviewActivated(mgr.getActiveTask(), mgr.getActiveReview());
		}
		mgr.addReviewActivationListener(this);

		getSite().getPage().addPartListener(markCommentAsReadListener);
	}

	@Override
	public void dispose() {
		cancelMarkCommentAsReadJob();

		ActiveReviewManager mgr = CrucibleUiPlugin.getDefault().getActiveReviewManager();

		for (IReviewActivationListener listener : reviewActivationListeners) {
			mgr.removeReviewActivationListener(listener);
		}

		CrucibleUiPlugin.getDefault().getActiveReviewManager().removeReviewActivationListener(this);

		ReviewExplorerView explorerView = findReviewExplorerView();
		if (explorerView != null) {
			explorerView.getViewer().removeSelectionChangedListener(this);
		}

		getSite().getPage().removePartListener(linkWithReviewExplorerListener);
		getSite().getPage().removePartListener(markCommentAsReadListener);

		if (toolkit != null) {
			toolkit.dispose();
			toolkit = null;
		}

		super.dispose();
	}

	private Label createLabelControl(FormToolkit toolkit, Composite parent, String labelString) {
		Label labelControl = toolkit.createLabel(parent, labelString);
		labelControl.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		return labelControl;
	}

	@Override
	public void createPartControl(Composite ancestor) {
		if (toolkit == null) {
			toolkit = new FormToolkit(ancestor.getDisplay());
		}

		stackComposite = toolkit.createComposite(ancestor);
		stackLayout = new StackLayout();
		stackComposite.setLayout(stackLayout);

		linkComposite = toolkit.createComposite(stackComposite);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(15, 15).applyTo(linkComposite);
		toolkit.createLabel(linkComposite, NO_COMMENT_SELECTED);

		detailsComposite = createDetailsComposite(stackComposite);

		stackLayout.topControl = linkComposite;
		stackComposite.layout();

		createActions();
		createToolbar();
		createMenu();

		final ReviewExplorerView reviewExplorerView = findReviewExplorerView();
		if (reviewExplorerView != null) {
			selectionChanged(reviewExplorerView, reviewExplorerView.getViewer().getSelection());
			reviewExplorerView.getViewer().addSelectionChangedListener(this);
		} else {
			updateViewer();
		}

		getSite().getPage().addPartListener(linkWithReviewExplorerListener);
	}

	private Composite createDetailsComposite(Composite stackComposite) {
		Composite detailsComposite = toolkit.createComposite(stackComposite);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(15, 15).applyTo(detailsComposite);

		// Avatar | Author | Date | Revisions | Draft | Defect | Defect type
		// Comment text here

		header = toolkit.createComposite(detailsComposite);
		GridLayoutFactory.fillDefaults().numColumns(15).applyTo(header);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(header);

		authorAvatar = toolkit.createLabel(header, "", SWT.READ_ONLY | SWT.SINGLE);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(authorAvatar);
		author = toolkit.createLabel(header, "", SWT.READ_ONLY | SWT.SINGLE);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(author);

		createLabelControl(toolkit, header, "Created:");
		date = toolkit.createLabel(header, "", SWT.READ_ONLY | SWT.SINGLE);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(date);

		readState = createLabelControl(toolkit, header, "");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(readState);

		draftIcon = toolkit.createLabel(header, "", SWT.READ_ONLY | SWT.SINGLE);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).hint(15, SWT.DEFAULT).applyTo(draftIcon);

		draft = toolkit.createLabel(header, "", SWT.READ_ONLY | SWT.SINGLE);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(draft);

		defectIcon = toolkit.createLabel(header, "", SWT.READ_ONLY | SWT.SINGLE);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).hint(15, SWT.DEFAULT).applyTo(defectIcon);

		defect = toolkit.createLabel(header, "", SWT.READ_ONLY | SWT.SINGLE);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(defect);

		defectRank = toolkit.createLabel(header, "", SWT.READ_ONLY | SWT.SINGLE);
		defectRank.setToolTipText("Defect Rank");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(defectRank);

		defectClassification = toolkit.createLabel(header, "", SWT.READ_ONLY | SWT.SINGLE);
		defectClassification.setToolTipText("Defect Classification");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(defectClassification);

		createLabelControl(toolkit, header, "Applies to revisions:");
		revisions = toolkit.createLabel(header, "", SWT.READ_ONLY | SWT.SINGLE);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(revisions);

		return detailsComposite;
	}

	private void createToolbar() {
		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		mgr.add(openEditorAction);
		mgr.add(new Separator());
		mgr.add(replyToCommentAction);
		mgr.add(editCommentAction);
		mgr.add(removeCommentAction);
		mgr.add(postDraftCommentAction);
	}

	private void createMenu() {
		IMenuManager mgr = getViewSite().getActionBars().getMenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillMenu(manager);
			}
		});
		mgr.add(new Separator());
	}

	private void fillMenu(IMenuManager manager) {
		ToggleCommentsLeaveUnreadAction action = new ToggleCommentsLeaveUnreadAction();
		action.selectionChanged(currentSelection != null ? new StructuredSelection(currentSelection)
				: StructuredSelection.EMPTY);
		manager.add(action);
	}

	private void createActions() {
		openEditorAction = new EditActiveTaskAction();
		reviewActivationListeners.add(openEditorAction);

		replyToCommentAction = new ReplyToCommentAction();
		editCommentAction = new EditCommentAction();
		removeCommentAction = new RemoveCommentAction();
		postDraftCommentAction = new PostDraftCommentAction();

		// in the end register all additional activation listeners
		final ActiveReviewManager mgr = CrucibleUiPlugin.getDefault().getActiveReviewManager();
		for (IReviewActivationListener listener : reviewActivationListeners) {
			if (mgr.isReviewActive()) {
				listener.reviewActivated(mgr.getActiveTask(), mgr.getActiveReview());
			}
			mgr.addReviewActivationListener(listener);
		}

	}

	@Override
	public void setFocus() {
		if (editor != null) {
			editor.getControl().setFocus();
		}
	}

	@Nullable
	private ReviewExplorerView findReviewExplorerView() {
		final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null) {
			return null;
		}
		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		if (activePage == null) {
			return null;
		}
		for (IViewReference view : activePage.getViewReferences()) {
			if (view.getId().equals(ReviewExplorerView.ID) && view.getView(false) instanceof ReviewExplorerView) {
				return (ReviewExplorerView) view.getView(false);
			}
		}
		return null;
	}

	/**
	 * Here we listen to changes in {@link ReviewExplorerView}
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!(part instanceof ReviewExplorerView) && part != this) {
			return;
		}

		if (selection instanceof IStructuredSelection) {
			replyToCommentAction.selectionChanged((IStructuredSelection) selection);
			editCommentAction.selectionChanged((IStructuredSelection) selection);
			removeCommentAction.selectionChanged((IStructuredSelection) selection);
			postDraftCommentAction.selectionChanged((IStructuredSelection) selection);
		} else {
			replyToCommentAction.selectionChanged(StructuredSelection.EMPTY);
			editCommentAction.selectionChanged(StructuredSelection.EMPTY);
			removeCommentAction.selectionChanged(StructuredSelection.EMPTY);
			postDraftCommentAction.selectionChanged(StructuredSelection.EMPTY);
		}

		currentSelection = null;
		if (selection instanceof IStructuredSelection) {
			currentSelection = ((IStructuredSelection) selection).getFirstElement();
		}

		cancelMarkCommentAsReadJob();
		updateViewer();
	}

	private synchronized void cancelMarkCommentAsReadJob() {
		if (markAsReadJob != null) {
			markAsReadJob.cancel();
			markAsReadJob = null;
		}
	}

	private void updateViewer() {

		if (currentSelection instanceof Comment) {
			Comment activeComment = (Comment) currentSelection;

			if (activeComment != null) {
				if (activeComment.getReadState().equals(Comment.ReadState.READ)) {
					readState.setText("Read");
					readState.setFont(null);
				} else if (activeComment.getReadState().equals(Comment.ReadState.UNREAD)
						|| activeComment.getReadState().equals(Comment.ReadState.LEAVE_UNREAD)) {
					readState.setText("Unread");
					readState.setFont(CommonFonts.BOLD);
					runMarkCommentAsReadJob(activeComment);
				} else {
					readState.setText("");
					readState.setFont(null);
				}

				if (activeComment.isDraft()) {
					draftIcon.setImage(CrucibleImages.getImage(CrucibleImages.COMMENT_EDIT));
					draft.setText("Draft");
				} else {
					draftIcon.setImage(null);
					draft.setText("");
				}

				if (activeComment.isDefectRaised()) {
					defectIcon.setImage(CommonImages.getImage(CommonImages.PRIORITY_1));
					defect.setText("Defect");
				} else {
					defectIcon.setImage(null);
					defect.setText("");
				}

				defectClassification.setText("");
				defectRank.setText("");

				Map<String, CustomField> fields = activeComment.getCustomFields();
				if (fields != null) {
					if (fields.containsKey(CrucibleConstants.CLASSIFICATION_CUSTOM_FIELD_KEY)) {
						defectClassification.setText(fields.get(CrucibleConstants.CLASSIFICATION_CUSTOM_FIELD_KEY)
								.getValue());
					}

					if (fields.containsKey(CrucibleConstants.RANK_CUSTOM_FIELD_KEY)) {
						defectRank.setText(fields.get(CrucibleConstants.RANK_CUSTOM_FIELD_KEY).getValue());
					}
				}

				author.setText(activeComment.getAuthor().getDisplayName());
				author.setToolTipText(activeComment.getAuthor().getUsername());

				authorAvatar.setImage(CrucibleUiPlugin.getDefault().getAvatarsCache().getAvatarOrDefaultImage(
						activeComment.getAuthor(), AvatarSize.LARGE));

				date.setText(DateFormat.getDateInstance().format(activeComment.getCreateDate()));

				if (activeComment instanceof VersionedComment) {
					final Map<String, IntRanges> lines = ((VersionedComment) activeComment).getLineRanges();
					revisions.setText(lines != null ? StringUtils.join(lines.keySet(), ", ") : "none");
				} else {
					revisions.setText("none");
				}

				// fix for PLE-1011 - if RichTextEditor is on a visible composite it will grab focus
				// so we hide the composite for which we add RTE so it doesn't grab the focus
				stackLayout.topControl = linkComposite;
				stackComposite.layout();

				setCommentText(activeComment);

				header.layout();

				stackLayout.topControl = detailsComposite;
				stackComposite.layout();
				return;
			}
		}

		stackLayout.topControl = linkComposite;
		stackComposite.layout();
	}

	private synchronized void runMarkCommentAsReadJob(Comment activeComment) {
		if (getSite().getWorkbenchWindow().getActivePage().isPartVisible(this)) {
			if (activeComment.getReadState().equals(ReadState.UNREAD)) {
				cancelMarkCommentAsReadJob();
				markAsReadJob = new MarkCommentsReadJob(activeComment.getReview(),
						MiscUtil.buildArrayList(activeComment), true);
				markAsReadJob.schedule(MarkCommentsReadJob.DEFAULT_DELAY_INTERVAL);
			}
		}
	}

	private ScrolledComposite createScrolledWikiTextComment(FormToolkit toolkit, final Comment comment,
			final Composite parent) {
		final ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL);

		final Composite scrolledContent = new Composite(sc, SWT.NONE);
		sc.setContent(scrolledContent);
		toolkit.adapt(sc);
		scrolledContent.setLayout(new FillLayout());
		editor = CommentUiUtil.createWikiTextControl(toolkit, scrolledContent, comment);

		// these incantations make wrapped component (WikiText here) respecting resizing actions
		// and wrap if needed
		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);
		sc.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Rectangle r = sc.getClientArea();
				sc.setMinSize(scrolledContent.computeSize(r.width,
						SWT.DEFAULT));
			}
		});
		// end of incantations
		// this call instead did not cause the correct wrapping (longer texts were just cut)
		// scrolledContent.setSize(scrolledContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return sc;
	}

	private void setCommentText(Comment comment) {
		TaskRepository repository = TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(),
				task.getRepositoryUrl());

		if (toolkit != null) {
			// TaskEditorExtensions.setTaskEditorExtensionId(repository,
			// AtlassianUiUtil.CONFLUENCE_WIKI_TASK_EDITOR_EXTENSION);
			// AbstractTaskEditorExtension extension = TaskEditorExtensions.getTaskEditorExtension(repository);
			if (scrolledComposite != null) {
				scrolledComposite.dispose();
				scrolledComposite = null;
			}

			// scrolledComposite = new Composite(detailsComposite, SWT.NONE);
			// scrolledComposite.setLayout(new FillLayout());
			// final Label label = new Label(detailsComposite, SWT.WRAP);
			// label.setText(comment.getMessage());
			// GridDataFactory.fillDefaults().grab(true, true).applyTo(label);

			scrolledComposite = createScrolledWikiTextComment(toolkit, comment, detailsComposite);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(scrolledComposite);
			// editor = new RichTextEditor(repository, SWT.MULTI | SWT.BORDER, null, extension);
			// if (extension != null) {
			// editor.setReadOnly(false);
			// } else {
			// editor.setReadOnly(true);
			// }
			// editor.setText(comment.getMessage());
			// editor.createControl(detailsComposite, toolkit);
			// editor.showPreview();
			// editor.getViewer().getTextWidget().setBackground(null);

			// GridDataFactory.fillDefaults().grab(true, true).applyTo(editor.getControl());

			detailsComposite.layout();
		}
	}

	public void reviewActivated(ITask aTask, Review aReview) {
		this.task = aTask;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				updateViewer();
			}
		});
	}

	public void reviewDeactivated(ITask aTask, Review aReview) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				stackLayout.topControl = linkComposite;
				stackComposite.layout();
			}
		});
	}

	public void reviewUpdated(ITask aTask, Review aReview, Collection<CrucibleNotification> differences) {
		reviewActivated(aTask, aReview);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		selectionChanged(this, event.getSelection());
	}

}
