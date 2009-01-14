/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.ui.editor;

import com.atlassian.connector.eclipse.ui.editor.AbstractFormPagePart;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

/**
 * A form part that needs to be aware of the review that it is displaying
 * 
 * @author Shawn Minto
 */
public abstract class AbstractCrucibleEditorFormPart extends AbstractFormPagePart {

	public abstract void initialize(CrucibleReviewEditorPage editor, Review review);

}
