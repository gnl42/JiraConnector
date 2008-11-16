/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gabor Liptak - initial API and implementation
 *******************************************************************************/
package com.atlassian.theplugin.eclipse.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Pattern cache, and replacement method instead of String.replaceAll, wich is horribly slow.
 * 
 * @author Gabor Liptak
 */
public final class PatternProvider {
	private static final int MAX_CACHE_SIZE = 100;
	
	private static LinkedHashMap<String, Pattern> patterns = new LinkedHashMap<String, Pattern>() {
		private static final long serialVersionUID = 2921759287651173337L;

		protected boolean removeEldestEntry(Map.Entry<String, Pattern> eldest) {
			return this.size() > PatternProvider.MAX_CACHE_SIZE;
		}
	};
	
	public static String replaceAll(String strSource, String strPattern, String strReplacement) {
		return PatternProvider.getPattern(strPattern).matcher(strSource).replaceAll(strReplacement);
	}

	public static synchronized Pattern getPattern(String strPattern) {
		Pattern patternReturn = (Pattern) PatternProvider.patterns.get(strPattern);
		
		//if two threads would need the same new pattern in the same time, only one will compile it
		if (patternReturn == null) {
			patternReturn = Pattern.compile(strPattern);
			PatternProvider.patterns.put(strPattern, patternReturn);
		}
		return patternReturn;
	}
	
	private PatternProvider() {
	}
}
