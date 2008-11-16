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

package com.atlassian.theplugin.eclipse;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.eclipse.util.PluginUtil;

public class EclipseLogger extends LoggerImpl {
	
	private ILog eclipseLogger;

	public EclipseLogger(ILog eclipseLogger) {
		this.eclipseLogger = eclipseLogger;
		setInstance(this);
	}

	public void log(int level, String aMsg, Throwable t) {
		
		IStatus status = null;
		
		switch (level) {
			case LoggerImpl.LOG_VERBOSE:
			case LoggerImpl.LOG_DEBUG:
			case LoggerImpl.LOG_INFO:
				status = new Status(IStatus.INFO, PluginUtil.getPluginName(), aMsg, t);
				break;
			case LoggerImpl.LOG_ERR:
				status = new Status(IStatus.ERROR, PluginUtil.getPluginName(), aMsg, t);
				break;
			case LoggerImpl.LOG_WARN:
			default:
				status = new Status(IStatus.WARNING, PluginUtil.getPluginName(), aMsg, t);
		}
		
		eclipseLogger.log(status);

	}

}
