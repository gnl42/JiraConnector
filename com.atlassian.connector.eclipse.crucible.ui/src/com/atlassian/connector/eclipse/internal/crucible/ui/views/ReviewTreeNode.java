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

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class ReviewTreeNode {
	@NotNull
	private final List<Object> children = MiscUtil.buildArrayList();

	@Nullable
	private String pathToken;

	private int category;

	public ReviewTreeNode(@Nullable String pathToken, @Nullable Collection<? extends Object> children) {
		this.pathToken = pathToken;
		if (children != null) {
			this.children.addAll(children);
		}
	}

	@NotNull
	public List<?> getChildren() {
		return children;
	}

	private void compact(ReviewTreeNode node) {
		List<? extends Object> children = node.getChildren();
		if (children.size() == 1 && children.get(0) instanceof ReviewTreeNode) {
			node.pathToken = node.pathToken + '/' + ((ReviewTreeNode) children.get(0)).getPathToken();
			List<? extends Object> newChildren = ((ReviewTreeNode) children.get(0)).getChildren();
			node.children.clear();
			node.children.addAll(newChildren);
			compact(node);
		}

		for (Object child : children) {
			if (child instanceof ReviewTreeNode) {
				compact((ReviewTreeNode) child);
			}
		}
	}

	public void compact() {
		for (Object child : children) {
			if (child instanceof ReviewTreeNode) {
				compact((ReviewTreeNode) child);
			}
		}
	}

	private static ReviewTreeNode convert(List<String> paths, int offset, Object child) {
		if (offset + 2 == paths.size()) {
			return new ReviewTreeNode(paths.get(offset), MiscUtil.buildArrayList(child));
		}
		return new ReviewTreeNode(paths.get(offset), MiscUtil.buildArrayList(convert(paths, offset + 1, child)));
	}

	@NotNull
	public static ReviewTreeNode convert(@NotNull CrucibleFileInfo fileInfo) {
		final String path = fileInfo.getFileDescriptor().getUrl();
		final List<String> pathTokens = MiscUtil.buildArrayList(path.split("/|\\\\"));

		for (int i = 0, s = pathTokens.size(); i < s;) {
			if ("".equals(pathTokens.get(i))) {
				pathTokens.remove(i);
				s = pathTokens.size();
			} else {
				++i;
			}
		}

		if (pathTokens.size() <= 1) {
			return new ReviewTreeNode("/", MiscUtil.buildArrayList(fileInfo));
		} else {
			return convert(pathTokens, 0, fileInfo);
		}
	}

	public void addChild(Object nodeToAdd) {
		if (nodeToAdd instanceof ReviewTreeNode) {
			for (Object child : children) {
				if (child instanceof ReviewTreeNode) {
					if (((ReviewTreeNode) child).getPathToken().equals(((ReviewTreeNode) nodeToAdd).getPathToken())) {
						for (Object childToAdd : ((ReviewTreeNode) nodeToAdd).getChildren()) {
							((ReviewTreeNode) child).addChild(childToAdd);
						}
						return;
					}
				}
			}
		}

		children.add(nodeToAdd);
	}

	public String getPathToken() {
		return pathToken;
	}

	@Override
	public String toString() {
		return pathToken != null ? pathToken : "";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pathToken == null) ? 0 : pathToken.hashCode());
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
		ReviewTreeNode other = (ReviewTreeNode) obj;
		if (pathToken == null) {
			if (other.pathToken != null) {
				return false;
			}
		} else if (!pathToken.equals(other.pathToken)) {
			return false;
		}
		return true;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public int getCategory() {
		return category;
	}
}