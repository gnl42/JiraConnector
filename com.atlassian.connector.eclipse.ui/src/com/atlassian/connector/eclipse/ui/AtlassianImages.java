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

//	private static final String TOOL = "tool16";
//
//	public static final ImageDescriptor DELETE = create(TOOL, "delete.gif");

	public static final ImageDescriptor ATLASSIAN_LOGO = create("misc", "Atlassian.png");

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
