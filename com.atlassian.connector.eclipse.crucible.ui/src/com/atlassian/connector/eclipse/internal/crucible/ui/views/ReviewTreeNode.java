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
import java.util.List;

public class ReviewTreeNode {
	private final int category;

	@NotNull
	private List<Object> children = MiscUtil.buildArrayList();

	@Nullable
	private CrucibleFileInfo cfi;

	@Nullable
	private String pathToken;

	@Nullable
	private final ReviewTreeNode parent;

	public ReviewTreeNode(@Nullable ReviewTreeNode parent, @Nullable String pathToken) {
		this(parent, pathToken, 0);
	}

	public ReviewTreeNode(@Nullable ReviewTreeNode parent, @Nullable String pathToken, int category) {
		this.parent = parent;
		this.pathToken = pathToken;
		this.category = category;
	}

	@Nullable
	public CrucibleFileInfo getCrucibleFileInfo() {
		return cfi;
	}

	@NotNull
	public List<Object> getChildren() {
		return children;
	}

	void add(String[] path, CrucibleFileInfo aCfi) {
		if (path.length == 0) {
			cfi = aCfi;
			return;
		}
		for (Object child : children) {
			if (!(child instanceof ReviewTreeNode)) {
				continue;
			}
			if (((ReviewTreeNode) child).pathToken.equals(path[0])) {
				// there is no Arrays.copyOfRange in Java 1.5, so we are using this approach
				final String[] allButFirst = new String[path.length - 1];
				System.arraycopy(path, 1, allButFirst, 0, allButFirst.length);
				((ReviewTreeNode) child).add(allButFirst, aCfi);
				return;
			}
		}
		ReviewTreeNode newChild = new ReviewTreeNode(this, path[0]);
		children.add(newChild);

		String[] allButFirst = new String[path.length - 1];
		System.arraycopy(path, 1, allButFirst, 0, allButFirst.length);
		newChild.add(allButFirst, aCfi);
	}

	public void compact() {
		while (children.size() == 1) {
			Object onlyChild = children.get(0);
			final ReviewTreeNode childTreeNode = (ReviewTreeNode) onlyChild;
			if (onlyChild instanceof ReviewTreeNode && childTreeNode.getCrucibleFileInfo() == null) {
				if (pathToken == null) {
					pathToken = childTreeNode.pathToken;
				} else {
					pathToken = pathToken + "/" + childTreeNode.pathToken;
				}
				children = childTreeNode.children;
			} else {
				break;
			}
		}
		for (Object child : children) {
			if (child instanceof ReviewTreeNode) {
				((ReviewTreeNode) child).compact();
			}
		}
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
		result = prime * result + ((cfi == null) ? 0 : cfi.hashCode());
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
		if (!MiscUtil.isEqual(parent, other.parent)) {
			return false;
		}
		if (cfi == null) {
			if (other.cfi != null) {
				return false;
			}
		} else if (!cfi.equals(other.cfi)) {
			return false;
		}
		if (pathToken == null) {
			if (other.pathToken != null) {
				return false;
			}
		} else if (!pathToken.equals(other.pathToken)) {
			return false;
		}
		return true;
	}

	public int getCategory() {
		return category;
	}
}