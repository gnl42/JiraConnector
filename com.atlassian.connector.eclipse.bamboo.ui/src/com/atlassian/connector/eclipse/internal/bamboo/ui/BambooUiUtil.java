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

package com.atlassian.connector.eclipse.internal.bamboo.ui;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
import com.atlassian.theplugin.commons.bamboo.PlanState;
import com.atlassian.theplugin.commons.bamboo.TestDetails;

import org.eclipse.mylyn.internal.provisional.commons.ui.CommonFonts;
import org.eclipse.swt.graphics.Font;

import java.util.Iterator;

final public class BambooUiUtil {
	private BambooUiUtil() {
		// ignore
	}

	public static final String LOG_STR_ERROR = "error";

	public static String getCommentSnippet(String comment) {
		String[] commentLines = comment.split("[\r\n]");
		return commentLines.length == 0 ? "N/A" : commentLines[0];
	}

	public static String formatTestMethodName(String methodName) {
		int i = methodName.indexOf("test");
		if (i != -1) {
			methodName = methodName.substring(i + 4);
		}
		StringBuilder b = new StringBuilder();
		for (char c : methodName.toCharArray()) {
			if (Character.isUpperCase(c)) {
				b.append(" ");
			}
			b.append(c);
		}
		return b.toString();
	}

	public static String getFailedTestsDescription(BuildDetails buildDetails) {
		if (buildDetails == null) {
			return null;
		}
		StringBuilder b = new StringBuilder();
		Iterator<TestDetails> it = buildDetails.getFailedTestDetails().iterator();
		while (it.hasNext()) {
			TestDetails details = it.next();
			String testClassName = details.getTestClassName();
			int index = testClassName.lastIndexOf('.');
			if (index == -1) {
				testClassName = "N/A";
			} else {
				testClassName = testClassName.substring(index + 1);
			}
			b.append(testClassName);
			b.append("  : ");
			b.append(BambooUiUtil.formatTestMethodName(details.getTestMethodName()));
			if (it.hasNext()) {
				b.append("\n");
			}
		}
		return b.toString();
	}

	public static Font getFontForBuildStatus(Object element) {
		if (element instanceof EclipseBambooBuild) {
			BambooBuild build = ((EclipseBambooBuild) element).getBuild();
			if (build.getPlanState() == PlanState.BUILDING || build.getPlanState() == PlanState.IN_QUEUE) {
				return CommonFonts.ITALIC;
			}
		}
		return null;
	}

}
