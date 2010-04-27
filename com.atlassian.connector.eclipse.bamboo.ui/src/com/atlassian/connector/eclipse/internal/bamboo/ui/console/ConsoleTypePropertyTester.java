/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.bamboo.ui.console;

import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooImages;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.console.IConsole;

public class ConsoleTypePropertyTester extends PropertyTester {

	public ConsoleTypePropertyTester() {
		// ignore
	}

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		IConsole console = (IConsole) receiver;
		String type = console.getType();
		boolean result = type != null ? type.equals(expectedValue) : false;
		return result
				|| ("javaStackTraceConsole".equals(expectedValue) && BambooImages.CONSOLE.equals(console.getImageDescriptor()));
	}

}
