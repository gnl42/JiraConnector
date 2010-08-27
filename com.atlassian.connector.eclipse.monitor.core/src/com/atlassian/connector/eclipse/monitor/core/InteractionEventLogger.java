/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Ken Sueda - XML serialization
 *******************************************************************************/

package com.atlassian.connector.eclipse.monitor.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.JDomDriver;

/**
 * @author Mik Kersten
 * @author Pawel Niewiadomski
 */
public class InteractionEventLogger extends AbstractMonitorLog {

	private final List<InteractionEvent> queue = new CopyOnWriteArrayList<InteractionEvent>();

	private final XStream xs;

	public InteractionEventLogger(File outputFile) {
		this.outputFile = outputFile;
		xs = new XStream(new JDomDriver());
		xs.aliasType("interactionEvent", InteractionEvent.class);
	}

	public synchronized void interactionObserved(InteractionEvent event) {
		if (MonitorCorePlugin.getDefault() == null) {
			StatusHandler.log(new Status(IStatus.WARNING, MonitorCorePlugin.ID_PLUGIN,
					"Attempted to log event before usage monitor start"));
		}
		try {
			if (started) {
				String xml = getXmlForEvent(event);
				outputStream.write(xml.getBytes());
			} else if (event != null) {
				queue.add(event);
			}
		} catch (Throwable t) {
			StatusHandler.log(new Status(IStatus.WARNING, MonitorCorePlugin.ID_PLUGIN,
					"Could not log interaction event", t));
		}
	}

	@Override
	public void startMonitoring() {
		super.startMonitoring();
		for (InteractionEvent queuedEvent : queue) {
			interactionObserved(queuedEvent);
		}
		queue.clear();
	}

	@Override
	public void stopMonitoring() {
		super.stopMonitoring();
	}

	private String getXmlForEvent(InteractionEvent event) {
		try {
			return xs.toXML(event);
		} catch (Throwable t) {
			StatusHandler.log(new Status(IStatus.ERROR, MonitorCorePlugin.ID_PLUGIN, "Could not write event", t));
			return "";
		}
	}

	/**
	 * @return true if successfully cleared
	 */
	public synchronized void clearInteractionHistory() throws IOException {
		this.clearInteractionHistory(true);
	}

	public synchronized void clearInteractionHistory(boolean startMonitoring) throws IOException {
		stopMonitoring();
		outputStream = new FileOutputStream(outputFile, false);
		outputStream.flush();
		outputStream.close();
		outputFile.delete();
		outputFile.createNewFile();
		if (startMonitoring) {
			startMonitoring();
		}
	}

	public List<InteractionEvent> getHistoryFromFile(File file) {
		List<InteractionEvent> events = new ArrayList<InteractionEvent>();
		try {
			// The file may be a zip file...
			if (file.getName().endsWith(".zip")) {
				ZipFile zip = new ZipFile(file);
				if (zip.entries().hasMoreElements()) {
					ZipEntry entry = zip.entries().nextElement();
					getHistoryFromStream(zip.getInputStream(entry), events);
				}
			} else {
				InputStream reader = new FileInputStream(file);
				getHistoryFromStream(reader, events);
				reader.close();
			}

		} catch (Exception e) {
			StatusHandler.log(new Status(IStatus.ERROR, MonitorCorePlugin.ID_PLUGIN,
					"Could not read interaction history", e));
		}
		return events;
	}

	/**
	 * @param events
	 * @param tag
	 * @param endl
	 * @param buf
	 */
	private void getHistoryFromStream(InputStream reader, List<InteractionEvent> events) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write("<list>".getBytes());
		IOUtils.copy(reader, out);
		out.write("</list>".getBytes());

		List<?> list = (List<?>) xs.fromXML(out.toString());
		if (list != null) {
			for (Object e : list) {
				if (e instanceof InteractionEvent) {
					events.add((InteractionEvent) e);
				}
			}
		}
	}

}
