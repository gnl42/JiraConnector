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

package com.atlassian.connector.eclipse.internal.crucible.ui.decoration;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.FileType;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.jetbrains.annotations.Nullable;

public class CrucibleFileInfoDecorator extends AbstractSimpleLightweightIconDecorator {

	@Nullable
	public ImageDescriptor getImageDescriptor(CrucibleFileInfo cfi) {
		if (cfi.getFileType() == FileType.File) {
			switch (cfi.getCommitType()) {
			case Added:
				return CrucibleImages.OVR_ADDED;
			case Deleted:
				return CrucibleImages.OVR_DELETED;
			case Modified:
			case Moved:
			case Copied:
				return CrucibleImages.OVR_MODIFIED;
			case Unknown:
			default:
				break;
			}
		}
		return null;

	}

	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof CrucibleFileInfo) {
			CrucibleFileInfo cfi = (CrucibleFileInfo) element;
			final ImageDescriptor imageDescriptor = getImageDescriptor(cfi);
			if (imageDescriptor != null) {
				decoration.addOverlay(imageDescriptor, IDecoration.BOTTOM_RIGHT);
			}

		}
	}

}
