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

import org.eclipse.swt.widgets.Shell;

/**
 * Dialog management interface
 * 
 * @author Alexander Gurov
 */
public interface IDialogManager {
    public static final int LEVEL_OK = 0;
    public static final int LEVEL_WARNING = 1;
    public static final int LEVEL_ERROR = 2;
    
    public Shell getShell();
    
    public void setButtonEnabled(int idx, boolean enabled);
    public boolean isButtonEnabled(int idx);
    public void setMessage(int level, String message);
    
    public void forceClose(int buttonId);
    
}
