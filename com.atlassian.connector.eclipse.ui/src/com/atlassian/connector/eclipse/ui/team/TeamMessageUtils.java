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

import com.atlassian.connector.eclipse.ui.exceptions.UnsupportedTeamProviderException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
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
		String message = "Unable to open file.  Please check that:\n\n"
				+ "- The enclosing project the project is checked out at the latest revision.\n"
				+ "- The file has not been moved or deleted since the creation of the review\n"
				+ "- You are using a supported team provider. Supported providers: Subclipse";

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

	public static void openCouldNotOpenFileErrorMessage(final String repoUrl, final String filePath,
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
		String message = "Unable to open file.  Please check that:\n\n"
				+ "- The enclosing project the project is checked out at the latest revision.\n"
				+ "- The file has not been moved or deleted since the creation of the review\n"
				+ "- You are using a supported team provider. Supported providers: Subclipse";

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

	public static void openUnsupportedTeamProviderErrorMessage(final UnsupportedTeamProviderException exception) {
		if (Display.getCurrent() != null) {
			internalOpenUnsupportedTeamProviderErrorMessage(exception);
		} else {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					internalOpenUnsupportedTeamProviderErrorMessage(exception);
				}
			});
		}
	}

	private static void internalOpenUnsupportedTeamProviderErrorMessage(final UnsupportedTeamProviderException exception) {

		String message = NLS.bind("Unsupported team provider ({0}).\n\n"
				+ "Subclipse is the supported team provider for accessing SVN repositories."
				+ "\nSee the Atlassian Eclipse Connector web site for future updates.", exception.getMessage());

		MessageDialog.openWarning(null, MESSAGE_DIALOG_TITLE, message);
	}

	private static void internalOpenUnableToCompareErrorMessage(String repoUrl, String filePath, String oldRevision,
			String newRevision) {
		String message = "Unable to compare revisions.  Please check that:\n\n"
				+ "- The enclosing project the project is checked out at the latest revision.\n"
				+ "- The file has not been moved or deleted since the creation of the review\n"
				+ "- You are using a supported team provider. Supported providers: Subclipse";

		MessageDialog.openInformation(null, MESSAGE_DIALOG_TITLE, message);
	}
}
