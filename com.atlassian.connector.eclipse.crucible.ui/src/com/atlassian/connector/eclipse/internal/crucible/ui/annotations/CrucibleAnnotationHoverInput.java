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

import com.atlassian.connector.eclipse.internal.crucible.core.VersionedCommentDateComparator;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import java.util.Collections;
import java.util.Comparator;
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
		Collections.sort(annotations, new Comparator<CrucibleCommentAnnotation>() {

			private final VersionedCommentDateComparator comparator = new VersionedCommentDateComparator();

			public int compare(CrucibleCommentAnnotation o1, CrucibleCommentAnnotation o2) {
				if (o1 != null && o2 != null) {
					VersionedComment c1 = o1.getVersionedComment();
					VersionedComment c2 = o2.getVersionedComment();
					return comparator.compare(c1, c2);

				}
				return 0;
			}

		});
	}

	public boolean containsInput() {
		return annotations != null && annotations.size() > 0;
	}

	public List<CrucibleCommentAnnotation> getCrucibleAnnotations() {

		return annotations;
	}
}
