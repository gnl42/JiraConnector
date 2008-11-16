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

package com.atlassian.theplugin.eclipse.ui.panel;

/**
 * Abstract properties panel interface
 * 
 * @author Alexander Gurov
 */
public interface IPropertiesPanel {

	public void saveChanges();
	public void resetChanges();
	public void cancelChanges();
	public void initialize();
	
}
