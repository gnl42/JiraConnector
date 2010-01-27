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

package com.atlassian.connector.eclipse.internal.fisheye.ui.action;

import com.atlassian.connector.eclipse.fisheye.ui.FishEyeUiUtil;
import com.atlassian.connector.eclipse.fisheye.ui.preferences.FishEyePreferenceContextData;
import com.atlassian.connector.eclipse.fisheye.ui.preferences.NoMatchingFishEyeConfigurationException;
import com.atlassian.connector.eclipse.fisheye.ui.preferences.SourceRepositoryMappingPreferencePage;
import com.atlassian.connector.eclipse.internal.fisheye.ui.FishEyeUiPlugin;
import com.atlassian.connector.eclipse.internal.fisheye.ui.dialogs.ErrorDialogWithHyperlink;
import com.atlassian.connector.eclipse.team.ui.LocalStatus;
import com.atlassian.connector.eclipse.team.ui.ScmRepository;
import com.atlassian.connector.eclipse.team.ui.TeamUiUtils;
import com.atlassian.connector.eclipse.ui.actions.AbstractResourceAction;
import com.atlassian.connector.eclipse.ui.commons.ResourceEditorBean;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractFishEyeLinkAction extends AbstractResourceAction {

	public AbstractFishEyeLinkAction(String text) {
		super(text);
	}

	protected abstract void processUrl(String url);

	@Override
	protected boolean updateSelection(IStructuredSelection structuredSelection) {
		if (!super.updateSelection(structuredSelection)) {
			return false;
		}

		boolean isEnabled = false;
		List<ResourceEditorBean> selection = getSelectionData();

		if (selection != null && selection.size() == 1
				&& selection.get(0) != null && selection.get(0).getResource() != null) {
			IResource resource = selection.get(0).getResource();

			try {
				if (TeamUiUtils.hasNoTeamConnectors()) {
					isEnabled = true;
				} else {
					LocalStatus status = TeamUiUtils.getLocalRevision(resource);
					if (status != null && status.isVersioned()) {
						isEnabled = true;
					}
				}
			} catch (CoreException e) {
				isEnabled = false;
			}
		}
		return isEnabled;
	}

	@Override
	protected void processResources(@NotNull List<ResourceEditorBean> selection, final Shell shell) {
		if (!TeamUiUtils.checkTeamConnectors()) {
			return;
		}

		if (selection.get(0) != null) {
			IResource resource = selection.get(0).getResource();
			LineRange lineRange = selection.get(0).getLineRange();
			if (resource != null) {
				try {
					final String url = FishEyeUiUtil.buildFishEyeUrl(resource, lineRange);
					processUrl(url);
				} catch (final NoMatchingFishEyeConfigurationException e) {
					final ScmRepository repoInfo = TeamUiUtils.getApplicableRepository(resource);
					String scmPathToConfigure = (repoInfo != null) ? repoInfo.getScmPath() : null;
					handleException(resource, shell, e.getMessage(), new FishEyePreferenceContextData(
							scmPathToConfigure));
				} catch (CoreException e) {
					handleException(resource, shell, e.getMessage(), null);
				}
			}
		}
	}

	private void handleException(IResource resource, final Shell shell, String message,
			@Nullable final FishEyePreferenceContextData contextData) {
		new ErrorDialogWithHyperlink(shell, FishEyeUiPlugin.PRODUCT_NAME, "Cannot build FishEye URL for "
				+ resource.getName() + ": " + message, "<a>Configure FishEye Settings</a>", new Runnable() {
			public void run() {
				final PreferenceDialog prefDialog = PreferencesUtil.createPreferenceDialogOn(shell,
						SourceRepositoryMappingPreferencePage.ID, null, contextData);
				if (prefDialog != null) {
					prefDialog.open();
				}
			}
		}).open();

	}
}