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

package com.atlassian.connector.eclipse.internal.crucible.ui.views;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

public class OffsettingCompositeImageDescriptor extends CompositeImageDescriptor {

	private final ImageData base;

	private final ImageData kind;

	protected Point size;

	public static final int OFFSET_DECORATION = 5;

	private static final int WIDTH_ICON = 16;

	private int offset = 0;

	private final ImageDescriptor icon;

	private final ImageDescriptor optionalMarker;

	public OffsettingCompositeImageDescriptor(ImageDescriptor icon, ImageDescriptor optionalMarker) {
		this.icon = icon;
		this.optionalMarker = optionalMarker;
		this.base = getImageData(icon);
		if (optionalMarker != null) {
			kind = getImageData(optionalMarker);
		} else {
			kind = null;
		}
		int width = WIDTH_ICON + OFFSET_DECORATION;
		offset = OFFSET_DECORATION;
		this.size = new Point(width, base.height);
	}

	@Override
	protected void drawCompositeImage(int width, int height) {
		drawImage(base, offset, 1);
		if (kind != null) {
			drawImage(kind, 0, 1);
		}
	}

	@Override
	public int hashCode() {
		return icon.hashCode() + 11 * (optionalMarker != null ? optionalMarker.hashCode() : 0);
	}

	private ImageData getImageData(ImageDescriptor descriptor) {
		ImageData data = descriptor.getImageData();
		// see bug 51965: getImageData can return null
		if (data == null) {
			data = DEFAULT_IMAGE_DATA;
		}
		return data;
	}

	@Override
	protected Point getSize() {
		return new Point(size.x, size.y);
	}
}