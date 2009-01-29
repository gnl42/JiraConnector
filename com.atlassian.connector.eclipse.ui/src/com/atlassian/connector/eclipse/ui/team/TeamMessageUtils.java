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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * Class to handle displaying team error messages to users
 * 
 * @author Shawn Minto
 */
public final class TeamMessageUtils {

	private TeamMessageUtils() {
		// ignore
	}

	private static final String MESSAGE_DIALOG_TITLE = "Crucible";

	public static void openFileDeletedErrorMessage(final String repoUrl, final String filePath, final String revision) {
		if (Display.getCurrent() != null) {
			internalOpenFileDeletedErrorMessage(repoUrl, filePath, revision);
		} else {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					internalOpenFileDeletedErrorMessage(repoUrl, filePath, revision);
				}
			});
		}

	}

	private static void internalOpenFileDeletedErrorMessage(String repoUrl, String filePath, String revision) {
		String fileUrl = (repoUrl != null ? repoUrl : "") + filePath;
		String message = "Please update the project to revision " + revision
				+ " as the following file may have been removed or deleted:\n\n" + fileUrl;

		MessageDialog.openInformation(null, MESSAGE_DIALOG_TITLE, message);
	}

	public static void openFileDoesntExistErrorMessage(final String repoUrl, final String filePath,
			final String revision) {
		if (Display.getCurrent() != null) {
			internalOpenFileDoesntExistErrorMessage(repoUrl, filePath, revision);
		} else {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					internalOpenFileDoesntExistErrorMessage(repoUrl, filePath, revision);
				}
			});
		}
	}

	private static void internalOpenFileDoesntExistErrorMessage(String repoUrl, String filePath, String revision) {

		String fileUrl = (repoUrl != null ? repoUrl : "") + filePath;
		String message = "Please update the project to revision " + revision
				+ " as the following file may have been removed or deleted:\n\n" + fileUrl;

		MessageDialog.openInformation(null, MESSAGE_DIALOG_TITLE, message);
	}

	public static void openUnableToCompareErrorMessage(final String repoUrl, final String filePath,
			final String oldRevision, final String newRevision) {
		if (Display.getCurrent() != null) {
			internalOpenUnableToCompareErrorMessage(repoUrl, filePath, oldRevision, newRevision);
		} else {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					internalOpenUnableToCompareErrorMessage(repoUrl, filePath, oldRevision, newRevision);
				}
			});
		}
	}

	private static void internalOpenUnableToCompareErrorMessage(String repoUrl, String filePath, String oldRevision,
			String newRevision) {

		String fileUrl = (repoUrl != null ? repoUrl : "") + filePath;
		String message = "Unable to compare revisions.  Please update the project to revision " + newRevision
				+ " as the following file may have been removed or deleted:\n\n" + fileUrl;

		MessageDialog.openInformation(null, MESSAGE_DIALOG_TITLE, message);
	}

	public static void openNotTeamResourceErrorMessage(final String repoUrl, final String filePath,
			final String revision) {
		if (Display.getCurrent() != null) {
			internalOpenNotTeamResourceErrorMessage(repoUrl, filePath, revision);
		} else {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					internalOpenNotTeamResourceErrorMessage(repoUrl, filePath, revision);
				}
			});
		}
	}

	private static void internalOpenNotTeamResourceErrorMessage(String repoUrl, String filePath, String revision) {
		String fileUrl = repoUrl != null ? repoUrl : "" + filePath;
		String message = "Please checkout the project as the following file is not managed by a team provider:\n\n"
				+ fileUrl;

		MessageDialog.openWarning(null, MESSAGE_DIALOG_TITLE, message);
	}
}
