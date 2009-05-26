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

package com.atlassian.connector.eclipse.internal.fisheye.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Wojciech Seliga
 */
public final class FishEyeImages {

	private static ImageRegistry imageRegistry;

	private static final URL BASE_URL = FishEyeUiPlugin.getDefault().getBundle().getEntry("/icons/"); //$NON-NLS-1$

	private static final String T_OBJ = "obj16"; //$NON-NLS-1$

	private static final String T_WIZBAN = "wizban"; //$NON-NLS-1$

	public static final ImageDescriptor FISHEYE_ICON = create(T_OBJ, "fisheye-16.png"); //$NON-NLS-1$

	public static final ImageDescriptor FISHEYE_WIZ_BAN_ICON = create(T_WIZBAN, "fisheye-logo.png"); //$NON-NLS-1$

	private FishEyeImages() {
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
