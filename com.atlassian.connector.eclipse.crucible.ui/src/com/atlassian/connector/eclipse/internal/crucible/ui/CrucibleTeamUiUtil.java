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

package com.atlassian.connector.eclipse.internal.crucible.ui;

import com.atlassian.connector.eclipse.team.ui.AtlassianTeamUiPlugin;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector;
import com.atlassian.connector.eclipse.team.ui.TeamUiResourceManager;
import com.atlassian.connector.eclipse.team.ui.TeamUiUtils;
import com.atlassian.connector.eclipse.ui.exceptions.UnsupportedTeamProviderException;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.util.StringUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class CrucibleTeamUiUtil {

	private CrucibleTeamUiUtil() {
	}

	@Nullable
	public static CrucibleFile getCorrespondingCrucibleFileFromEditorInput(IEditorInput editorInput, Review review) {
		if (review == null) {
			return null;
		}

		TeamUiResourceManager teamResourceManager = AtlassianTeamUiPlugin.getDefault().getTeamResourceManager();

		for (ITeamUiResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled() && connector.canHandleEditorInput(editorInput)) {
				CrucibleFile fileInfo;
				try {
					fileInfo = connector.getCrucibleFileFromReview(review, editorInput);
				} catch (UnsupportedTeamProviderException e) {
					return null;
				}
				if (fileInfo != null) {
					return fileInfo;
				}
			}
		}

		try {
			CrucibleFile file = TeamUiUtils.getDefaultConnector().getCrucibleFileFromReview(review, editorInput);
			if (file != null) {
				return file;
			}
		} catch (UnsupportedTeamProviderException e) {
			// ignore
		}

		return getCruciblePreCommitFile(editorInput, review);

	}

	@Nullable
	private static CrucibleFile getCruciblePreCommitFile(IEditorInput editorInput, Review review) {

		if (editorInput instanceof CruciblePreCommitFileInput) {
			CruciblePreCommitFileInput input = (CruciblePreCommitFileInput) editorInput;
			return input.getCrucibleFile();
		} else if (editorInput instanceof FileEditorInput) {

			IFile localFile = ((FileEditorInput) editorInput).getFile();

			return getCruciblePreCommitFile(localFile, review);

		}
		return null;
	}

	public static CrucibleFile getCrucibleFileFromResource(IResource resource, Review review) {
		if (review == null || !(resource instanceof IFile)) {
			return null;
		}

		IFile file = (IFile) resource;

		TeamUiResourceManager teamResourceManager = AtlassianTeamUiPlugin.getDefault().getTeamResourceManager();

		for (ITeamUiResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled() && connector.canHandleFile(file)) {
				CrucibleFile fileInfo;
				try {
					fileInfo = connector.getCrucibleFileFromReview(review, file);
				} catch (UnsupportedTeamProviderException e) {
					return null;
				}
				if (fileInfo != null) {
					return fileInfo;
				}
			}
		}

		try {
			CrucibleFile crucibleFile = TeamUiUtils.getDefaultConnector().getCrucibleFileFromReview(review, file);
			if (crucibleFile != null) {
				return crucibleFile;
			}
		} catch (UnsupportedTeamProviderException e) {
			// ignore
		}

		return getCruciblePreCommitFile(file, review);
	}

	private static CrucibleFile getCruciblePreCommitFile(IFile file, Review review) {
		String localFileUrl = StringUtil.removeLeadingAndTrailingSlashes(file.getFullPath().toString());

		Set<CrucibleFileInfo> reviewFiles;
		try {
			reviewFiles = review.getFiles();
		} catch (ValueNotYetInitialized e) {
			StatusHandler.log(new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID, NLS.bind(
					"Cannot find file {0} in the review {1}", file.getName(), review.getPermId().getId()), e));
			return null;
		}

		for (CrucibleFileInfo cruFile : reviewFiles) {
			String newFileUrl = StringUtil.removeLeadingAndTrailingSlashes(cruFile.getFileDescriptor().getUrl());
			String oldFileUrl = StringUtil.removeLeadingAndTrailingSlashes(cruFile.getOldFileDescriptor().getUrl());

			if (newFileUrl != null && newFileUrl.equals(localFileUrl)) {
				return new CrucibleFile(cruFile, false);
			} else if (oldFileUrl != null && oldFileUrl.equals(localFileUrl)) {
				return new CrucibleFile(cruFile, true);
			}
		}

		return null;
	}
}
