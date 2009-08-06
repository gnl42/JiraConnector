/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.monitor.usage;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.mylyn.internal.monitor.core.collection.InteractionEventSummary;

/**
 * Sorts SingleSummary objects based on type, id, name, or usage count.
 */
public class InteractionEventSummarySorter extends ViewerSorter {

	/**
	 * Constructor argument values that indicate to sort items by different columns.
	 */
	public final static int TYPE = 1;

	public final static int ID = 2;

	public final static int NAME = 3;

	public final static int USAGE_COUNT = 4;

	// Criteria that the instance uses
	private final int criteria;

	/**
	 * Creates a resource sorter that will use the given sort criteria.
	 * 
	 * @param criteria
	 *            the sort criterion to use: one of <code>LABEL_VIEW_REPOSITORIES</code> or <code>TYPE</code>
	 */
	public InteractionEventSummarySorter(int criteria) {
		super();
		this.criteria = criteria;
	}

	@Override
	public int compare(Viewer viewer, Object obj1, Object obj2) {
		InteractionEventSummary summary1 = (InteractionEventSummary) obj1;
		InteractionEventSummary summary2 = (InteractionEventSummary) obj2;

		switch (criteria) {
		case TYPE:
			return compareTypes(summary1, summary2);
		case NAME:
			return compareNames(summary1, summary2);
		case USAGE_COUNT:
			return compareUsageCount(summary2, summary1);
		default:
			return 0;
		}
	}

	/**
	 * Returns a number reflecting the collation order of the given summaries based on their usage count.
	 * 
	 * @param summary1
	 * @param summary2
	 * @return a negative number if the first element is less than the second element; the value <code>0</code> if the
	 *         first element is equal to the second element; and a positive number if the first element is greater than
	 *         the second element
	 */
	private int compareUsageCount(InteractionEventSummary summary1, InteractionEventSummary summary2) {
		int result = summary1.getUsageCount() - summary2.getUsageCount();
		result = result < 0 ? -1 : (result > 0) ? 1 : 0;
		return result;
	}

	/**
	 * Returns a number reflecting the collation order of the given summaries based on their names.
	 * 
	 * @param summary1
	 *            the first task element to be ordered
	 * @param summary2
	 *            the second task element to be ordered
	 * @return a negative number if the first element is less than the second element; the value <code>0</code> if the
	 *         first element is equal to the second element; and a positive number if the first element is greater than
	 *         the second element
	 */
	@SuppressWarnings("unchecked")
	protected int compareNames(InteractionEventSummary summary1, InteractionEventSummary summary2) {
		return getComparator().compare(summary1.getName(), summary2.getName());
	}

	/**
	 * Returns a number reflecting the collation order of the given summaries based on their types.
	 * 
	 * @param summary1
	 *            the first task element to be ordered
	 * @param summary2
	 *            the second task element to be ordered
	 * @return a negative number if the first element is less than the second element; the value <code>0</code> if the
	 *         first element is equal to the second element; and a positive number if the first element is greater than
	 *         the second element
	 */
	@SuppressWarnings("unchecked")
	protected int compareTypes(InteractionEventSummary summary1, InteractionEventSummary summary2) {
		return getComparator().compare(summary1.getType(), summary2.getType());
	}

	/**
	 * Returns the sort criteria of this this sorter.
	 * 
	 * @return the sort criterion
	 */
	public int getCriteria() {
		return criteria;
	}
}
