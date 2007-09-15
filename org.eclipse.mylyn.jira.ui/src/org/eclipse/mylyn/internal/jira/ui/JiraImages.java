/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/*
 * Created on Apr 20, 2004
 */
package org.eclipse.mylyn.internal.jira.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * @author Mik Kersten
 */
public class JiraImages {

	private static ImageRegistry imageRegistry;

	private static final String T_VIEW = "eview16";
	
	private static final String T_TOOL = "etool16";

	private static final URL baseURL = JiraUiPlugin.getDefault().getBundle().getEntry("/icons/");

	public static final ImageDescriptor OVERLAY_JIRA = create(T_VIEW, "overlay-jira.gif");

	public static final ImageDescriptor OVERLAY_BUG = create(T_VIEW, "overlay-bug.gif");

	public static final ImageDescriptor OVERLAY_FEATURE = create(T_VIEW, "overlay-feature.gif");

	public static final ImageDescriptor OVERLAY_IMPROVEMENT = create(T_VIEW, "overlay-improvement.gif");

	public static final ImageDescriptor OVERLAY_TASK = create(T_VIEW, "overlay-task.gif");

	public static final ImageDescriptor OVERLAY_SUB_TASK = create(T_VIEW, "overlay-sub-task.gif");
	
	public static final ImageDescriptor NEW_SUB_TASK = create(T_TOOL, "sub-task-new.gif");
	
	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static URL makeIconFileURL(String prefix, String name) throws MalformedURLException {
		if (baseURL == null)
			throw new MalformedURLException();

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
