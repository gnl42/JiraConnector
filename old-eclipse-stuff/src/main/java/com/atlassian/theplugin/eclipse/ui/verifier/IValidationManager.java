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

package com.atlassian.theplugin.eclipse.ui.verifier;

import org.eclipse.swt.widgets.Control;


/**
 * Validation manager interface allows us to provide validation API to any component at our choice
 * 
 * @author Alexander Gurov
 */
public interface IValidationManager {

	public void attachTo(Control cmp, AbstractVerifier verifier);
	public void detachFrom(Control cmp);
	public void detachAll();

	public boolean isFilledRight();
	public void validateContent();
	
}
