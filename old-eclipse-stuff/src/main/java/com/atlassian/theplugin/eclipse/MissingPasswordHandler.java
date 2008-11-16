/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * 
 */
package com.atlassian.theplugin.eclipse;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import com.atlassian.theplugin.eclipse.preferences.Activator;

/**
 * @author Jacek
 *
 */
public class MissingPasswordHandler implements Runnable {

	/** 
	 * That method is designed to be run in UI thread.
	 */
	public void run() {
		MessageBox missingPassword = new MessageBox(Activator.getDefault().getShell(), 
				SWT.ICON_WARNING | SWT.OK);
		
		missingPassword.setText("Warning");
		missingPassword.setMessage("Missing password");
	}

}
