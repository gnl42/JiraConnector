/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Leah Findlater - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.monitor.core.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * @author Leah Findlater
 */
public class InteractionByTypeSummary {
	HashMap<String, HashMap<Integer, Integer>> usageMap;

	public InteractionByTypeSummary() {
		usageMap = new HashMap<String, HashMap<Integer, Integer>>();
	}

	public void setUserCount(int userId, String originId, int count) {
		if (!usageMap.containsKey(originId)) {
			usageMap.put(originId, new HashMap<Integer, Integer>());
		}
		usageMap.get(originId).put(userId, count);
	}

	public int getUserCount(int userId, String originId) {
		if (usageMap.containsKey(originId) && usageMap.get(originId).containsKey(userId)) {
			return usageMap.get(originId).get(userId);
		} else {
			return 0;
		}
	}

	public int getTotalCount(String originId) {
		int count = 0;
		for (Integer userId : usageMap.get(originId).keySet()) {
			count = count + usageMap.get(originId).get(userId);
		}

		return count;
	}

	public void printOut(Set<Integer> allUserIdsList) {
		System.out.print("EventId"); //$NON-NLS-1$
		// Collections.sort(allUserIdsList);
		for (Integer userId : allUserIdsList) {
			System.out.print("\t" + userId); //$NON-NLS-1$
		}
		System.out.println();

		for (String originId : usageMap.keySet()) {
			System.out.print(originId);
			Set<Integer> userIdSet = usageMap.get(originId).keySet();
			for (int userId : allUserIdsList) {
				if (userIdSet.contains(userId)) {
					System.out.print("\t1"); //$NON-NLS-1$
				} else {
					System.out.print("\t0"); //$NON-NLS-1$
				}
			}
			System.out.println();

			/*
			 * for (int userId : usageMap.get(originId).keySet()) {
			 * System.out.print("\t" + userId); } System.out.println();
			 */
		}
	}

	public ArrayList<String> getOriginIdList() {
		ArrayList<String> originIdList = new ArrayList<String>(usageMap.keySet());

		return originIdList;
	}

}
