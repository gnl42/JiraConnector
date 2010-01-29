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

package com.atlassian.connector.eclipse.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Common class for getting and creating images
 * 
 * @author sminto
 */
public final class AtlassianImages {

	private static ImageRegistry imageRegistry;

	private static final URL BASE_URL = AtlassianUiPlugin.getDefault().getBundle().getEntry("/icons/");

	private static final String T_OBJ = "obj16"; //$NON-NLS-1$

	private static final String T_OVR = "ovr16"; //$NON-NLS-1$

	private static final String T_ECL = "elcl16";

	public static final ImageDescriptor ATLASSIAN_LOGO = create("misc", "Atlassian.png");

	public static final ImageDescriptor IMG_FLAT_MODE = create(T_OBJ, "flatLayout.gif"); //$NON-NLS-1$

	public static final ImageDescriptor IMG_TREE_MODE = create(T_OBJ, "treeLayout.gif"); //$NON-NLS-1$

	public static final ImageDescriptor IMG_COMPRESSED_FOLDER_MODE = create(T_OBJ, "compressedLayout.gif"); //$NON-NLS-1$

	public static final ImageDescriptor IMG_FILE_CHANGED = create(T_OVR, "fileChanged_ov.gif"); //$NON-NLS-1$

	public static final ImageDescriptor IMG_LINK_WITH_EDITOR = create(T_ECL, "synced.gif"); //$NON-NLS-1$

	private AtlassianImages() {
	}

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

	private static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
		}

		return imageRegistry;
	}

	public static Image getImage(ImageDescriptor imageDescriptor) {
		ImageRegistry registry = getImageRegistry();

		Image image = registry.get("" + imageDescriptor.hashCode());
		if (image == null) {
			image = imageDescriptor.createImage();
			registry.put("" + imageDescriptor.hashCode(), image);
		}
		return image;
	}

}
