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

package com.atlassian.connector.eclipse.internal.bamboo.ui;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.swt.graphics.Image;

public class BambooImageUtil {
	private static int iconBuildingIndex;

	public static Image getImage(BambooBuild build) {

		if (build.getEnabled()) {
			if (build.getErrorMessage() != null) {
				return CommonImages.getImage(BambooImages.STATUS_DISABLED);
			}
			switch (build.getStatus()) {
			case FAILURE:
				return CommonImages.getImage(BambooImages.STATUS_FAILED);
			case SUCCESS:
				return CommonImages.getImage(BambooImages.STATUS_PASSED);
			case BUILDING:
			case IN_QUEUE:
				switch (build.getLastStatus()) {
				case FAILURE:
					return CommonImages.getImage(BambooImages.STATUS_FAILED);
				case SUCCESS:
					return CommonImages.getImage(BambooImages.STATUS_PASSED);
				default:
					return CommonImages.getImage(BambooImages.STATUS_DISABLED);
				}
			case UNKNOWN:
			default:
				return CommonImages.getImage(BambooImages.STATUS_DISABLED);
			}
		}

		return CommonImages.getImage(BambooImages.STATUS_DISABLED);
	}

	public static Image getBuildingImage(BambooBuild build) {
		switch (build.getStatus()) {
		case BUILDING:
			++iconBuildingIndex;
			iconBuildingIndex %= BambooImages.STATUS_BUILDING.length;
			return CommonImages.getImage(BambooImages.STATUS_BUILDING[iconBuildingIndex]);
		case IN_QUEUE:
			return CommonImages.getImage(BambooImages.STATUS_WAITING);
		case FAILURE:
		case SUCCESS:
		case UNKNOWN:
		default:
			return null;
		}
	}

}
