/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.eclipse.view.popup;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * @author Mik Kersten
 */
public class CompositeContainerImageDescriptor extends CompositeImageDescriptor {

	private ImageData base;

	private ImageData overlay;

	protected Point size;

	public CompositeContainerImageDescriptor(ImageDescriptor icon, ImageDescriptor overlay, boolean wide) {
		this.base = getImageData(icon);
		if (overlay != null) {
			this.overlay = getImageData(overlay);
		}
		int width = CompositeTaskImageDescriptor.WIDTH_ICON;
		if (wide) {
			width += CompositeTaskImageDescriptor.OFFSET_DECORATION;
		}
		this.size = new Point(width, base.height);
	}

	@Override
	protected void drawCompositeImage(int width, int height) {
		drawImage(base, 0, 0);
		if (overlay != null) {
			drawImage(overlay, base.width + 2, 0);
		}
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