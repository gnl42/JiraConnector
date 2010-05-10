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

import com.atlassian.connector.commons.misc.IntRanges;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.AvatarImages.AvatarSize;
import com.atlassian.connector.eclipse.internal.crucible.ui.util.CommentUiUtil;
import com.atlassian.connector.eclipse.ui.commons.CustomToolTip;
import com.atlassian.connector.eclipse.ui.viewers.ICustomToolTipInfo;
import com.atlassian.connector.eclipse.ui.viewers.ICustomToolTipInfoProvider;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.RepositoryType;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import java.util.Map;

public class ReviewExplorerInfoProvider implements ICustomToolTipInfoProvider {

	public ICustomToolTipInfo getToolTipInfo(Object hoverObject) {
		if (hoverObject instanceof Widget) {
			final Object data = ((Widget) hoverObject).getData();
			if (data != null) {
				if (data instanceof ReviewTreeNode) {
					final CrucibleFileInfo fileInfo = ((ReviewTreeNode) data).getCrucibleFileInfo();
					if (fileInfo != null) {
						return new ICustomToolTipInfo() {
							public boolean isContainer() {
								return false;
							}

							public void createToolTipArea(CustomToolTip tooltip, Composite composite) {

								VersionedVirtualFile newFile = fileInfo.getFileDescriptor();
								VersionedVirtualFile oldFile = fileInfo.getOldFileDescriptor();

								String details = newFile.getRevision();
								if (oldFile != null && oldFile.getRevision().length() != 0) {
									details += '-' + oldFile.getRevision();
								}

								if (fileInfo.getRepositoryType() == RepositoryType.SCM) {
									tooltip.addIconAndLabel(composite, null, "Post-commit review item for revisions:",
											true);
									tooltip.addIconAndLabel(composite, null, details);
								} else if (fileInfo.getRepositoryType() == RepositoryType.UPLOAD) {
									tooltip.addIconAndLabel(composite, null, "Pre-commit review item (" + details + ")");
								}
							}
						};
					}
				} else if (data instanceof VersionedComment) {
					assert Display.getCurrent() != null;

					final VersionedComment comment = (VersionedComment) data;

					final Image avatarImage = CrucibleUiPlugin.getDefault().getAvatarsCache().getAvatar2(comment.getAuthor(),
							AvatarSize.LARGE);

					return new ICustomToolTipInfo() {
						public boolean isContainer() {
							return false;
						}

						public void createToolTipArea(CustomToolTip tooltip, Composite composite) {
							Map<String, IntRanges> ranges = comment.getLineRanges();
							tooltip.addIconAndLabel(composite, avatarImage, CommentUiUtil.getCommentInfoHeaderText(comment),
									true);
							if (ranges == null || ranges.keySet() == null) {
								tooltip.addIconAndLabel(composite, null, "General File Comment", false);
								return;
							}

							final String infoText = CommentUiUtil.getCompactedLineInfoText(ranges);
							tooltip.addIconAndLabel(composite, null, infoText, false);
						}

					};
				} else if (data instanceof Comment) {
					final Comment comment = (Comment) data;
					final Image avatarImage = CrucibleUiPlugin.getDefault().getAvatarsCache().getAvatar2(comment.getAuthor(),
							AvatarSize.LARGE);
					return new ICustomToolTipInfo() {
						public boolean isContainer() {
							return false;
						}

						public void createToolTipArea(CustomToolTip tooltip, Composite composite) {
							tooltip.addIconAndLabel(composite, avatarImage, CommentUiUtil.getCommentInfoHeaderText(comment),
									true);

							tooltip.addIconAndLabel(composite, null,
									comment.getParentComment() != null ? "Comment Reply" : "General Comment",
									false);
						}
					};

				}
			}
		}
		return null;
	}

}
