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

package com.atlassian.connector.eclipse.internal.crucible.ui.annotations;

import java.util.List;

/**
 * Data model to represent the annotations that we need to display in the hover.
 * 
 * @author Shawn Minto
 */
public class CrucibleAnnotationHoverInput {

	private final List<CrucibleCommentAnnotation> annotations;

	public CrucibleAnnotationHoverInput(List<CrucibleCommentAnnotation> annotations) {
		this.annotations = annotations;
	}

	public boolean containsInput() {
		return annotations != null && annotations.size() > 0;
	}
}
