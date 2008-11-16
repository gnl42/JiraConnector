/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.ui.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.internal.preferences.Base64;

import com.atlassian.theplugin.eclipse.preferences.Activator;

/**
 * This storage allow us to handle comment, user name etc. histories in one way
 * 
 * @author Alexander Gurov
 */
public class UserInputHistory {

	protected static final String HISTORY_NAME_BASE = "history.";

	protected String name;
	protected int depth;
	protected List<String> history;

	public UserInputHistory(String name) {
		this(name, 5);
	}

	public UserInputHistory(String name, int depth) {
		this.name = name;
		this.depth = depth;

		this.loadHistoryLines();
	}

	public String getName() {
		return this.name;
	}

	public int getDepth() {
		return this.depth;
	}

	public String[] getHistory() {
		return (String[]) this.history.toArray(new String[this.history.size()]);
	}

	public void addLine(String line) {
		if (line == null || line.trim().length() == 0) {
			return;
		}
		this.history.remove(line);
		this.history.add(0, line);
		if (this.history.size() > this.depth) {
			this.history.remove(this.history.size() - 1);
		}
		this.saveHistoryLines();
	}

	public void clear() {
		this.history.clear();
		this.saveHistoryLines();
	}

	@SuppressWarnings("restriction")
	protected void loadHistoryLines() {
		this.history = new ArrayList<String>();
		String historyData = Activator.getDefault().getPreferenceStore()
				.getString(UserInputHistory.HISTORY_NAME_BASE + this.name);
		if (historyData != null && historyData.length() > 0) {
			String[] historyArray = historyData.split(";");
			for (int i = 0; i < historyArray.length; i++) {
				historyArray[i] = new String(Base64.decode(historyArray[i]
						.getBytes()));
			}
			this.history.addAll(Arrays.<String> asList(historyArray));
		}
	}

	@SuppressWarnings("restriction")
	protected void saveHistoryLines() {
		String result = "";
		for (Iterator<String> it = this.history.iterator(); it.hasNext();) {
			String str = (String) it.next();
			str = new String(Base64.encode(str.getBytes()));
			result += result.length() == 0 ? str : (";" + str);
		}
		Activator.getDefault().getPreferenceStore().setValue(
				UserInputHistory.HISTORY_NAME_BASE + this.name, result);
	}

}
