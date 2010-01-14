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

package com.atlassian.connector.eclipse.internal.crucible.ui.actions;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.team.ui.AtlassianTeamUiPlugin;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector;
import com.atlassian.connector.eclipse.team.ui.TeamConnectorType;
import com.atlassian.connector.eclipse.team.ui.TeamUiUtils;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector.State;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("restriction")
public class CreateReviewFromEditorSelectionAction extends TeamAction implements IEditorActionDelegate {

	private IEditorPart editor;

	private TextSelection selection;

	private IFile resource;

	public CreateReviewFromEditorSelectionAction() {
	}

	@Override
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {

		if (!TeamUiUtils.checkTeamConnectors()) {
			// no connectors at all
			return;
		}

		ITeamUiResourceConnector connector = AtlassianTeamUiPlugin.getDefault()
				.getTeamResourceManager()
				.getTeamConnector(resource);

		if (connector == null) {
			MessageDialog.openInformation(getShell(), CrucibleUiPlugin.PLUGIN_ID,
					"Cannot find Atlassian SCM Integration for '" + resource.getName() + "'.");
			return;
		} else if (connector.getType() != TeamConnectorType.SVN) {
			MessageDialog.openInformation(getShell(), CrucibleUiPlugin.PLUGIN_ID,
					"Cannot create review from non Subversion resource. Only Subversion is supported.");
			return;
		}

		boolean isPostCommit = false;

		if (connector.isResourceAcceptedByFilter(resource, State.SF_VERSIONED)
				&& !connector.isResourceAcceptedByFilter(resource, State.SF_ANY_CHANGE)) {
			// versioned and committed file found (without any local change)
			// we need Crucible 2.1 to add such file to the review
			isPostCommit = true;
		}

		openReviewWizard(resource, editor, connector, isPostCommit);

	}

	private void openReviewWizard(final IResource resource, final IEditorPart editorPart,
			final ITeamUiResourceConnector connector, boolean isPostCommit) {

//		final HashMap<IEditorInput, LineRange> comments = new HashMap<IEditorInput, LineRange>();
//		comments.put(editorPart.getEditorInput(), TeamUiUtils.getSelectedLineNumberRangeFromEditorInput(editorPart,
//				editor.getEditorInput()));
//
//		if (isPostCommit) {
//			SelectCrucible21RepositoryPage selectRepositoryPage = new SelectCrucible21RepositoryPage() {
//				@Override
//				protected IWizard createWizard(TaskRepository taskRepository) {
//					ReviewWizard wizard = new ReviewWizard(taskRepository, MiscUtil.buildHashSet(
//							ReviewWizard.Type.ADD_SCM_RESOURCES, ReviewWizard.Type.ADD_COMMENT_TO_FILE));
//					wizard.setRoots(Arrays.asList(resource));
//					wizard.setFilesCommentData(comments);
//					return wizard;
//				}
//			};
//
//			WizardDialog wd = new WizardDialog(WorkbenchUtil.getShell(), new CrucibleRepositorySelectionWizard(
//					selectRepositoryPage));
//			wd.setBlockOnOpen(true);
//			wd.open();
//
//		} else {
//
//			Collection<UploadItem> uploadItems;
//			try {
//				uploadItems = connector.getUploadItemsForResources(new IResource[] { resource },
//						new NullProgressMonitor());
//			} catch (CoreException e) {
//				MessageDialog.openInformation(getShell(), CrucibleUiPlugin.PLUGIN_ID, "Cannot create UploadItem for '"
//						+ resource.getName() + "'.");
//				return;
//			}
//
//			final List<UploadItem> items = new ArrayList<UploadItem>(uploadItems);
//
//			SelectCrucibleRepositoryPage selectRepositoryPage = new SelectCrucibleRepositoryPage(
//					SelectCrucibleRepositoryPage.ENABLED_CRUCIBLE_REPOSITORY_FILTER) {
//				@Override
//				protected IWizard createWizard(TaskRepository taskRepository) {
//					ReviewWizard wizard = new ReviewWizard(taskRepository, MiscUtil.buildHashSet(
//							ReviewWizard.Type.ADD_UPLOAD_ITEMS, ReviewWizard.Type.ADD_COMMENT_TO_FILE));
//
//					wizard.setUploadItems(items);
//					wizard.setFilesCommentData(comments);
//					return wizard;
//				}
//			};
//
//			// skip repository selection wizard page if there is only one repository on the list
//			List<TaskRepository> taskRepositories = selectRepositoryPage.getTaskRepositories();
//			WizardDialog wd = null;
//			if (taskRepositories.size() != 1) {
//				wd = new WizardDialog(WorkbenchUtil.getShell(), new RepositorySelectionWizard(selectRepositoryPage));
//			} else {
//				ReviewWizard wizard = new ReviewWizard(taskRepositories.get(0), MiscUtil.buildHashSet(
//						ReviewWizard.Type.ADD_UPLOAD_ITEMS, ReviewWizard.Type.ADD_COMMENT_TO_FILE));
//				wizard.setUploadItems(items);
//				wd = new WizardDialog(WorkbenchUtil.getShell(), wizard);
//			}
//			wd.setBlockOnOpen(true);
//			wd.open();
//		}
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.editor = targetEditor;
		if (targetEditor != null) {
			this.resource = (IFile) targetEditor.getEditorInput().getAdapter(IFile.class);
		}
//		action.setEnabled(false);
		action.setEnabled(true);
	}

	// TODO jj remove old actions both the java files and plugin.xml entries

	// TODO jj check if selection field is necessary or should be passed further instead of IEditorInput
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof TextSelection) {
			TextSelection sel = (TextSelection) selection;
//			action.setEnabled(sel.getLength() > 0);
			action.setEnabled(true);
			this.selection = sel;
		}
	}

}
