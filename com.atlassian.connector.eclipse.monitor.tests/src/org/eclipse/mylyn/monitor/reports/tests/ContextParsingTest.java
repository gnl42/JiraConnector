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

package org.eclipse.mylyn.monitor.reports.tests;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Path;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.core.InteractionContext;
import org.eclipse.mylyn.internal.context.core.InteractionContextScaling;
import org.eclipse.mylyn.internal.monitor.usage.InteractionEventLogger;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.monitor.tests.MonitorTestsPlugin;

/**
 * @author Mik Kersten
 */
public class ContextParsingTest extends TestCase {

	private static final String PATH_USAGE_FILE = "testdata/usage-parsing.zip";

	private List<InteractionEvent> events;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		File file;
		if (MonitorTestsPlugin.getDefault() != null) {
			file = FileTool.getFileInPlugin(MonitorTestsPlugin.getDefault(), new Path(PATH_USAGE_FILE));
		} else {
			file = new File(PATH_USAGE_FILE);
		}
		InteractionEventLogger logger = new InteractionEventLogger(file);
		events = logger.getHistoryFromFile(file);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		events.clear();
	}

	public void testOriginIdValidity() {
		for (InteractionEvent event : events) {
			if (event.isValidStructureHandle()) {
				assertFalse(event.getStructureHandle().equals("null"));
			}
		}
	}

	public void testHistoryParsingWithDecayReset() {
		InteractionContextScaling scalingFactors = new InteractionContextScaling();
		// scalingFactors.setDecay(new ScalingFactor("decay", .05f));
		InteractionContext context = new InteractionContext("test", scalingFactors);
		int numEvents = 0;
		for (InteractionEvent event : events) {
			if (event.isValidStructureHandle()) {
				// if (SelectionMonitor.isValidStructureHandle(event)) {
				InteractionEvent newEvent = InteractionEvent.makeCopy(event, 1f);
				context.parseEvent(newEvent);
				if (event.isValidStructureHandle() && event.getKind().equals(InteractionEvent.Kind.SELECTION)) {
					// if (SelectionMonitor.isValidStructureHandle(event) &&
					// event.getKind().equals(InteractionEvent.Kind.SELECTION))
					// {
					IInteractionElement element = context.parseEvent(event);

					// reset decay if not selected
					if (element.getInterest().getValue() < 0) {
						float decayOffset = (-1) * (element.getInterest().getValue()) + 1;
						element = context.parseEvent(new InteractionEvent(InteractionEvent.Kind.MANIPULATION,
								event.getStructureKind(), event.getStructureHandle(), "test-decay", decayOffset));
					}

					assertTrue("should be positive: " + element.getInterest().getValue(), element.getInterest()
							.getValue() >= 0);
					numEvents++;
				}
			}
		}
	}
}
