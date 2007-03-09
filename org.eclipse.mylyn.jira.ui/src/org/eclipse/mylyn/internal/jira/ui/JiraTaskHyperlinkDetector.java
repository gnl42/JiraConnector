/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui;

import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.mylar.tasks.ui.editors.AbstractTaskHyperlinkDetector;

/**
 * @author Eugene Kuleshov
 */
public class JiraTaskHyperlinkDetector extends AbstractTaskHyperlinkDetector {

	@Override
	protected IHyperlink[] findHyperlinks(TaskRepository repository, String text, int lineOffset, int regionOffset) {
		AbstractRepositoryConnector connector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				repository.getKind());

		int startPos = lineOffset;
		while (startPos > 0) {
			char c = text.charAt(startPos);
			if (Character.isWhitespace(c) || ",.;[](){}".indexOf(c) > -1)
				break;
			startPos--;
		}
		int endPos = lineOffset;
		while (endPos < text.length()) {
			char c = text.charAt(endPos);
			if (Character.isWhitespace(c) || ",.;[](){}".indexOf(c) > -1)
				break;
			endPos++;
		}

		String[] taskIds = connector.getTaskIdsFromComment(repository, text.substring(startPos, endPos));
		if (taskIds == null || taskIds.length == 0)
			return null;

		IHyperlink[] links = new IHyperlink[taskIds.length];
		for (int i = 0; i < taskIds.length; i++) {
			String taskId = taskIds[i];
			int startRegion = text.indexOf(taskId, startPos);
			links[i] = new JiraHyperLink(new Region(regionOffset + startRegion, taskId.length()), repository, taskId,
					connector.getTaskWebUrl(repository.getUrl(), taskId));
		}
		return links;
	}

	@Override
	protected String getTargetID() {
		return JiraUiPlugin.REPOSITORY_KIND;
	}

}
