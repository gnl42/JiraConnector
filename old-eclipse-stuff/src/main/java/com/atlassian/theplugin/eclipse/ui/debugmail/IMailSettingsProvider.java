/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.ui.debugmail;

/**
 * Mail settings provider
 *
 * @author Sergiy Logvin
 */
public interface IMailSettingsProvider {
	/**
	 * Returns report addressee
	 * @return report addressee
	 */
	public String getEmailTo();
	/**
	 * Returns report sender
	 * @return report sender
	 */
	public String getEmailFrom();
	/**
	 * Returns plug-in name
	 * @return plug-in name
	 */
	public String getPluginName();
	/**
	 * Returns plug-in version
	 * @return plug-in version
	 */
	public String getProductVersion();
	/**
	 * Returns mail server host
	 * @return mail server host
	 */
	public String getHost();
	/**
	 * Returns mail server port
	 * @return mail server port
	 */
	public String getPort();
}
