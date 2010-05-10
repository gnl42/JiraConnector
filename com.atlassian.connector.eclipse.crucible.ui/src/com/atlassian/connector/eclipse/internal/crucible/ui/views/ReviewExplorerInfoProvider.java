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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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

							final String infoText = getCompactedLineInfoText(ranges);
							tooltip.addIconAndLabel(composite, null, infoText, false);
						}

						private boolean isSimpleInfoEnough(Map<String, IntRanges> ranges) {
							if (ranges.size() <= 1) {
								return true;
							}
							final Iterator<Entry<String, IntRanges>> it = ranges.entrySet().iterator();
							final IntRanges lines = it.next().getValue();
							while (it.hasNext()) {
								if (!lines.equals(it.next().getValue())) {
									return false;
								}
							}
							return true;
						}

						private String getCompactedLineInfoText(Map<String, IntRanges> ranges) {

							if (isSimpleInfoEnough(ranges)) {
								final StringBuilder infoText = new StringBuilder("File comment for ");
								final Iterator<Entry<String, IntRanges>> it = ranges.entrySet().iterator();
								Entry<String, IntRanges> curEntry = it.next();
								IntRanges lines = curEntry.getValue();
								infoText.append(getLineInfo(lines));
								if (it.hasNext()) {
									infoText.append(" in revisions: ");
								} else {
									infoText.append(" in revision: ");
								}

								do {
									infoText.append(curEntry.getKey());
									if (it.hasNext()) {
										infoText.append(", ");
									} else {
										break;
									}
									curEntry = it.next();
								} while (true);
								return infoText.toString();
							} else {
								final StringBuilder infoText = new StringBuilder("File comment for:\n");
								for (Map.Entry<String, IntRanges> range : ranges.entrySet()) {
									infoText.append("- ");
									infoText.append(getLineInfo(range.getValue()));
									infoText.append(" in revision: ");
									infoText.append(range.getKey());
									infoText.append("\n");
								}
								return infoText.toString();
							}
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

	private static String getLineInfo(IntRanges intRanges) {
		if (intRanges.getTotalMin() == intRanges.getTotalMax()) {
			return "line " + intRanges.getTotalMin();
		} else {
			return "lines " + intRanges.toNiceString();
		}
	}

}
