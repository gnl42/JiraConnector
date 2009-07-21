/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brian de Alwis - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.monitor.ui;

/**
 * Notified of monitor life-cycle changes.
 * 
 * @author Brian de Alwis
 * @since 3.0
 */
public interface IMonitorLifecycleListener {

	public void startMonitoring();

	public void stopMonitoring();

}
