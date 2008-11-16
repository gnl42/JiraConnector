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

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * Dialog panel interface
 * 
 * @author Alexander Gurov
 */
public interface IDialogPanel {
    public void initPanel(IDialogManager manager);
    public void dispose();
    public void addListeners();
    public void postInit();
    
    public String getDialogTitle();
    public String getDialogDescription();
    public String getDefaultMessage();
    public String getImagePath();
    public Point getPrefferedSize();
    
    public void createControls(Composite parent);
    
    public String []getButtonNames();
    
    public void buttonPressed(int idx);
    public boolean canClose();
    
    public String getHelpId();
    
}
