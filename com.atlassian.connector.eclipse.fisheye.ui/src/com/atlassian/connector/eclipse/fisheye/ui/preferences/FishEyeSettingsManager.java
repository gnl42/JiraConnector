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

package com.atlassian.connector.eclipse.fisheye.ui.preferences;

import com.atlassian.connector.eclipse.internal.fisheye.core.FishEyeCorePlugin;
import com.atlassian.connector.eclipse.ui.team.RevisionInfo;
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.source.LineRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FishEyeSettingsManager {
	private final List<FishEyeMappingConfiguration> mappings = MiscUtil.buildArrayList();

	public FishEyeSettingsManager() {
		mappings.add(new FishEyeMappingConfiguration("https://studio.atlassian.com/svn/ACC/",
				"https://studio.atlassian.com/source/", "ACC"));
		mappings.add(new FishEyeMappingConfiguration("https://studio.atlassian.com/svn/PLE/",
				"https://studio.atlassian.com/source/", "PLE"));

	}

	public List<FishEyeMappingConfiguration> getMappings() {
		return mappings;
	}

	@Nullable
	public FishEyeMappingConfiguration getConfiguration(@NotNull RevisionInfo revisionInfo) throws CoreException {
		for (FishEyeMappingConfiguration cfgEntry : mappings) {
			if (revisionInfo.getScmPath().startsWith(cfgEntry.getScmPath())) {
				return cfgEntry;
			}
		}
		return null;
	}

	@NotNull
	public String buildFishEyeUrl(@NotNull IResource resource, @Nullable LineRange lineRange) throws CoreException {
		final RevisionInfo revisionInfo = TeamUiUtils.getLocalRevision(resource);
		if (revisionInfo == null) {
			throw new CoreException(new Status(IStatus.ERROR, FishEyeCorePlugin.PLUGIN_ID,
					"Cannot determine locally checked-out revision for resource " + resource.getFullPath()));
		}

		final FishEyeMappingConfiguration cfg = getConfiguration(revisionInfo);
		if (cfg == null) {
			throw new CoreException(new Status(IStatus.ERROR, FishEyeCorePlugin.PLUGIN_ID,
					"Cannot find matching FishEye configuration for SCM path " + revisionInfo.getScmPath()));
		}

		final String scmPath = revisionInfo.getScmPath();
		final String cfgScmPath = cfg.getScmPath();
		if (scmPath.startsWith(cfgScmPath) == false) {
			throw new CoreException(new Status(IStatus.ERROR, FishEyeCorePlugin.PLUGIN_ID,
					"Cannot find matching FishEye repository for " + revisionInfo));
		}
		final String path = scmPath.substring(cfgScmPath.length());
		final String repo = cfg.getFishEyeRepo();
		final String fishEyeUrl = cfg.getFishEyeServer();
		final StringBuilder res = new StringBuilder(fishEyeUrl);
		if (res.length() > 0 && res.charAt(res.length() - 1) != '/') {
			res.append('/');
		}
		res.append("browse/");
		res.append(repo);
		if (!path.startsWith("/")) {
			res.append('/');
		}
		res.append(path);
		if (revisionInfo.getRevision() != null) {
			res.append("?r=");
			res.append(revisionInfo.getRevision());
		}
		if (lineRange != null) {
			res.append("#l").append(lineRange.getStartLine());
		}
		return res.toString();
	}
}
