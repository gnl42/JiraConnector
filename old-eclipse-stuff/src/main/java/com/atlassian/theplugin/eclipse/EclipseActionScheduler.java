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

import org.eclipse.swt.SWTException;

import com.atlassian.theplugin.commons.UIActionScheduler;
import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.util.PluginUtil;

/**
 * @author Jacek
 *
 */
public final class EclipseActionScheduler implements UIActionScheduler {

	private static UIActionScheduler instance = new EclipseActionScheduler();

	private EclipseActionScheduler() {
	}

	public static UIActionScheduler getInstance() {
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see com.atlassian.theplugin.commons.UIActionScheduler#invokeLater(java.lang.Runnable)
	 */
	public void invokeLater(Runnable runnable) {
		try {
			Activator.getDefault().getWorkbench().getDisplay().asyncExec(runnable);
		} catch (SWTException ex) {
			PluginUtil.getLogger().warn(ex);
		} catch (NullPointerException ex) {
			PluginUtil.getLogger().warn(ex);
		}
	}

}
