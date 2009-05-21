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
import com.atlassian.connector.eclipse.ui.exceptions.UnsupportedTeamProviderException;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.internal.MergeSourceViewer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.LineRange;
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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * A utility class for doing UI related operations for team items
 * 
 * @author Shawn Minto
 */
public final class TeamUiUtils {

	public static final String TEAM_PROVIDER_ID_CVS_ECLIPSE = "org.eclipse.team.cvs.core.cvsnature";

	public static final String TEAM_PROV_ID_SVN_SUBCLIPSE = "org.tigris.subversion.subclipse.core.svnnature";

	public static final String TEAM_PROV_ID_SVN_SUBVERSIVE = "org.eclipse.team.svn.core.svnnature";

	private static DefaultTeamResourceConnector defaultConnector = new DefaultTeamResourceConnector();

	private TeamUiUtils() {
	}

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
					return connector.openFile(repoUrl, filePath, otherRevisionFilePath, revisionString,
							otherRevisionString, monitor);
				} catch (CoreException e) {
					//ignore and try with default
					System.err.println();
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

	public static SortedSet<Long> getRevisionsForFile(IFile file, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(file);
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();

		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled()) {
				try {
					return connector.getRevisionsForFile(file, monitor);
				} catch (CoreException e) {
					// ignore and try other connector(s)
				}
			}
		}
		return defaultConnector.getRevisionsForFile(file, monitor);
	}

	private interface ConnectorOperation<T> {
		T execute(ITeamResourceConnector connector, IProgressMonitor monitor) throws CoreException;
	}

	private static <T> T executeOnConnectors(ConnectorOperation<T> operation, IProgressMonitor monitor)
			throws CoreException {
		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();

		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled()) {
				try {
					return operation.execute(connector, monitor);
				} catch (CoreException e) {
					// ignore and try other connector(s)
				}
			}
		}
		return operation.execute(defaultConnector, monitor);
	}

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
	public static Map<CustomRepository, SortedSet<ICustomChangesetLogEntry>> getAllChangesets(String repositoryUrl,
			int limit, IProgressMonitor monitor, MultiStatus status) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();
		Map<CustomRepository, SortedSet<ICustomChangesetLogEntry>> toReturn;
		toReturn = new HashMap<CustomRepository, SortedSet<ICustomChangesetLogEntry>>();
		monitor.beginTask("Retrieving changesets", teamResourceManager.getTeamConnectors().size() + 1);
		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			IProgressMonitor subMonitor = Policy.subMonitorFor(monitor, 1);
			if (connector.isEnabled()) {
				try {
					toReturn.putAll(connector.getLatestChangesets(repositoryUrl, limit, subMonitor, status));
				} catch (CoreException e) {
					status.add(e.getStatus());
				}
			}
			subMonitor.done();
		}
		IProgressMonitor subMonitor = Policy.subMonitorFor(monitor, 1);
		try {
			toReturn.putAll(defaultConnector.getLatestChangesets(repositoryUrl, limit, subMonitor, status));
		} catch (CoreException e) {
			status.add(e.getStatus());
		}
		subMonitor.done();
		monitor.done();
		//if no changeset was retrieved and errors occurred, return errors
		if (toReturn.size() == 0 && status.getChildren().length > 0) {
			throw new CoreException(status);
		}
		return toReturn;
	}

	public static Map<IFile, SortedSet<Long>> getRevisionsForFile(List<IFile> files, IProgressMonitor monitor)
			throws CoreException {
		Assert.isNotNull(files);
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();

		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled()) {
				try {
					return connector.getRevisionsForFile(files, monitor);
				} catch (CoreException e) {
					// ignore and try other connector(s)
				}
			}
		}
		return defaultConnector.getRevisionsForFile(files, monitor);
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
					//ignore and try with default
				}
			}
		}
		try {
			if (!defaultConnector.openCompareEditor(repoUrl, filePath, otherRevisionFilePath, oldRevisionString,
					newRevisionString, annotationModel, monitor)) {
				TeamMessageUtils.openUnableToCompareErrorMessage(repoUrl, filePath, oldRevisionString,
						newRevisionString);
			}
		} catch (UnsupportedTeamProviderException e) {
			TeamMessageUtils.openUnsupportedTeamProviderErrorMessage(e);
		} catch (CoreException e) {
			TeamMessageUtils.openUnableToCompareErrorMessage(repoUrl, filePath, oldRevisionString, newRevisionString);
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
					"Editor is not an ITextEditor or editor inputs not equal" + editor));
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
			return defaultConnector.getCorrespondingCrucibleFileFromEditorInput(editorInput, activeReview);
		} catch (UnsupportedTeamProviderException e) {
			return null;
		}
	}

	public static RevisionInfo getLocalRevision(@NotNull IResource resource) throws CoreException {
		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();

		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled()) {
				RevisionInfo res = connector.getLocalRevision(resource);
				if (res != null) {
					return res;
				}
			}
		}
		return defaultConnector.getLocalRevision(resource);

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
			} catch (Throwable t) {
				StatusHandler.log(new Status(IStatus.WARNING, AtlassianUiPlugin.PLUGIN_ID,
						"Could not initialize annotation model for " + input.getName(), t));
			}
		}
		return contentViewer;
	}
}
