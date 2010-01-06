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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonFonts;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;

import java.net.MalformedURLException;
import java.net.URL;

public abstract class AbstractCrucibleReviewItemLabelProvider extends ColumnLabelProvider implements
		IStyledLabelProvider {

	public static final StyledString.Styler BOLD_FONT_STYLER = new StyledString.Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = CommonFonts.BOLD;
		}
	};

	public static final StyledString.Styler BOLD_FONT_DECORATION_STYLER = new StyledString.Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = CommonFonts.BOLD;
			textStyle.foreground = JFaceResources.getColorRegistry().get(JFacePreferences.DECORATIONS_COLOR);
		}
	};

	protected Image getImage(final Comment comment) {
		final String avatarUrl = comment.getAuthor().getAvatarUrl();
		//			final Image image = CrucibleImages.getImage(CrucibleImages.DEFAULT_AVATAR);
		final Image image = CrucibleImages.getImage(new OffsettingCompositeImageDescriptor(
				CrucibleImages.DEFAULT_AVATAR, null));
		if (avatarUrl != null) {
			// this stuff will be actually used only when avatar URLs are served by Crucible to all authorized users
			// who see given review
			try {
				return CrucibleImages.getImage(ImageDescriptor.createFromURL(new URL(avatarUrl + "%3Fs%3D16&s=16")));
			} catch (MalformedURLException e) {
				return image;
			}
		} else {
			//				return CrucibleImages.getImage(new MyCompositeImageDescriptor(CrucibleImages.DEFAULT_AVATAR,
			//						comment.isDefectRaised() ? CommonImages.PRIORITY_1 : null));
			return image;
		}
	}

	public StyledString getStyledText(Object element) {
		return element == null ? new StyledString("") : new StyledString(element.toString());
	}
}