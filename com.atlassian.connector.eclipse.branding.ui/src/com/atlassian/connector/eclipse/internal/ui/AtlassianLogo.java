/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import com.atlassian.connector.eclipse.internal.branding.ui.AtlassianBrandingPlugin;

/**
 * Common class for getting and creating images
 * 
 * @author sminto
 */
public final class AtlassianLogo {

	private static ImageRegistry imageRegistry;

	private static final URL BASE_URL = AtlassianBrandingPlugin.getDefault().getBundle().getEntry("/icons/");

	public static final ImageDescriptor ATLASSIAN_LOGO = create("misc", "Atlassian.png");

	private AtlassianLogo() {
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
