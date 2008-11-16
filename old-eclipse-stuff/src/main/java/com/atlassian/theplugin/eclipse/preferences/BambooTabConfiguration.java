/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.eclipse.preferences;

import java.util.ArrayList;
import java.util.List;

public class BambooTabConfiguration {

	int[] columnsOrder;
	List<Integer> columnsWidth = new ArrayList<Integer>();

	public int[] getColumnsOrder() {
		return columnsOrder;
	}

	public void setColumnsOrder(int[] order) {
		columnsOrder = order;
	}

	public String getColumnsOrderString() {
		StringBuilder ret = new StringBuilder();

		for (Integer column : columnsOrder) {
			ret.append(column + " ");
		}

		return ret.toString();
	}

	public void setColumnsOrderString(String columnsOrder) {

		String[] order = columnsOrder.split(" ");

		List<Integer> columnsOrderList = new ArrayList<Integer>(order.length);

		for (String column : order) {
			if (column != null && column.length() > 0) {
				try {
					columnsOrderList.add(Integer.valueOf(column));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}

		// copy ArrayList to the simple array (size can be smaller than
		// order.lenght)
		this.columnsOrder = new int[columnsOrderList.size()];

		for (int i = 0; i < columnsOrderList.size(); ++i) {
			this.columnsOrder[i] = columnsOrderList.get(i);
		}

	}

	public List<Integer> getColumnsWidth() {
		return columnsWidth;
	}

	public void setColumnsWidth(List<Integer> columnsWidth) {
		this.columnsWidth = columnsWidth;
	}

	public String getColumnsWidthString() {
		StringBuilder ret = new StringBuilder();

		for (Integer column : columnsWidth) {
			ret.append(column + " ");
		}

		return ret.toString();
	}

	public void setColumnsWidthString(String columnsWidth) {
		this.columnsWidth.clear();

		String[] order = columnsWidth.split(" ");
		for (String column : order) {
			if (column != null && column.length() > 0) {

				try {
					this.columnsWidth.add(Integer.valueOf(column));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
