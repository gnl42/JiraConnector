/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.monitor.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.mylyn.context.tests.AbstractContextTest;
import org.eclipse.mylyn.internal.monitor.usage.InteractionEventLogger;
import org.eclipse.mylyn.internal.monitor.usage.MonitorPreferenceConstants;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;
import org.eclipse.mylyn.monitor.core.InteractionEvent;

/**
 * @author Mik Kersten
 */
@SuppressWarnings("deprecation")
public class InteractionEventExternalizationTest extends AbstractContextTest {

	private static final String PATH = "test-log.xml";

	public void testXmlStringConversion() {
		String testStrings[] = { "single", "simple string with spaces", "<embedded-xml>",
				"<more complicated=\"xml\"><example with='comp:licated'/></more>",
				"<embedded>\rcarriage-returns\nnewlines\tand tabs" };
		for (String s : testStrings) {
			assertEquals(
					s,
					org.eclipse.mylyn.internal.commons.core.XmlStringConverter.convertXmlToString(org.eclipse.mylyn.internal.commons.core.XmlStringConverter.convertToXmlString(s)));
		}
	}

	public void testManualExternalization() throws IOException {
		UiUsageMonitorPlugin.getPrefs().setValue(MonitorPreferenceConstants.PREF_MONITORING_OBFUSCATE, false);

		List<InteractionEvent> events = new ArrayList<InteractionEvent>();
		File f = new File(PATH);
		if (f.exists()) {
			f.delete();
		}
		InteractionEventLogger logger = new InteractionEventLogger(f);
		logger.clearInteractionHistory();
		logger.startMonitoring();
		String handle = "";
		for (int i = 0; i < 100; i++) {
			handle += "1";
			InteractionEvent event = new InteractionEvent(InteractionEvent.Kind.SELECTION, "structureKind", handle,
					"originId", "navigatedRelation", "delta", 2f, new Date(), new Date());
			events.add(event);
			logger.interactionObserved(event);
		}
		logger.stopMonitoring();

		File infile = new File(PATH);
		List<InteractionEvent> readEvents = logger.getHistoryFromFile(infile);
		for (int i = 0; i < events.size(); i++) {
			// NOTE: shouldn't use toString(), but get timezone failures
			assertEquals(events.get(i), readEvents.get(i));
			// assertEquals(events.get(i), readEvents.get(i));
		}

		infile.delete();
		UiUsageMonitorPlugin.getPrefs().setValue(MonitorPreferenceConstants.PREF_MONITORING_OBFUSCATE, true);
	}
}
