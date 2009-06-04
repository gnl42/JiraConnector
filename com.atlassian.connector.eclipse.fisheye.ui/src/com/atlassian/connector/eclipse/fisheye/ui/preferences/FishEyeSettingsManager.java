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
import com.atlassian.connector.eclipse.internal.fisheye.ui.FishEyeUiPlugin;
import com.atlassian.connector.eclipse.ui.team.RevisionInfo;
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 * This class uses somewhat crazy approach for persisting preferences - XML is serialized to a string and then such
 * string is put into properties using standard {@link IPreferenceStore} interface. Such approach is also used by other
 * standard Eclipse plugins - e.g. JDT uses it for persisting JRE configuration.
 * 
 * @author Wojciech Seliga
 */
public class FishEyeSettingsManager {
	private static final String MAPPING_ELEMENT = "mapping";

	private static final String ROOT_ELEMENT = "mappings";

	private static final String SCM_PATH = "scmPath";

	private static final String FISHEYE_SERVER = "fishEyeServer";

	private static final String FISHEYE_REPO = "fishEyeRepo";

	private final List<FishEyeMappingConfiguration> mappings = MiscUtil.buildArrayList();

	private final IPreferenceStore preferenceStore;

	private static final String PREFERENCE_MAPPINGS = ROOT_ELEMENT;

	public FishEyeSettingsManager(IPreferenceStore preferenceStore) {
		this.preferenceStore = preferenceStore;
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
		final String mappingsStr = preferenceStore.getString(PREFERENCE_MAPPINGS);
		if (mappingsStr == null || mappingsStr.length() == 0) {
			return;
		}
		try {
			final Document document = new SAXBuilder().build(new StringReader(mappingsStr));
			@SuppressWarnings("unchecked")
			final List<Element> mappingElements = document.getRootElement().getChildren();
			for (Element element : mappingElements) {
				if (element.getName().equals(MAPPING_ELEMENT)) {
					mappings.add(new FishEyeMappingConfiguration(element.getAttributeValue(SCM_PATH),
							element.getAttributeValue(FISHEYE_SERVER), element.getAttributeValue(FISHEYE_REPO)));
				}
			}
		} catch (JDOMException e) {
			StatusHandler.log(new Status(IStatus.WARNING, FishEyeUiPlugin.PLUGIN_ID,
					"Error while loading FishEye preferences", e));
		} catch (IOException e) {
			StatusHandler.log(new Status(IStatus.WARNING, FishEyeUiPlugin.PLUGIN_ID,
					"Error while loading FishEye preferences", e));
		}
	}

	@SuppressWarnings("unchecked")
	public void save() throws IOException {
		final Element root = new Element(ROOT_ELEMENT);
		for (FishEyeMappingConfiguration mapping : mappings) {
			final Element mappingElem = new Element(MAPPING_ELEMENT);
			mappingElem.setAttribute(SCM_PATH, mapping.getScmPath());
			mappingElem.setAttribute(FISHEYE_SERVER, mapping.getFishEyeServer());
			mappingElem.setAttribute(FISHEYE_REPO, mapping.getFishEyeRepo());
			root.getChildren().add(mappingElem);
		}
		final StringWriter sw = new StringWriter();
		new XMLOutputter().output(root, sw);
		preferenceStore.setValue(PREFERENCE_MAPPINGS, sw.toString());
		if (preferenceStore instanceof IPersistentPreferenceStore) {
			((IPersistentPreferenceStore) preferenceStore).save();
		}
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
	public String buildFishEyeUrl(@NotNull IResource resource, @Nullable LineRange lineRange) throws CoreException,
			NoMatchingFishEyeConfigurationException {
		final RevisionInfo revisionInfo = TeamUiUtils.getLocalRevision(resource);
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
		final String fishEyeUrl = cfg.getFishEyeServer();
		final StringBuilder res = new StringBuilder(fishEyeUrl);
		if (res.length() > 0 && res.charAt(res.length() - 1) != '/') {
			res.append('/');
		}
		res.append("browse/");

		final boolean isBinary = revisionInfo.isBinary() != null && revisionInfo.isBinary();

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
