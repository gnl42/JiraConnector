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
public class CompositeSynchImageDescriptor extends CompositeImageDescriptor {

	private ImageData base;

	private ImageData background;

	private boolean fillBackground;

	protected Point size;

	static int WIDTH;

	public CompositeSynchImageDescriptor(ImageDescriptor icon, boolean fillBackground) {
		this.base = getImageData(icon);
		//this.background = getImageData(TasksUiImages.OVERLAY_SOLID_WHITE);
		this.size = new Point(background.width, background.height);
		this.fillBackground = fillBackground;
	}

	@Override
	protected void drawCompositeImage(int width, int height) {
		if (fillBackground) {
			drawImage(background, 0, 0);
		}
		drawImage(base, 3, 2);
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