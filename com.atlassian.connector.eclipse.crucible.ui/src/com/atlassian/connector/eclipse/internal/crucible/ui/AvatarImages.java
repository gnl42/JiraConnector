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

package com.atlassian.connector.eclipse.internal.crucible.ui;

import com.atlassian.theplugin.commons.crucible.api.model.User;

import org.apache.commons.io.IOUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.services.IDisposable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public final class AvatarImages implements IDisposable {

	public enum AvatarSize {
		ORIGINAL, SMALL, LARGE
	}

	private static ImageRegistry imageRegistry;

	public void dispose() {
		imageRegistry.dispose();
		imageRegistry = null;
	}

	private static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
		}

		return imageRegistry;
	}

	public static Image getImage(String url, ImageDescriptor imageDescriptor) {
		ImageRegistry registry = getImageRegistry();

		Image image = registry.get(url);
		if (image == null) {
			image = imageDescriptor.createImage();
			registry.put(url, image);
		}
		return image;
	}

	public Image getAvatar(User author, AvatarSize size) {
		return getImageRegistry().get(author.getAvatarUrl() + size.toString());
	}

	public void addAvatar(User key, byte[] value) {
		InputStream is = new ByteArrayInputStream(value);
		try {
			Image image = getImage(key.getAvatarUrl() + AvatarSize.ORIGINAL.toString(),
					ImageDescriptor.createFromImageData(new ImageData(is)));
			getImageRegistry().put(key.getAvatarUrl() + AvatarSize.SMALL.toString(),
					new Image(Display.getDefault(), image.getImageData().scaledTo(16, 16)));
			getImageRegistry().put(key.getAvatarUrl() + AvatarSize.LARGE.toString(),
					new Image(Display.getDefault(), image.getImageData().scaledTo(32, 32)));
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
}
