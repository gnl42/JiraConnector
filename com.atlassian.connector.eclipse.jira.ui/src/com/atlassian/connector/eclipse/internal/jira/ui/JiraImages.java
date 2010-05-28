/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Mik Kersten
 */
public class JiraImages {

	private static ImageRegistry imageRegistry;

	private static final String T_VIEW = "eview16"; //$NON-NLS-1$

	private static final URL baseURL = JiraUiPlugin.getDefault().getBundle().getEntry("/icons/"); //$NON-NLS-1$

	public static final ImageDescriptor OVERLAY_JIRA = create(T_VIEW, "overlay-jira.gif"); //$NON-NLS-1$

	public static final ImageDescriptor OVERLAY_BUG = create(T_VIEW, "overlay-bug.gif"); //$NON-NLS-1$

	public static final ImageDescriptor OVERLAY_STORY = create(T_VIEW, "overlay-story.gif"); //$NON-NLS-1$

	public static final ImageDescriptor OVERLAY_FEATURE = create(T_VIEW, "overlay-feature.gif"); //$NON-NLS-1$

	public static final ImageDescriptor OVERLAY_IMPROVEMENT = create(T_VIEW, "overlay-improvement.gif"); //$NON-NLS-1$

	public static final ImageDescriptor OVERLAY_TASK = create(T_VIEW, "overlay-task.gif"); //$NON-NLS-1$

	public static final ImageDescriptor OVERLAY_SUB_TASK = create(T_VIEW, "overlay-sub-task.gif"); //$NON-NLS-1$

	public static final ImageDescriptor START_PROGRESS = create("etool16", "startprogress.png"); //$NON-NLS-1$ //$NON-NLS-2$

	public static final ImageDescriptor LIGHTBULB = create("obj16", "lightbulb.gif"); //$NON-NLS-1$ //$NON-NLS-2$

	public static ImageDescriptor create(String prefix, String name) {
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

		StringBuilder buffer = new StringBuilder(prefix);
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

		Image image = imageRegistry.get("" + imageDescriptor.hashCode()); //$NON-NLS-1$
		if (image == null) {
			image = imageDescriptor.createImage();
			imageRegistry.put("" + imageDescriptor.hashCode(), image); //$NON-NLS-1$
		}
		return image;
	}
}
