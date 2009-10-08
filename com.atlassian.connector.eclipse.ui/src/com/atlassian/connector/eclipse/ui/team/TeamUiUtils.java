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

package com.atlassian.connector.eclipse.ui.team;

import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;
import com.atlassian.connector.eclipse.ui.CruciblePreCommitFileInput;
import com.atlassian.connector.eclipse.ui.exceptions.UnsupportedTeamProviderException;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.contentmergeviewer.ContentMergeViewer;
import org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.internal.MergeSourceViewer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A utility class for doing UI related operations for team items
 * 
 * @author Shawn Minto
 */
@SuppressWarnings("restriction")
public final class TeamUiUtils {

	public static final String TEAM_PROVIDER_ID_CVS_ECLIPSE = "org.eclipse.team.cvs.core.cvsnature";

	public static final String TEAM_PROV_ID_SVN_SUBCLIPSE = "org.tigris.subversion.subclipse.core.svnnature";

	public static final String TEAM_PROV_ID_SVN_SUBVERSIVE = "org.eclipse.team.svn.core.svnnature";

	private static DefaultTeamResourceConnector defaultConnector = new DefaultTeamResourceConnector();

	private TeamUiUtils() {
	}

	@Nullable
	public static IEditorPart openFile(String repoUrl, String filePath, String otherRevisionFilePath,
			String revisionString, String otherRevisionString, IProgressMonitor monitor) {
		// TODO if the repo url is null, we should probably use the task repo host and look at all repos

		assert (filePath != null);
		assert (revisionString != null);

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();

		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled() && connector.canHandleFile(repoUrl, filePath, monitor)) {
				try {
					IEditorPart part = connector.openFile(repoUrl, filePath, otherRevisionFilePath, revisionString,
							otherRevisionString, monitor);
					if (part != null) {
						return part;
					}
				} catch (CoreException e) {
					StatusHandler.log(e.getStatus());
					//ignore and try with default
				}
			}
		}

		// try a backup solution
		try {
			return defaultConnector.openFile(repoUrl, filePath, otherRevisionFilePath, revisionString,
					otherRevisionString, monitor);
		} catch (UnsupportedTeamProviderException e) {
			TeamMessageUtils.openUnsupportedTeamProviderErrorMessage(e);
			return null;
		} catch (CoreException e) {
			TeamMessageUtils.openCouldNotOpenFileErrorMessage(repoUrl, filePath, revisionString);
			return null;
		}
	}

	public static Collection<String> getSupportedTeamConnectors() {
		Collection<String> res = MiscUtil.buildArrayList();
		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();
		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled()) {
				res.add(connector.getName());
			}
		}
		res.add(defaultConnector.getName());
		return res;
	}

	@Nullable
	public static SortedSet<Long> getRevisionsForFile(IFile file, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(file);
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();

		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled()) {
				try {
					SortedSet<Long> revisions = connector.getRevisionsForFile(file, monitor);
					if (revisions != null) {
						return revisions;
					}
				} catch (CoreException e) {
					StatusHandler.log(e.getStatus());
					// ignore and try other connector(s)
				}
			}
		}
		return defaultConnector.getRevisionsForFile(file, monitor);
	}

//	private interface ConnectorOperation<T> {
//		T execute(ITeamResourceConnector connector, IProgressMonitor monitor) throws CoreException;
//	}

