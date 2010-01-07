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

import java.util.Arrays;
import java.util.List;

public class ReviewTreeNode {
	@NotNull
	private List<ReviewTreeNode> children = MiscUtil.buildArrayList();

	@Nullable
	private CrucibleFileInfo cfi;

	@Nullable
	private String pathToken;

	public ReviewTreeNode(@Nullable String pathToken) {
		this.pathToken = pathToken;
	}

	@Nullable
	public CrucibleFileInfo getCrucibleFileInfo() {
		return cfi;
	}

	@NotNull
	public List<ReviewTreeNode> getChildren() {
		return children;
	}

	void add(String[] path, CrucibleFileInfo aCfi) {
		if (path.length == 0) {
			cfi = aCfi;
			return;
		}
		for (ReviewTreeNode child : children) {
			if (child.pathToken.equals(path[0])) {
				child.add(Arrays.copyOfRange(path, 1, path.length), aCfi);
				return;
			}
		}
		ReviewTreeNode newChild = new ReviewTreeNode(path[0]);
		children.add(newChild);
		newChild.add(Arrays.copyOfRange(path, 1, path.length), aCfi);
		return;
	}

	public void compact() {
		int size = children.size();
		if (size == 1) {
			ReviewTreeNode onlyChild = children.get(0);
			if (onlyChild.getCrucibleFileInfo() == null) {
				if (pathToken == null) {
					pathToken = onlyChild.pathToken;
				} else {
					pathToken = pathToken + "/" + onlyChild.pathToken;
				}
				children = onlyChild.children;
			}
		}
		for (ReviewTreeNode child : children) {
			child.compact();
		}
	}

	@Override
	public String toString() {
		return pathToken;
	}
}