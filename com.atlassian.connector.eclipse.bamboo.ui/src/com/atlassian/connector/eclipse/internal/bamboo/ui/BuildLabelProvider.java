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

import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class BuildLabelProvider implements ILabelProvider, IFontProvider {

	public Image getImage(Object element) {

		if (element instanceof EclipseBambooBuild) {
			return BambooImageUtil.getImage(((EclipseBambooBuild) element).getBuild());
		}
		return CommonImages.getImage(BambooImages.STATUS_DISABLED);
	}

	public String getText(Object element) {
		StringBuilder builder = new StringBuilder();
		EclipseBambooBuild bambooBuild = (EclipseBambooBuild) element;
		if (bambooBuild.getBuild().getPlanName() == null) {
			builder.append("N/A");
		} else {
			builder.append(bambooBuild.getBuild().getPlanName());
		}
		if (bambooBuild.getBuild().getPlanKey() != null) {
			builder.append("  [");
			builder.append(bambooBuild.getBuild().getPlanKey());
			try {
				String number = String.valueOf(bambooBuild.getBuild().getNumber());
				builder.append("-");
				builder.append(number);
			} catch (UnsupportedOperationException e) {
				if (bambooBuild.getBuild().getPlanName() != null) {
					builder.append("-");
					builder.append("N/A");
				}
			}
			builder.append("]");
		}
		return builder.toString();
	}

	public Font getFont(Object element) {
		return BambooUiUtil.getFontForBuildStatus(element);
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}
}