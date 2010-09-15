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

import com.atlassian.connector.commons.crucible.api.model.ReviewModelUtil;
import com.atlassian.connector.commons.misc.IntRanges;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.DownloadAvatarsJob;
import com.atlassian.connector.eclipse.internal.crucible.ui.ActiveReviewManager;
import com.atlassian.connector.eclipse.internal.crucible.ui.AvatarImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.ICrucibleFileProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.ActiveReviewManager.IReviewActivationListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.AvatarImages.AvatarSize;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.ActiveReviewCompletnesSwitcherAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.AddChangesetToActiveReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.AddFileCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.AddGeneralCommentToActiveReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.AddPatchToActiveReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.CommentNavigationAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.CompareVirtualFilesAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.EditActiveTaskAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.EditCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.OpenVirtualFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.PostDraftCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.PublishAllDraftCommentsAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.RefreshActiveReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.RemoveCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.ReplyToCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.ToggleCommentsLeaveUnreadAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.CrucibleFileInfoCompareEditorInput;
import com.atlassian.connector.eclipse.internal.crucible.ui.util.EditorUtil;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.connector.eclipse.ui.AtlassianImages;
import com.atlassian.connector.eclipse.ui.OpenAndLinkWithEditorHelper;
import com.atlassian.connector.eclipse.ui.PartListenerAdapter;
import com.atlassian.connector.eclipse.ui.commons.CustomToolTip;
import com.atlassian.connector.eclipse.ui.util.SelectionUtil;
import com.atlassian.connector.eclipse.ui.viewers.CollapseAllAction;
import com.atlassian.connector.eclipse.ui.viewers.ExpandAllAction;
import com.atlassian.connector.eclipse.ui.viewers.ExpandCollapseSelectionAction;
import com.atlassian.connector.eclipse.ui.viewers.TreeViewerUtil;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.notification.AbstractCommentNotification;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;
import com.atlassian.theplugin.commons.crucible.api.model.notification.NewCommentNotification;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.commons.util.StringUtil;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Pawel Niewiadomski
 */
public class ReviewExplorerView extends ViewPart implements IReviewActivationListener {

	private static final class GeneralCommentsNode extends ReviewTreeNode {
		private Review newReview;

		private GeneralCommentsNode(ReviewTreeNode parent, String pathToken, int category, Review newReview) {
			super(parent, pathToken, category);
			this.newReview = newReview;
		}

		@Override
		public List<Object> getChildren() {
			return Collections.<Object> unmodifiableList(newReview.getGeneralComments());
		}

		public void setReview(Review review) {
			this.newReview = review;
		}
	}

