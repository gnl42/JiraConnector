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

package com.atlassian.connector.eclipse.internal.crucible.ui.decoration;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;

public class CommentDecorator extends AbstractSimpleLightweightIconDecorator {

	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof Comment) {
			Comment comment = (Comment) element;
			if (comment.isDefectRaised()) {
				decoration.addOverlay(CommonImages.PRIORITY_1, IDecoration.BOTTOM_LEFT);
			}
		}
	}

}
