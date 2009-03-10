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
import com.atlassian.theplugin.commons.bamboo.BuildStatus;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

public class BambooBuildViewerComparator extends ViewerComparator {

	private SortOrder sortOrder = SortOrder.UNSORTED;

	public enum SortOrder {
		UNSORTED(SWT.NONE), STATE_PASSED_FAILED(SWT.UP), STATE_FAILED_PASSED(SWT.DOWN);
		private final int direction;

		private SortOrder(int direction) {
			this.direction = direction;
		}

		public int getDirection() {
			return direction;
		}

		public static SortOrder next(SortOrder current) {
			switch (current) {
			case UNSORTED:
				return STATE_PASSED_FAILED;
			case STATE_PASSED_FAILED:
				return STATE_FAILED_PASSED;
			default:
				return UNSORTED;
			}
		}
	}

	public SortOrder toggleSortOrder() {
		sortOrder = SortOrder.next(sortOrder);
		return sortOrder;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}

	@Override
	public boolean isSorterProperty(Object element, String property) {
		// ignore
		return true;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof BambooBuild && e2 instanceof BambooBuild) {
			BuildStatus state1 = ((BambooBuild) e1).getStatus();
			BuildStatus state2 = ((BambooBuild) e2).getStatus();
			switch (sortOrder) {
			case UNSORTED:
				return sortByName((BambooBuild) e1, (BambooBuild) e2);
			case STATE_PASSED_FAILED:
				if (state1 == state2) {
					return sortByName((BambooBuild) e1, (BambooBuild) e2);
				}
				if (state1 == BuildStatus.SUCCESS) {
					return -1;
				}
				if (state2 == BuildStatus.SUCCESS) {
					return 1;
				}
				if (state1 == BuildStatus.FAILURE) {
					return -1;
				}
				if (state2 == BuildStatus.FAILURE) {
					return 1;
				}
				return sortByName((BambooBuild) e1, (BambooBuild) e2);
			case STATE_FAILED_PASSED:
				if (state1 == state2) {
					return sortByName((BambooBuild) e1, (BambooBuild) e2);
				}
				if (state1 == BuildStatus.FAILURE) {
					return -1;
				}
				if (state2 == BuildStatus.FAILURE) {
					return 1;
				}
				if (state1 == BuildStatus.SUCCESS) {
					return -1;
				}
				if (state2 == BuildStatus.SUCCESS) {
					return 1;
				}
				return sortByName((BambooBuild) e1, (BambooBuild) e2);
			}
		}
		return 0;
	}

	private int sortByName(BambooBuild bb1, BambooBuild bb2) {
		if (bb1 == null || bb2 == null || bb1.getPlanName() == null || bb2.getPlanName() == null) {
			return 0;
		}
		return bb1.getPlanName().compareTo(bb2.getPlanName());
	}
}