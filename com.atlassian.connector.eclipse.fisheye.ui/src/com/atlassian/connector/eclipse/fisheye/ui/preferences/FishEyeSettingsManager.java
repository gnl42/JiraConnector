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

import com.atlassian.connector.eclipse.fisheye.ui.FishEyeUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.TaskRepositoryUtil;
import com.atlassian.connector.eclipse.internal.fisheye.core.FishEyeCorePlugin;
import com.atlassian.connector.eclipse.team.ui.LocalStatus;
import com.atlassian.connector.eclipse.team.ui.TeamUiUtils;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class uses somewhat crazy approach for persisting preferences - XML is serialized to a string and then such
 * string is put into properties using standard {@link IPreferenceStore} interface. Such approach is also used by other
 * standard Eclipse plugins - e.g. JDT uses it for persisting JRE configuration.
 *
 * @author Wojciech Seliga
 */
public class FishEyeSettingsManager {

	private final List<FishEyeMappingConfiguration> mappings = MiscUtil.buildArrayList();

	public FishEyeSettingsManager(IPreferenceStore preferenceStore) {
		load();
	}

	/**
	 *
	 * @return live copy of mappings
	 */
	public List<FishEyeMappingConfiguration> getMappings() {
		return mappings;
	}

	public void setMappings(@NotNull List<FishEyeMappingConfiguration> newMappings) {
		if (newMappings == null) {
			throw new IllegalArgumentException("List cannot be null");
		}
		mappings.clear();
		mappings.addAll(newMappings);
	}

	private void load() {
		Set<TaskRepository> fishEyeRepositories = FishEyeUiUtil.getFishEyeServers();
		for (TaskRepository tr : fishEyeRepositories) {
			Map<String, String> scmRepositoryMappings = TaskRepositoryUtil.getScmRepositoryMappings(tr);
			for (String scmPath : scmRepositoryMappings.keySet()) {
				mappings.add(new FishEyeMappingConfiguration(tr, scmPath, scmRepositoryMappings.get(scmPath)));
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void save() throws IOException {
		Map<TaskRepository, Map<String, String>> scmRepositoryMappings = MiscUtil.buildHashMap();
		for (FishEyeMappingConfiguration mapping : mappings) {
			if (!scmRepositoryMappings.containsKey(mapping.getTaskRepository())) {
				scmRepositoryMappings.put(mapping.getTaskRepository(), new HashMap<String, String>());
			}

			scmRepositoryMappings.get(mapping.getTaskRepository()).put(mapping.getScmPath(), mapping.getFishEyeRepo());
		}

		for (TaskRepository taskRepository : scmRepositoryMappings.keySet()) {
			TaskRepositoryUtil.setScmRepositoryMappings(taskRepository, scmRepositoryMappings.get(taskRepository));
		}
	}

	@Nullable
	public FishEyeMappingConfiguration getConfiguration(@NotNull LocalStatus revisionInfo) throws CoreException {
		for (FishEyeMappingConfiguration cfgEntry : mappings) {
			if (revisionInfo.getScmPath().startsWith(cfgEntry.getScmPath())) {
				return cfgEntry;
			}
		}
		return null;
	}

	@NotNull
	public String buildFishEyeUrl(@NotNull IResource resource, @Nullable LineRange lineRange) throws CoreException,
			NoMatchingFishEyeConfigurationException {
		final LocalStatus revisionInfo = TeamUiUtils.getLocalRevision(resource);
		if (revisionInfo == null) {
			throw new CoreException(new Status(IStatus.ERROR, FishEyeCorePlugin.PLUGIN_ID,
					"Cannot determine locally checked-out revision for resource " + resource.getFullPath()));
		}

		final String scmPath = revisionInfo.getScmPath();
		final FishEyeMappingConfiguration cfg = getConfiguration(revisionInfo);
		if (cfg == null) {
			throw new NoMatchingFishEyeConfigurationException(new Status(IStatus.ERROR, FishEyeCorePlugin.PLUGIN_ID,
					"Cannot find matching FishEye repository for " + revisionInfo), scmPath);
		}

		final String cfgScmPath = cfg.getScmPath();

		// for binary resources the URL is like: $SERVER_URL/browse/~raw,r=$REVISION/$REPO/$PATH

		final String path = scmPath.substring(cfgScmPath.length());
		final String repo = cfg.getFishEyeRepo();
		final String fishEyeUrl = cfg.getTaskRepository().getRepositoryUrl();
		final StringBuilder res = new StringBuilder(fishEyeUrl);
		if (res.length() > 0 && res.charAt(res.length() - 1) != '/') {
			res.append('/');
		}
		res.append("browse/");

		final boolean isBinary = revisionInfo.isBinary();

		if (isBinary && revisionInfo.getRevision() != null) {
			res.append("~r=");
			res.append(revisionInfo.getRevision());
			res.append("/");
		}

		res.append(repo);
		if (!path.startsWith("/")) {
			res.append('/');
		}
		res.append(path);
		if (!isBinary) {
			if (revisionInfo.getRevision() != null) {
				res.append("?r=");
				res.append(revisionInfo.getRevision());
			}
			if (lineRange != null) {
				res.append("#l").append(lineRange.getStartLine());
			}
		}
		return res.toString();
	}
}
