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

package com.atlassian.theplugin.commons.crucible.api.model;

/**
 * Contrary to other enums used by review model, I don't store here information how to persist these enums. I fully believe that
 * such information should not be mixed with the model itself (separation of concerns)
 *
 * @author Wojciech Seliga
 */
public enum ReviewType {
	REVIEW,
	SNIPPET,
}
