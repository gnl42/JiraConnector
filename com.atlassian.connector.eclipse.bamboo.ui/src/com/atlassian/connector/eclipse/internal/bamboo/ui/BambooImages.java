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

package com.atlassian.connector.eclipse.internal.bamboo.ui;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.Bundle;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Steffen Pingel
 */
public class BambooImages {

	private static final URL BASE_URL = BambooUiPlugin.getDefault().getBundle().getEntry("/icons/"); //$NON-NLS-1$

	private static final IPath ICONS_PATH = new Path("$nl$/icons"); //$NON-NLS-1$

	private static final String T_OBJ = "obj16"; //$NON-NLS-1$

	public static final ImageDescriptor STATUS_DISABLED = create(T_OBJ, "icn_plan_disabled.gif"); //$NON-NLS-1$

	public static final ImageDescriptor STATUS_FAILED = create(T_OBJ, "icn_plan_failed.gif"); //$NON-NLS-1$

	public static final ImageDescriptor STATUS_PASSED = create(T_OBJ, "icn_plan_passed.gif"); //$NON-NLS-1$

	public static final ImageDescriptor[] STATUS_BUILDING = { create(T_OBJ, "icn_building_1.gif"),
			create(T_OBJ, "icn_building_2.gif"), create(T_OBJ, "icn_building_3.gif"),
			create(T_OBJ, "icn_building_4.gif"), create(T_OBJ, "icn_building_5.gif"),
			create(T_OBJ, "icn_building_6.gif"), create(T_OBJ, "icn_building_7.gif"),
			create(T_OBJ, "icn_building_8.gif") };

	public static final ImageDescriptor VIEW_STATUS_WARNING = create(T_OBJ, "bamboo_warning.png"); //$NON-NLS-1$

	public static final ImageDescriptor VIEW_STATUS_FAILED = create(T_OBJ, "bamboo_error.png"); //$NON-NLS-1$

	public static final ImageDescriptor VIEW_STATUS_PASSED = create(T_OBJ, "bamboo_success.png"); //$NON-NLS-1$

	public static final ImageDescriptor BAMBOO = create(T_OBJ, "bamboo.png"); //$NON-NLS-1$

	public static final ImageDescriptor CONSOLE = create(T_OBJ, "console_view.gif"); //$NON-NLS-1$

	public static final ImageDescriptor JUNIT = create(T_OBJ, "junit.gif"); //$NON-NLS-1$

	public static final ImageDescriptor RUN_BUILD = create(T_OBJ, "run_exc.gif"); //$NON-NLS-1$

	public static final ImageDescriptor ADD_REPOSITORY = create(T_OBJ, "repository-new.gif"); //$NON-NLS-1$

	public static final ImageDescriptor REPOSITORY = create(T_OBJ, "bamboo_repository.gif"); //$NON-NLS-1$

	public static final ImageDescriptor REPOSITORIES = create(T_OBJ, "repositories.gif"); //$NON-NLS-1$

	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static URL makeIconFileURL(String prefix, String name) throws MalformedURLException {
		if (BASE_URL == null) {
			throw new MalformedURLException();
		}

		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append('/');
		buffer.append(name);
		return new URL(BASE_URL, buffer.toString());
	}

	public static ImageDescriptor getImageDescriptor(String relativePath) {
		IPath path = ICONS_PATH.append(relativePath);
		return createImageDescriptor(BambooUiPlugin.getDefault().getBundle(), path, true);
	}

	private static ImageDescriptor createImageDescriptor(Bundle bundle, IPath path, boolean useMissingImageDescriptor) {
		URL url = FileLocator.find(bundle, path, null);
		if (url != null) {
			return ImageDescriptor.createFromURL(url);
		}
		if (useMissingImageDescriptor) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
		return null;
	}

}