//	private static <T> T executeOnConnectors(ConnectorOperation<T> operation, IProgressMonitor monitor)
//			throws CoreException {
//		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();
//
//		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
//			if (connector.isEnabled()) {
//				try {
//					return operation.execute(connector, monitor);
//				} catch (CoreException e) {
//					// ignore and try other connector(s)
//				}
//			}
//		}
//		return operation.execute(defaultConnector, monitor);
//	}

	/**
	 * @param monitor
	 *            progress monitor
	 * @return all supported repositories configured in current workspace
	 */
	@NotNull
	public static Collection<RepositoryInfo> getRepositories(IProgressMonitor monitor) {
		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();
		Collection<RepositoryInfo> res = MiscUtil.buildArrayList();

		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled()) {
//				try {
				res.addAll(connector.getRepositories(monitor));
//				} catch (CoreException e) {
//					StatusHandler.log(new Status(IStatus.WARNING, AtlassianUiPlugin.PLUGIN_ID,
//							"Cannot get repositories for a connector"));
//					// ignore and try other connector(s)
//				}
			}
		}
		res.addAll(defaultConnector.getRepositories(monitor));
		return res;

	}

	/**
	 * Retrieve (some)changesets for (a given) repository
	 * 
	 * @param repositoryUrl
	 *            The repository URL to get changesets from, or NULL to retrieve form all available repositories
	 * @param limit
	 *            The amount of revisions to retrieve
	 */
	@NotNull
	public static SortedSet<ICustomChangesetLogEntry> getLatestsChangesets(@NotNull String repositoryUrl, int limit,
			IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();

		monitor.beginTask("Retrieving changesets", teamResourceManager.getTeamConnectors().size() + 1);
		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			IProgressMonitor subMonitor = Policy.subMonitorFor(monitor, 1);
			try {
				if (connector.isEnabled()) {
					try {
						SortedSet<ICustomChangesetLogEntry> changesets = connector.getLatestChangesets(repositoryUrl,
								limit, subMonitor);
						if (changesets != null) {
							return changesets;
						}
					} catch (RuntimeException e) {
						throw new CoreException(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID,
								"Exception encountered while building list of the latest changesets", e));
					}
				}
			} finally {
				subMonitor.done();
			}
		}

		IProgressMonitor subMonitor = Policy.subMonitorFor(monitor, 1);
		try {
			SortedSet<ICustomChangesetLogEntry> changesets = defaultConnector.getLatestChangesets(repositoryUrl, limit,
					subMonitor);
			if (changesets != null) {
				return changesets;
			}
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID,
					"Exception encountered while building list of the latest changesets", e));
		}

		subMonitor.done();
		monitor.done();
		return new TreeSet<ICustomChangesetLogEntry>();
	}

	public static Map<IFile, SortedSet<Long>> getRevisionsForFiles(Collection<IFile> files, IProgressMonitor monitor)
			throws CoreException {
		Assert.isNotNull(files);
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();

		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled()) {
				try {
					Map<IFile, SortedSet<Long>> revisions = connector.getRevisionsForFiles(files, monitor);
					if (revisions != null) {
						return revisions;
					}
				} catch (CoreException e) {
					StatusHandler.log(e.getStatus());
					// ignore and try other connector(s)
				}
			}
		}
		return defaultConnector.getRevisionsForFiles(files, monitor);
	}

	public static void openCompareEditor(String repoUrl, String filePath, String otherRevisionFilePath,
			String oldRevisionString, String newRevisionString, ICompareAnnotationModel annotationModel,
			IProgressMonitor monitor) {
		assert (filePath != null);
		assert (oldRevisionString != null);
		assert (newRevisionString != null);

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();

		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled() && connector.canHandleFile(repoUrl, filePath, monitor)) {
				try {
					if (connector.openCompareEditor(repoUrl, filePath, otherRevisionFilePath, oldRevisionString,
							newRevisionString, annotationModel, monitor)) {
						return;
					}
				} catch (CoreException e) {
					StatusHandler.log(e.getStatus());
					//ignore and try with default
				}
			}
		}

		try {
			if (!defaultConnector.openCompareEditor(repoUrl, filePath, otherRevisionFilePath, oldRevisionString,
					newRevisionString, annotationModel, monitor)) {
				TeamMessageUtils.openUnableToCompareErrorMessage(repoUrl, filePath, oldRevisionString,
						newRevisionString, null);
			}
		} catch (UnsupportedTeamProviderException e) {
			TeamMessageUtils.openUnsupportedTeamProviderErrorMessage(e);
		} catch (CoreException e) {
			TeamMessageUtils.openUnableToCompareErrorMessage(repoUrl, filePath, oldRevisionString, newRevisionString, e);
		}
	}

	public static LineRange getSelectedLineNumberRangeFromEditorInput(IEditorPart editor, IEditorInput editorInput) {

		if (editor instanceof ITextEditor && editor.getEditorInput() == editorInput) {
			ISelection selection = ((ITextEditor) editor).getSelectionProvider().getSelection();
			if (selection instanceof TextSelection) {
				TextSelection textSelection = ((TextSelection) selection);
				return new LineRange(textSelection.getStartLine() + 1, textSelection.getEndLine()
						- textSelection.getStartLine());
			} else {
				StatusHandler.log(new Status(IStatus.INFO, AtlassianUiPlugin.PLUGIN_ID,
						"Selection is not a text selection " + selection));
			}
		} else {
			StatusHandler.log(new Status(IStatus.INFO, AtlassianUiPlugin.PLUGIN_ID,
					"Editor is not an ITextEditor or editor inputs not equal " + editor));
		}
		return null;
	}

	public static IEditorPart openLocalResource(final IResource resource) {
		if (Display.getCurrent() != null) {
			return openLocalResourceInternal(resource);
		} else {
			final IEditorPart[] part = new IEditorPart[1];
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					part[0] = openLocalResourceInternal(resource);
				}
			});
			return part[0];
		}
	}

	private static IEditorPart openLocalResourceInternal(IResource resource) {
		// the local revision matches the revision we care about and the file is in sync
		try {
			return IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
					(IFile) resource, true);
		} catch (PartInitException e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return null;
	}

	@Nullable
	public static CrucibleFile getCorrespondingCrucibleFileFromEditorInput(IEditorInput editorInput, Review activeReview) {
		if (activeReview == null) {
			return null;
		}

		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();

		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled() && connector.canHandleEditorInput(editorInput)) {
				CrucibleFile fileInfo;
				try {
					fileInfo = connector.getCorrespondingCrucibleFileFromEditorInput(editorInput, activeReview);
				} catch (UnsupportedTeamProviderException e) {
					return null;
				}
				if (fileInfo != null) {
					return fileInfo;
				}
			}
		}

		try {
			CrucibleFile file = defaultConnector.getCorrespondingCrucibleFileFromEditorInput(editorInput, activeReview);
			if (file != null) {
				return file;
			}
		} catch (UnsupportedTeamProviderException e) {
			// ignore
		}

		try {
			return getCorrespondingCruciblePreCommitFileFromEditorInput(editorInput, activeReview);
		} catch (ValueNotYetInitialized e1) {
			return null;
		}

	}

	private static CrucibleFile getCorrespondingCruciblePreCommitFileFromEditorInput(IEditorInput editorInput,
			Review activeReview) throws ValueNotYetInitialized {

		if (editorInput instanceof CruciblePreCommitFileInput) {
			CruciblePreCommitFileInput input = (CruciblePreCommitFileInput) editorInput;

			return input.getCrucibleFile();

//			for (CrucibleFileInfo file : activeReview.getFiles()) {
//				VersionedVirtualFile fileDescriptor = file.getFileDescriptor();
//				VersionedVirtualFile oldFileDescriptor = file.getOldFileDescriptor();
//
//				if (fileDescriptor.getContentUrl() != null && fileDescriptor.getContentUrl().equals(inputUrl)
//						&& fileDescriptor.getRevision() != null && fileDescriptor.getRevision().equals(anObject)) {
//					return new CrucibleFile(file, false);
//				} else if (oldFileDescriptor.getContentUrl() != null
//						&& oldFileDescriptor.getContentUrl().equals(inputUrl)) {
//					return new CrucibleFile(file, true);
//				}
//			}
		}
		return null;
	}

	@Nullable
	public static RepositoryInfo getApplicableRepository(@NotNull IResource resource) {
		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();

		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled()) {
				try {
					RepositoryInfo res = connector.getApplicableRepository(resource);
					if (res != null) {
						return res;
					}
				} catch (CoreException e) {
					StatusHandler.log(new Status(IStatus.WARNING, AtlassianUiPlugin.PLUGIN_ID, e.getMessage(), e));
					// and try the next connector
				}
			}
		}
		return null;

	}

	public static RevisionInfo getLocalRevision(@NotNull IResource resource) throws CoreException {
		ITeamResourceConnector connector = AtlassianUiPlugin.getDefault().getTeamResourceManager().getTeamConnector(
				resource);

		if (connector != null && connector.isEnabled()) {
			RevisionInfo res = connector.getLocalRevision(resource);
			if (res != null) {
				return res;
			}
		}
		return null;
	}

	public static void selectAndReveal(final ITextEditor textEditor, int startLine, int endLine) {
		IDocumentProvider documentProvider = textEditor.getDocumentProvider();
		IEditorInput editorInput = textEditor.getEditorInput();
		if (documentProvider != null) {
			IDocument document = documentProvider.getDocument(editorInput);
			if (document != null) {
				try {
					final int offset = document.getLineOffset(startLine);
					final int length = document.getLineOffset(endLine) - offset;
					if (Display.getCurrent() != null) {
						internalSelectAndReveal(textEditor, offset, length);
					} else {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								internalSelectAndReveal(textEditor, offset, length);
							}
						});
					}

				} catch (BadLocationException e) {
					StatusHandler.log(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		}
	}

	public static void openCompareEditorForInput(final CompareEditorInput compareEditorInput) {
		if (Display.getCurrent() != null) {
			internalOpenCompareEditorForInput(compareEditorInput);
		} else {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					internalOpenCompareEditorForInput(compareEditorInput);
				}
			});
		}
	}

	private static void internalOpenCompareEditorForInput(CompareEditorInput compareEditorInput) {
		IWorkbench workbench = AtlassianUiPlugin.getDefault().getWorkbench();
		IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
		CompareUI.openCompareEditorOnPage(compareEditorInput, page);
	}

	private static void internalSelectAndReveal(ITextEditor textEditor, final int offset, final int length) {
		textEditor.selectAndReveal(offset, length);
	}

	public static Viewer findContentViewer(Viewer contentViewer, ICompareInput input, Composite parent,
			ICompareAnnotationModel annotationModel) {

		// FIXME: hack
		if (contentViewer instanceof TextMergeViewer) {
			TextMergeViewer textMergeViewer = (TextMergeViewer) contentViewer;
			try {
				Class<TextMergeViewer> clazz = TextMergeViewer.class;
				Field declaredField = clazz.getDeclaredField("fLeft");
				declaredField.setAccessible(true);
				final MergeSourceViewer fLeft = (MergeSourceViewer) declaredField.get(textMergeViewer);

				declaredField = clazz.getDeclaredField("fRight");
				declaredField.setAccessible(true);
				final MergeSourceViewer fRight = (MergeSourceViewer) declaredField.get(textMergeViewer);

				annotationModel.attachToViewer(fLeft, fRight);
				annotationModel.focusOnComment();
				annotationModel.registerContextMenu();

				hackGalileo(contentViewer, textMergeViewer, fLeft, fRight);
			} catch (Throwable t) {
				StatusHandler.log(new Status(IStatus.WARNING, AtlassianUiPlugin.PLUGIN_ID,
						"Could not initialize annotation model for " + input.getName(), t));
			}
		}
		return contentViewer;
	}

	private static void hackGalileo(Viewer contentViewer, TextMergeViewer textMergeViewer,
			final MergeSourceViewer fLeft, final MergeSourceViewer fRight) {
		// FIXME: hack for e3.5
		try {
			Method getCompareConfiguration = ContentMergeViewer.class.getDeclaredMethod("getCompareConfiguration");
			getCompareConfiguration.setAccessible(true);
			CompareConfiguration cc = (CompareConfiguration) getCompareConfiguration.invoke(textMergeViewer);

			Method getMergeContentProvider = ContentMergeViewer.class.getDeclaredMethod("getMergeContentProvider");
			getMergeContentProvider.setAccessible(true);
			IMergeViewerContentProvider cp = (IMergeViewerContentProvider) getMergeContentProvider.invoke(textMergeViewer);

			Method getSourceViewer = MergeSourceViewer.class.getDeclaredMethod("getSourceViewer");

			Method configureSourceViewer = TextMergeViewer.class.getDeclaredMethod("configureSourceViewer",
					SourceViewer.class, boolean.class);
			configureSourceViewer.setAccessible(true);
			configureSourceViewer.invoke(contentViewer, getSourceViewer.invoke(fLeft), cc.isLeftEditable()
					&& cp.isLeftEditable(textMergeViewer.getInput()));
			configureSourceViewer.invoke(contentViewer, getSourceViewer.invoke(fRight), cc.isRightEditable()
					&& cp.isRightEditable(textMergeViewer.getInput()));

			Field isConfiguredField = TextMergeViewer.class.getDeclaredField("isConfigured");
			isConfiguredField.setAccessible(true);
			isConfiguredField.set(contentViewer, true);
		} catch (Throwable t) {
			// ignore as it may not exist in other versions
		}
	}

	public static void selectAndRevealComment(ITextEditor textEditor, VersionedComment comment, CrucibleFile file) {

		int startLine = comment.getToStartLine();
		if (file.isOldFile()) {
			startLine = comment.getFromStartLine();
		}

		int endLine = comment.getToEndLine();
		if (file.isOldFile()) {
			endLine = comment.getFromEndLine();
		}
		if (endLine == 0) {
			endLine = startLine;
		}
		if (startLine != 0) {
			startLine--;
		}
		selectAndReveal(textEditor, startLine, endLine);

	}

}
