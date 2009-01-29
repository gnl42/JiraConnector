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

package com.atlassian.connector.eclipse.internal.crucible.ui;

import org.eclipse.jface.action.IAction;

/**
 * An action that is used in a part for a review. This is used to notify other parts when the action has run (e.g. close
 * the annotation popup).
 * 
 * @author Shawn Minto
 */
public interface IReviewAction extends IAction {

	void setActionListener(IReviewActionListener listner);

}
