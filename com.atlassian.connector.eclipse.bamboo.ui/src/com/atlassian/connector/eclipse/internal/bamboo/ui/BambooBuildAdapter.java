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

public class BambooBuildAdapter {

	private final BambooBuild build;

	private static int iconBuildingIndex;

	public BambooBuildAdapter(BambooBuild build) {
		this.build = build;
	}

	public BambooBuild getBuild() {
		return build;
	}

	public Image getImage() {

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

	public Image getBuildingImage() {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((build == null) ? 0 : build.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BambooBuildAdapter other = (BambooBuildAdapter) obj;
		if (build == null) {
			if (other.build != null) {
				return false;
			}
		} else if (!build.equals(other.build)) {
			return false;
		}
		return true;
	}

}
