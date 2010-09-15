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

package com.atlassian.connector.eclipse.internal.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.jface.action.IAction;

/**
 * 
 * @author Jacek Jaroczynski
 */
public interface IReviewChangeListenerAction extends IAction {
	void updateReview(Review updatedReview, CrucibleFileInfo updatedFile);

	void updateReview(Review updatedReview, CrucibleFileInfo updatedFile, VersionedComment updatedComment);

}
