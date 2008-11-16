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
public class TaskListImageDescriptor extends CompositeImageDescriptor {

	private ImageData base;

	private ImageData overlay;

	private boolean top;

	private boolean left;

	protected Point size;

	public TaskListImageDescriptor(ImageDescriptor baseDesc,
			ImageDescriptor overlayDesc, boolean top, boolean left) {
		this.base = getImageData(baseDesc);
		this.top = top;
		this.left = left;
		if (overlayDesc != null) {
			this.overlay = getImageData(overlayDesc);
		}
		Point size = new Point(base.width, base.height);
		setImageSize(size);
	}

	public TaskListImageDescriptor(ImageDescriptor baseDesc, Point size) {
		this.base = getImageData(baseDesc);
		setImageSize(size);
	}

	@Override
	protected void drawCompositeImage(int width, int height) {
		drawImage(base, 0, 0);
		int x = 0;
		int y = 0;
		if (!left) {
			x = 8; // base.width - overlay.width;
		}
		if (!top) {
			y = 8; // base.height - overlay.height;
		}
		if (overlay != null) {
			drawImage(overlay, x, y);
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

	public void setImageSize(Point size) {
		this.size = size;
	}

	@Override
	protected Point getSize() {
		return new Point(size.x, size.y);
	}
}