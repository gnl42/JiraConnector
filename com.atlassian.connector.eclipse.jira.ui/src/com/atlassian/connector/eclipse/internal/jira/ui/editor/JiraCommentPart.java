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

package com.atlassian.connector.eclipse.internal.jira.ui.editor;

import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart;

/**
 * @author Jacek Jaroczynski
 */
@SuppressWarnings("restriction")
public class JiraCommentPart extends TaskEditorCommentPart {

//	@Override
//	protected void createCommentTitleExtention(final FormToolkit toolkit, Composite titleComposite, TaskComment comment) {
//		TaskAttribute visibleTo = comment.getTaskAttribute().getAttribute(IJiraConstants.COMMENT_SECURITY_LEVEL);
//
//		if (visibleTo != null && visibleTo.getValue() != null) {
//			Label l = toolkit.createLabel(titleComposite, Messages.JiraCommetVisible);
//			l.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
//			l.setBackground(null);
//			l.setToolTipText(Messages.JiraCommetVisibleTooltip);
//
//			Label ll = toolkit.createLabel(titleComposite, visibleTo.getValue());
//			ll.setForeground(toolkit.getColors().createColor("com.atlassian.connector.jira.red", 150, 20, 20)); //$NON-NLS-1$
//			ll.setToolTipText(Messages.JiraCommetVisibleTooltip);
//		}
//	}
}
