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

package com.atlassian.connector.eclipse.internal.monitor.usage;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * @author Mik Kersten
 */
public class UsageMonitorImages {

	private static ImageRegistry imageRegistry;

	private static final String T_ELCL = "elcl16";

	private static final String T_EVIEW = "eview16";

	private static final String T_MISC = "misc";

	private static final URL baseURL = UiUsageMonitorPlugin.getDefault().getBundle().getEntry("/icons/");

	public static final ImageDescriptor REFRESH = create(T_ELCL, "refresh.gif");

	public static final ImageDescriptor SYNCHED = create(T_ELCL, "synched.gif");

	public static final ImageDescriptor REMOVE = create(T_ELCL, "remove.gif");

	public static final ImageDescriptor MONITOR = create(T_EVIEW, "monitor.gif");

	public static final ImageDescriptor ZIP_FILE = create(T_ELCL, "import-zip.gif");

	public static final ImageDescriptor LOGO = create(T_MISC, "atlassian_icon-32.png");

	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static URL makeIconFileURL(String prefix, String name) throws MalformedURLException {
		if (baseURL == null) {
			throw new MalformedURLException();
		}

		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append('/');
		buffer.append(name);
		return new URL(baseURL, buffer.toString());
	}

	private static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
		}

		return imageRegistry;
	}

	/**
	 * Lazily initializes image map.
	 */
	public static Image getImage(ImageDescriptor imageDescriptor) {
		ImageRegistry imageRegistry = getImageRegistry();

		Image image = imageRegistry.get("" + imageDescriptor.hashCode());
		if (image == null) {
			image = imageDescriptor.createImage();
			imageRegistry.put("" + imageDescriptor.hashCode(), image);
		}
		return image;
	}
}