	private final class TextSelectionToReviewItemListener implements ISelectionListener {
		private boolean focusMatchingComment(CrucibleFileInfo fileInfo, String revision, int start) {
			for (VersionedComment comment : fileInfo.getVersionedComments()) {
				Map<String, IntRanges> commentRanges = comment.getLineRanges();
				if (commentRanges != null && commentRanges.containsKey(revision)) {
					IntRanges ranges = comment.getLineRanges().get(revision);
					if (ranges.getTotalMin() <= start && start <= ranges.getTotalMax()) {
						if (!inputIsSelected(comment)) {
							showInput(comment);
						} else {
							viewer.getTree().showSelection();
						}
						return true;
					}
				}
			}
			return false;
		}

		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof TextSelection && isLinkingEnabled() && part instanceof IEditorPart) {
				IEditorInput editorInput = ((IEditorPart) part).getEditorInput();
				TextSelection textSelection = (TextSelection) selection;
				int start = textSelection.getStartLine() + 1; // lines are counted from 0, but Crucible counts them from 1

				if (editorInput instanceof CrucibleFileInfoCompareEditorInput) {
					CrucibleFileInfo fileInfo = ((CrucibleFileInfoCompareEditorInput) editorInput).getCrucibleFileInfo();
					fileInfo = review.getFileByPermId(fileInfo.getPermId());

					if (focusMatchingComment(fileInfo, fileInfo.getOldFileDescriptor().getRevision(), start)
							|| focusMatchingComment(fileInfo, fileInfo.getFileDescriptor().getRevision(), start)) {
						return;
					}
				}

				if (editorInput instanceof ICrucibleFileProvider) {
					CrucibleFile crucibleFile = ((ICrucibleFileProvider) editorInput).getCrucibleFile();
					String revision = crucibleFile.getSelectedFile().getRevision();

					if (focusMatchingComment(review.getFileByPermId(crucibleFile.getCrucibleFileInfo().getPermId()),
							revision, start)) {
						return;
					}
				}

				if (editorInput instanceof IFileEditorInput) {
					CrucibleFile crucibleFile = CrucibleUiUtil.getCruciblePostCommitFile(
							((IFileEditorInput) editorInput).getFile(), review);

					if (crucibleFile != null && crucibleFile.getSelectedFile() != null) {
						String revision = crucibleFile.getSelectedFile().getRevision();

						if (focusMatchingComment(
								review.getFileByPermId(crucibleFile.getCrucibleFileInfo().getPermId()), revision, start)) {
							return;
						}
					}
				}
			}
		}
	}

	public static final String ID = "com.atlassian.connector.eclipse.crucible.ui.explorerView";

	private static final String TAG_LINK_EDITOR = "linkWithEditor"; //$NON-NLS-1$

	private static final String TAG_MEMENTO = "memento"; //$NON-NLS-1$

	private OpenVirtualFileAction openOldAction;

	private OpenVirtualFileAction openNewAction;

	private CompareVirtualFilesAction compareAction;

	private TreeViewer viewer;

	private AddFileCommentAction addFileCommentAction;

	private Review initializeWith;

	private Review review;

	private ReplyToCommentAction replyToCommentAction;

	private EditCommentAction editCommentAction;

	private RemoveCommentAction removeCommentAction;

	private PostDraftCommentAction postDraftCommentAction;

	private ExpandAllAction expandAll;

	private CollapseAllAction collapseAll;

	private PublishAllDraftCommentsAction publishAllDraftsAction;

	private EditActiveTaskAction openEditorAction;

	private final Collection<IReviewActivationListener> reviewActivationListeners = MiscUtil.buildHashSet();

	private Action showCommentsViewAction;

	private IAction expandSelected;

	private IAction collapseSelected;

	private AddGeneralCommentToActiveReviewAction addGeneralCommentAction;

	private Action showUnreadOnlyAction;

	private final IPartListener2 linkWithEditorListener = new PartListenerAdapter() {
		public void partInputChanged(IWorkbenchPartReference partRef) {
			if (partRef instanceof IEditorReference) {
				editorActivated(((IEditorReference) partRef).getEditor(true));
			}
		}

		public void partActivated(IWorkbenchPartReference partRef) {
			if (partRef instanceof IEditorReference) {
				editorActivated(((IEditorReference) partRef).getEditor(true));
			}
		}
	};

	private boolean linkingEnabled;

	private final IDialogSettings dialogSettings;

	private IMemento memento;

	private OpenAndLinkWithEditorHelper openAndLinkWithEditorHelper;

	private Action linkWithEditorAction;

	private final TextSelectionToReviewItemListener linkEditorSelectionToTreeListener = new TextSelectionToReviewItemListener();

	private RefreshActiveReviewAction refreshReviewAction;

	private AddChangesetToActiveReviewAction addChangesetAction;

	private AddPatchToActiveReviewAction addPatchAction;

	private ActiveReviewCompletnesSwitcherAction completnesSwitcherAction;

	private static final String[] NO_ACTIVE_REVIEW = new String[] { "There's no active review.\n"
			+ "This view contents are rendered only if there's an active review." };

	protected static final int COMMENT_PREVIEW_LENGTH = 50;

	private static final int MAX_EXPANDED_BY_DEFAULT_ELEMENTS = 100;

	public ReviewExplorerView() {
		// exception: initialize from preference
		dialogSettings = CrucibleUiPlugin.getDefault().getDialogSettingsSection(getClass().getName());

		linkingEnabled = dialogSettings.getBoolean(TAG_LINK_EDITOR);
	}

	@Nullable
	public TreeViewer getViewer() {
		return viewer;
	}

	@Override
	public void createPartControl(Composite parent) {

		viewer = new TreeViewer(parent);
		viewer.setUseHashlookup(true);
		viewer.setComparer(new IElementComparer() {
			public int hashCode(Object element) {
				// compare comments only by their perm id
				// this is needed to preserve selection after
				// comment was modified
				if (element instanceof Comment) {
					return ((Comment) element).getPermId().hashCode();
				}
				return element.hashCode();
			}

			public boolean equals(Object a, Object b) {
				if (a instanceof Comment && b instanceof Comment) {
					return ((Comment) a).getPermId().equals(((Comment) b).getPermId());
				}
				if (a instanceof CrucibleFileInfo && b instanceof CrucibleFileInfo) {
					return ((CrucibleFileInfo) a).getPermId().equals(((CrucibleFileInfo) b).getPermId());
				}
				return a.equals(b);
			}
		});
		viewer.setContentProvider(new ReviewContentProvider());
		final DecoratingStyledCellLabelProvider styledLabelProvider = new DecoratingStyledCellLabelProvider(
				new ReviewExplorerLabelProvider(this), PlatformUI.getWorkbench()
						.getDecoratorManager()
						.getLabelDecorator(), null);
		viewer.setLabelProvider(styledLabelProvider);
		viewer.setComparator(new ReviewTreeComparator());

		final CustomToolTip toolTip = new CustomToolTip(viewer.getControl());
		toolTip.setInfoProvider(new ReviewExplorerInfoProvider());

		viewer.getTree().addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				toolTip.hide();
			}
		});

		openAndLinkWithEditorHelper = new OpenAndLinkWithEditorHelper(viewer) {
			protected void activate(ISelection selection) {
				final Object selectedElement = SelectionUtil.getSingleElement(selection);
				IEditorPart part = EditorUtil.isOpenInEditor(selectedElement);
				IWorkbenchPage page = EditorUtil.getActivePage();
				if (page != null && part != null) {
					page.activate(part);
				}
			}

			protected void linkToEditor(ISelection selection) {
				ReviewExplorerView.this.linkToEditor(selection);
			}

			protected void open(ISelection selection, boolean activate) {
				if (selection instanceof IStructuredSelection) {
					final IStructuredSelection structuredSelection = ((IStructuredSelection) selection);
					if (structuredSelection.size() != 1) {
						return;
					}
					final Object element = ((IStructuredSelection) selection).getFirstElement();

					if (element instanceof VersionedComment && openNewAction.isEnabled() && openOldAction.isEnabled()) {
						VersionedComment parent = ReviewModelUtil.getParentVersionedComment((Comment) element);
						Map<String, IntRanges> ranges = parent.getLineRanges();
						if (ranges != null) {
							VersionedVirtualFile newFile = parent.getCrucibleFileInfo().getFileDescriptor();
							VersionedVirtualFile oldFile = parent.getCrucibleFileInfo().getOldFileDescriptor();

							if (newFile != null && ranges.containsKey(newFile.getRevision())) {
								openNewAction.run();
								return;
							}

							if (oldFile != null && ranges.containsKey(oldFile.getRevision())) {
								openOldAction.run();
								return;
							}
						}
					}

					if (openNewAction.isEnabled()) {
						openNewAction.run();
					} else if (openOldAction.isEnabled()) {
						openOldAction.run();
					} else {
						if (viewer.getExpandedState(element)) {
							viewer.collapseToLevel(element, AbstractTreeViewer.ALL_LEVELS);
						} else {
							viewer.expandToLevel(element, AbstractTreeViewer.ALL_LEVELS);

						}
					}
				}
			}

		};

		createActions();
		createToolbar();
		createMenu();
		createContextMenu();

		getSite().setSelectionProvider(viewer);
		getSite().getPage().addPostSelectionListener(linkEditorSelectionToTreeListener);
		setReviewImpl(initializeWith, true, Collections.<CrucibleNotification> emptyList());
		setLinkingEnabled(linkingEnabled);
	}

	protected void editorActivated(IEditorPart editor) {
		if (review == null) {
			return;
		}

		IEditorInput editorInput = editor.getEditorInput();
		if (editorInput == null) {
			return;
		}
		Object input = getInputFromEditor(editorInput);
		if (input == null) {
			return;
		}
		if (!inputIsSelected(input)) {
			showInput(input);
		} else {
			viewer.getTree().showSelection();
		}
	}

	private boolean inputIsSelected(Object input) {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (selection.size() != 1) {
			return false;
		}

		return selection.getFirstElement().equals(input);
	}

	boolean showInput(Object input) {
		Object element = null;

		if (input instanceof IFile) {
			final String fileName = StringUtil.removeLeadingAndTrailingSlashes(((IFile) input).getFullPath().toString());
			for (CrucibleFileInfo fileInfo : review.getFiles()) {
				if (StringUtil.removeLeadingAndTrailingSlashes(fileInfo.getFileDescriptor().getUrl()).equals(fileName)) {
					element = new ReviewTreeNode(fileInfo);
					break;
				}
			}
			if (element == null) {
				return false;
			}
		}

		if (input instanceof CrucibleFileInfo) {
			element = new ReviewTreeNode((CrucibleFileInfo) input);
		}

		if (element == null) {
			element = input;
		}

		if (element != null) {
			ISelection newSelection = new StructuredSelection(element);
			if (viewer.getSelection().equals(newSelection)) {
				viewer.reveal(element);
			} else {
				TreeViewerUtil.setSelection(viewer, element);
			}
			return true;
		}
		return false;
	}

	private Object getInputFromEditor(IEditorInput editorInput) {
		Object input = null;
		if (editorInput instanceof CrucibleFileInfoCompareEditorInput) {
			input = ((CrucibleFileInfoCompareEditorInput) editorInput).getCrucibleFileInfo();
		} else if (editorInput instanceof ICrucibleFileProvider) {
			input = ((ICrucibleFileProvider) editorInput).getCrucibleFile().getCrucibleFileInfo();
		}
		if (input == null) {
			input = editorInput.getAdapter(IFile.class);
		}
		if (input == null && editorInput instanceof IStorageEditorInput) {
			try {
				input = ((IStorageEditorInput) editorInput).getStorage();
			} catch (CoreException e) {
				// ignore
			}
		}
		return input;
	}

	private void createMenu() {
		IMenuManager mgr = getViewSite().getActionBars().getMenuManager();
		mgr.add(addChangesetAction);
		mgr.add(addPatchAction);
		mgr.add(new Separator());
		mgr.add(showUnreadOnlyAction);
		mgr.add(new Separator());
		mgr.add(linkWithEditorAction);
	}

	private ReviewTreeNode[] reviewToTreeNodes(/* final Review newReview */) {
		ReviewTreeNode[] nodes = new ReviewTreeNode[] { new GeneralCommentsNode(null, "General Comments", -1, review),
				new ReviewTreeNode(null, "Files") {
					public List<Object> getChildren() {
						return Arrays.<Object> asList((Object[]) compactReviewFiles(review));
					};
				} };
		return nodes;
	}

	private void setReviewImpl(Review aReview, boolean isFullRebuild, Collection<CrucibleNotification> differences) {
		final Review oldReview = review;
		review = aReview;
		if (review != null) {

			final ISelection currentSelection = viewer.getSelection();
			if (isFullRebuild) {
				final ReviewTreeNode[] newInput = reviewToTreeNodes(/* newReview */);
				final Object[] previouslyExpandedElements = viewer.getExpandedElements();
				viewer.setInput(newInput);
				if (oldReview == null || !oldReview.equals(review)) {
					final ArrayList<Object> expandedElements = MiscUtil.<Object> buildArrayList();
					fillExpandedElements(expandedElements, Arrays.asList(newInput));
					viewer.setExpandedElements(expandedElements.subList(0,
							Math.min(expandedElements.size(), MAX_EXPANDED_BY_DEFAULT_ELEMENTS)).toArray());
				} else {
					viewer.setExpandedElements(previouslyExpandedElements);
				}
			}

			Comment focusOnComment = null;
			if (oldReview != null) {
				for (CrucibleNotification diff : differences) {
					if (diff instanceof NewCommentNotification) {
						focusOnComment = ((NewCommentNotification) diff).getComment();
						break;
					}
				}
			}

			if (focusOnComment == null) {
				// this simple trick (thanks to properly working equals on permIds) refreshes selection, when
				// the old selection now points to an outdated review model element (e.g. a review comment which has been
				// changed)
				viewer.setSelection(currentSelection);
			} else {
				TreeViewerUtil.setSelection(getViewer(), focusOnComment);
			}
		} else {
			viewer.setInput(NO_ACTIVE_REVIEW);
		}

		if (review == null) {
			setContentDescription("");
		} else {
			setContentDescription(NLS.bind("Review files for {0} ({1} files, {2} comments)", new Object[] {
					review.getPermId().getId(), review.getFiles().size(), review.getNumberOfVersionedComments() }));
		}
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento == null) {
			String persistedMemento = dialogSettings.get(TAG_MEMENTO);
			if (persistedMemento != null) {
				try {
					memento = XMLMemento.createReadRoot(new StringReader(persistedMemento));
				} catch (WorkbenchException e) {
					// don't do anything. Simply don't restore the settings
				}
			}
		}

		this.memento = memento;
		if (this.memento != null) {
			restoreLinkingEnabled(memento);
		}

		final ActiveReviewManager mgr = CrucibleUiPlugin.getDefault().getActiveReviewManager();
		if (mgr.isReviewActive()) {
			reviewActivated(mgr.getActiveTask(), mgr.getActiveReview());
		}
		mgr.addReviewActivationListener(this);
	}

	@Override
	public void dispose() {
		ActiveReviewManager mgr = CrucibleUiPlugin.getDefault().getActiveReviewManager();

		for (IReviewActivationListener listener : reviewActivationListeners) {
			mgr.removeReviewActivationListener(listener);
		}

		mgr.removeReviewActivationListener(this);

		XMLMemento memento = XMLMemento.createWriteRoot("reviewExplorer"); //$NON-NLS-1$
		saveState(memento);
		StringWriter writer = new StringWriter();
		try {
			memento.save(writer);
			dialogSettings.put(TAG_MEMENTO, writer.getBuffer().toString());
		} catch (IOException e) {
			// don't do anything. Simply don't store the settings
		}

		// always remove even if we didn't register
		getSite().getPage().removePartListener(linkWithEditorListener);
		getSite().getPage().removePostSelectionListener(linkEditorSelectionToTreeListener);

		super.dispose();
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public boolean isFocusedOnUnreadComments() {
		return showUnreadOnlyAction != null && showUnreadOnlyAction.isChecked();
	}

	public void createActions() {
		showUnreadOnlyAction = new Action("Focus on unread comments only", IAction.AS_CHECK_BOX) {
			{
				setImageDescriptor(CommonImages.FILTER_COMPLETE);
			}

			public void run() {
				viewer.refresh();
			};
		};

		linkWithEditorAction = new Action("&Link with Editor", IAction.AS_CHECK_BOX) {
			{
				setImageDescriptor(AtlassianImages.IMG_LINK_WITH_EDITOR);
				setChecked(isLinkingEnabled());
			}

			public void run() {
				setLinkingEnabled(isChecked());
			};

		};

		refreshReviewAction = new RefreshActiveReviewAction(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						viewer.refresh(true);
					}
				});
			}
		});
		reviewActivationListeners.add(refreshReviewAction);

		addFileCommentAction = new AddFileCommentAction();
		viewer.addSelectionChangedListener(addFileCommentAction);

		openOldAction = new OpenVirtualFileAction(true);
		viewer.addSelectionChangedListener(openOldAction);

		openNewAction = new OpenVirtualFileAction(false);
		viewer.addSelectionChangedListener(openNewAction);

		compareAction = new CompareVirtualFilesAction();
		viewer.addSelectionChangedListener(compareAction);

		replyToCommentAction = new ReplyToCommentAction();
		viewer.addSelectionChangedListener(replyToCommentAction);

		editCommentAction = new EditCommentAction();
		viewer.addSelectionChangedListener(editCommentAction);

		removeCommentAction = new RemoveCommentAction();
		viewer.addSelectionChangedListener(removeCommentAction);

		postDraftCommentAction = new PostDraftCommentAction();
		viewer.addSelectionChangedListener(postDraftCommentAction);

		expandAll = new ExpandAllAction(viewer);
		collapseAll = new CollapseAllAction(viewer);

		publishAllDraftsAction = new PublishAllDraftCommentsAction();
		reviewActivationListeners.add(publishAllDraftsAction);

		expandSelected = new ExpandCollapseSelectionAction(viewer, true);
		collapseSelected = new ExpandCollapseSelectionAction(viewer, false);

		showCommentsViewAction = new Action() {
			{
				setText("Show Comment View");
				setToolTipText("Show Comment View");
				setImageDescriptor(CrucibleImages.COMMENT_SMALL);
			}

			public void run() {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
							CrucibleUiPlugin.COMMENT_VIEW_ID);
				} catch (PartInitException e) {
					// don't care
				}
			};
		};

		addGeneralCommentAction = new AddGeneralCommentToActiveReviewAction();
		reviewActivationListeners.add(addGeneralCommentAction);

		openEditorAction = new EditActiveTaskAction();
		reviewActivationListeners.add(openEditorAction);

		addChangesetAction = new AddChangesetToActiveReviewAction();
		reviewActivationListeners.add(addChangesetAction);

		addPatchAction = new AddPatchToActiveReviewAction();
		reviewActivationListeners.add(addPatchAction);

		completnesSwitcherAction = new ActiveReviewCompletnesSwitcherAction();
		reviewActivationListeners.add(completnesSwitcherAction);

		// in the end register all additional activation listeners
		final ActiveReviewManager mgr = CrucibleUiPlugin.getDefault().getActiveReviewManager();
		for (IReviewActivationListener listener : reviewActivationListeners) {
			if (mgr.isReviewActive()) {
				listener.reviewActivated(mgr.getActiveTask(), mgr.getActiveReview());
			}
			mgr.addReviewActivationListener(listener);
		}
	}

	public void createToolbar() {
		final IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		final CommentNavigationAction prevCommentAction = new CommentNavigationAction(this, getViewSite(), false);
		final CommentNavigationAction nextCommentAction = new CommentNavigationAction(this, getViewSite(), true);

		mgr.add(expandAll);
		mgr.add(collapseAll);
		mgr.add(linkWithEditorAction);
		mgr.add(new Separator());
		mgr.add(showUnreadOnlyAction);
		mgr.add(new Separator());
		mgr.add(prevCommentAction);
		mgr.add(nextCommentAction);
		mgr.add(new Separator());
		mgr.add(openEditorAction);
		mgr.add(refreshReviewAction);
		mgr.add(showCommentsViewAction);
		mgr.add(new Separator());
		mgr.add(addGeneralCommentAction);
		mgr.add(publishAllDraftsAction);
		mgr.add(completnesSwitcherAction);
	}

	private void createContextMenu() {
		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(mgr);
			}
		});

		Menu menu = mgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		getSite().registerContextMenu(mgr, viewer);
	}

	private void fillContextMenu(MenuManager mgr) {
		mgr.add(expandSelected);
		mgr.add(collapseSelected);
		mgr.add(new Separator());
		mgr.add(addGeneralCommentAction);
		mgr.add(addFileCommentAction);
		mgr.add(replyToCommentAction);
		mgr.add(editCommentAction);
		mgr.add(removeCommentAction);
		mgr.add(postDraftCommentAction);
		ToggleCommentsLeaveUnreadAction action = new ToggleCommentsLeaveUnreadAction();
		action.selectionChanged((IStructuredSelection) viewer.getSelection());
		mgr.add(action);
		mgr.add(new Separator());
		mgr.add(openOldAction);
		mgr.add(openNewAction);
		mgr.add(compareAction);
	}

	private void fillExpandedElements(Collection<Object> expandedElements, List<? extends Object> roots) {
		for (Object root : roots) {
			if (root instanceof ReviewTreeNode) {
				expandedElements.add(root);
				fillExpandedElements(expandedElements, ((ReviewTreeNode) root).getChildren());
			}
		}
	}

	public void reviewActivated(ITask task, final Review newReview) {
		setReview(newReview, true, Collections.<CrucibleNotification> emptyList());
	}

	private void setReview(final Review newReview, final boolean isFullRebuild,
			final Collection<CrucibleNotification> differences) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (viewer != null) {
					// check if we need to perform full review (when requested explicitly or we
					// cannot handle incremental refresh (with tree compacting it's pretty hard when files are changed)
					boolean isFull = isFullRebuild || !refreshIncrementallyViewer(newReview, differences);
					setReviewImpl(newReview, isFull, differences);
				} else {
					initializeWith = newReview;
				}
			}
		});

		downloadAvatarImages(newReview);
	}

	private void downloadAvatarImages(final Review newReview) {
		// now let's download images then we'll update tree viewer
		CrucibleClient client = CrucibleUiUtil.getClient(newReview);
		Job downloadAvatars = client.createDownloadAvatarsJob(CrucibleUiUtil.getCrucibleTaskRepository(newReview),
				newReview);
		downloadAvatars.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						final DownloadAvatarsJob job = ((DownloadAvatarsJob) event.getJob());
						boolean isNewAvatarAvailable = false;
						final AvatarImages avatarsCache = CrucibleUiPlugin.getDefault().getAvatarsCache();

						for (Map.Entry<User, byte[]> avatar : job.getAvatars().entrySet()) {
							synchronized (avatarsCache) {
								if (avatarsCache.getAvatar(avatar.getKey(), AvatarSize.ORIGINAL) == null) {
									avatarsCache.addAvatar(avatar.getKey(), avatar.getValue());
									isNewAvatarAvailable = true;
								}
							}
						}
						if (isNewAvatarAvailable) {
							// update just whole viewer (takes <200 ms for huge reviews (1500 files) AND fully expanded trees
							viewer.refresh(true);
						}
					}
				});
			}
		});
		downloadAvatars.setPriority(Job.DECORATE);
		downloadAvatars.schedule();
	}

	/**
	 * Uses linear search (potentially very slow), but in fact, when I tested in on a review with 1700 files, it was
	 * able to find random {@link CrucibleFileInfo} in 50 - 100 ms. More than good enough.
	 * 
	 * So I decided not to keep a separate map {@link CrucibleFileInfo} -> {@link ReviewTreeNode}
	 * 
	 * @param cfi
	 * @return a node with {@link CrucibleFileInfo} similar to the argument (the same root info, but perhaps different
	 *         comments)
	 */
	private ReviewTreeNode findReviewTreeNode(@NotNull CrucibleFileInfo cfi) {
		ReviewTreeNode[] model = getCurrentModel();
		for (ReviewTreeNode reviewTreeNode : model) {
			final ReviewTreeNode matchingNode = findReviewTreeNode(cfi, reviewTreeNode);
			if (matchingNode != null) {
				return matchingNode;
			}
		}
		return null;
	}

	private ReviewTreeNode findReviewTreeNode(@NotNull CrucibleFileInfo cfi, ReviewTreeNode rootNode) {
		if (rootNode.getCrucibleFileInfo() != null && rootNode.getCrucibleFileInfo().equals(cfi)) {
			return rootNode;
		}

		for (Object child : rootNode.getChildren()) {
			if (child instanceof ReviewTreeNode) {
				ReviewTreeNode childNode = (ReviewTreeNode) child;
				final ReviewTreeNode matchingNode = findReviewTreeNode(cfi, childNode);
				if (matchingNode != null) {
					return matchingNode;
				}
			}
		}
		return null;
	}

	public static ReviewTreeNode[] compactReviewFiles(Review review) {
		ReviewTreeNode root = new ReviewTreeNode(null, null);
		for (CrucibleFileInfo cfi : review.getFiles()) {
			final String path = cfi.getFileDescriptor().getUrl();
			final String[] pathTokens = path.split("/|\\\\");
			root.add(pathTokens, cfi);
		}
		root.compact();
		return (root.getPathToken() != null) ? new ReviewTreeNode[] { root } : root.getChildren().toArray(
				new ReviewTreeNode[0]);
	}

	public void reviewDeactivated(ITask task, Review aReview) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				setReviewImpl(null, true, null);
			}
		});
	}

	private boolean canHandleIncrementalRefresh(Collection<CrucibleNotification> differences) {
		for (CrucibleNotification diff : differences) {
			switch (diff.getType()) {
			// these require full refresh
			case EXCEPTION_RAISED:
			case NEW_REVIEW:
			case NEW_REVIEW_ITEM:
			case NOT_VISIBLE_REVIEW:
			case REMOVED_REVIEW_ITEM:
			default:
				return false;
				// continue - these can be handled incrementally
			case REVIEW_DATA_CHANGED:
			case PROJECT_CHANGED:
			case NAME_CHANGED:
			case COMMENT_READ_UNREAD_STATE_CHANGED:
			case AUTHOR_CHANGED:
			case DUE_DATE_CHANGED:
			case NEW_COMMENT:
			case REMOVED_COMMENT:
			case REVIEWERS_CHANGED:
			case REVIEWER_COMPLETED:
			case REVIEW_COMPLETED:
			case REVIEW_STATE_CHANGED:
			case STATEMENT_OF_OBJECTIVES_CHANGED:
			case SUMMARY_CHANGED:
			case MODERATOR_CHANGED:
			case UPDATED_COMMENT:
				break;
			}
		}
		return true;
	}

	public void reviewUpdated(ITask task, Review aReview, Collection<CrucibleNotification> differences) {
		setReview(aReview, false, differences);
	}

	private boolean refreshIncrementallyViewer(Review aReview, Collection<CrucibleNotification> differences) {
		if (aReview != null) {
			if (!canHandleIncrementalRefresh(differences)) {
				return false;
			}
			final ReviewTreeNode[] currentInput = getCurrentModel();
			final GeneralCommentsNode generalCommentsNode = (currentInput[0] instanceof GeneralCommentsNode) ? (GeneralCommentsNode) currentInput[0]
					: null;

			for (CrucibleNotification diff : differences) {
				if (diff instanceof AbstractCommentNotification) {
					final Comment comment = ((AbstractCommentNotification) diff).getComment();
					final VersionedComment parentVerComment = ReviewModelUtil.getParentVersionedComment(comment);
					if (parentVerComment != null) {
						// in this case let's refresh entire node with given files and all its children
						final ReviewTreeNode treeNode = findReviewTreeNode(parentVerComment.getCrucibleFileInfo());

						final CrucibleFileInfo fileByPermId = aReview.getFileByPermId(parentVerComment.getCrucibleFileInfo()
								.getPermId());
						if (fileByPermId == null) {
							return false;
						}

						treeNode.setCrucibleFileInfo(fileByPermId);
						getViewer().refresh(treeNode);
					} else {
						// it's a general comment or reply to a general comment - let's refresh all general comments
						generalCommentsNode.setReview(aReview);
						getViewer().refresh(generalCommentsNode);
					}
				}
			}
		}
		return true;
	}

	@Nullable
	private ReviewTreeNode[] getCurrentModel() {
		final Object input = viewer.getInput();
		if (input instanceof ReviewTreeNode[]) {
			return (ReviewTreeNode[]) input;
		}
		return null;
	}

	public void setLinkingEnabled(boolean enabled) {
		linkingEnabled = enabled;
		saveDialogSettings();

		IWorkbenchPage page = getSite().getPage();
		if (enabled) {
			page.addPartListener(linkWithEditorListener);

			IEditorPart editor = page.getActiveEditor();
			if (editor != null) {
				editorActivated(editor);
			}
		} else {
			page.removePartListener(linkWithEditorListener);
		}
		openAndLinkWithEditorHelper.setLinkWithEditor(enabled);
	}

	private void saveDialogSettings() {
		dialogSettings.put(TAG_LINK_EDITOR, linkingEnabled);
	}

	private void restoreLinkingEnabled(IMemento memento) {
		Integer val = memento.getInteger(TAG_LINK_EDITOR);
		linkingEnabled = val != null && val.intValue() != 0;
	}

	private void linkToEditor(ISelection selection) {
		Object obj = SelectionUtil.getSingleElement(selection);
		if (obj != null) {
			IEditorPart part = EditorUtil.isOpenInEditor(obj);
			if (part != null) {
				IWorkbenchPage page = getSite().getPage();
				page.bringToTop(part);

				if (obj instanceof Comment) {
					revealComment(part, (Comment) obj);
				}
			}
		}
	}

	private void revealComment(IEditorPart part, Comment comment) {
		VersionedComment parent = ReviewModelUtil.getParentVersionedComment(comment);
		IEditorInput input = part.getEditorInput();

		if (part instanceof ITextEditor) {
			if (input instanceof ICrucibleFileProvider) {
				EditorUtil.selectAndReveal((ITextEditor) part, parent,
						((ICrucibleFileProvider) input).getCrucibleFile().getSelectedFile());
			}

			if (input instanceof IFileEditorInput) {
				CrucibleFile fromEditor = CrucibleUiUtil.getCruciblePostCommitFile(
						((IFileEditorInput) input).getFile(), CrucibleUiPlugin.getDefault()
								.getActiveReviewManager()
								.getActiveReview());
				if (fromEditor != null) {
					EditorUtil.selectAndReveal((ITextEditor) part, parent, fromEditor.getSelectedFile());
				}
			}
		} else if (part instanceof CompareEditor) {
			IEditorInput editorInput = part.getEditorInput();
			if (editorInput instanceof CrucibleFileInfoCompareEditorInput) {
				CrucibleFileInfoCompareEditorInput compareInput = (CrucibleFileInfoCompareEditorInput) editorInput;
				compareInput.getAnnotationModelToAttach().focusOnComment(parent);
			}
		}
	}

	public boolean isLinkingEnabled() {
		return linkingEnabled;
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		memento.putInteger(TAG_LINK_EDITOR, linkingEnabled ? 1 : 0);
	}
}