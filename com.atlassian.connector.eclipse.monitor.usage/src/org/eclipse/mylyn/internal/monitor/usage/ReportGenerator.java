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

package org.eclipse.mylyn.internal.monitor.usage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.monitor.core.collection.IUsageCollector;
import org.eclipse.mylyn.internal.monitor.core.collection.IUsageScanner;
import org.eclipse.mylyn.internal.monitor.core.collection.InteractionEventComparator;
import org.eclipse.mylyn.internal.monitor.core.collection.InteractionEventSummary;
import org.eclipse.mylyn.internal.monitor.core.collection.InteractionEventUtil;
import org.eclipse.mylyn.monitor.core.InteractionEvent;

/**
 * Used for generating reports of user activity.
 * 
 * @author Mik Kersten
 * @since 3.0
 */
public class ReportGenerator {

	public static final String SUMMARY_SEPARATOR = "<hr><br>";

	private final InteractionEventLogger logger;

	private UsageStatisticsSummary lastParsedSummary = null;

	private final Set<Integer> userIds = new HashSet<Integer>();

	private final List<IUsageCollector> collectors;

	private List<IUsageScanner> scanners;

	private Map<Integer, Map<String, SortedSet<InteractionEvent>>> allUserEvents;

	private boolean saveAllUserEvents = false;

	private IJobChangeListener listener = null;

	private boolean forceSyncForTesting = false;

	public ReportGenerator(InteractionEventLogger logger, IUsageCollector collector, boolean saveAllUserEvents) {
		this(logger, collector);
		this.saveAllUserEvents = saveAllUserEvents;
	}

	public ReportGenerator(InteractionEventLogger logger, List<IUsageCollector> collectors, boolean saveAllUserEvents) {
		this(logger, collectors);
		this.saveAllUserEvents = saveAllUserEvents;
	}

	public ReportGenerator(InteractionEventLogger logger, IUsageCollector collector) {
		List<IUsageCollector> collectors = new ArrayList<IUsageCollector>();
		collectors.add(collector);
		this.logger = logger;
		this.collectors = collectors;
	}

	public ReportGenerator(InteractionEventLogger logger, List<IUsageCollector> collectors) {
		this.logger = logger;
		this.collectors = collectors;
	}

	public ReportGenerator(InteractionEventLogger logger, List<IUsageCollector> collectors,
			List<IUsageScanner> scanners, IJobChangeListener listener) {
		this(logger, collectors);
		this.scanners = scanners;
	}

	public ReportGenerator(InteractionEventLogger logger, IUsageCollector collector, IJobChangeListener listener) {
		this(logger, collector);
		this.listener = listener;
	}

	public ReportGenerator(InteractionEventLogger logger, List<IUsageCollector> collectors,
			IJobChangeListener listener, boolean forceSyncForTesting) {
		this(logger, collectors);
		this.listener = listener;
		this.forceSyncForTesting = forceSyncForTesting;
	}

	public void setScanners(List<IUsageScanner> scanners) {
		this.scanners = scanners;
	}

	public void getStatisticsFromInteractionHistory(File source, IJobChangeListener listener) {
		List<File> sources = new ArrayList<File>();
		sources.add(source);
		getStatisticsFromInteractionHistories(sources, listener);
	}

	public void getStatisticsFromInteractionHistories(List<File> sources, IJobChangeListener jobChangeListener) {

		GenerateStatisticsJob job = new GenerateStatisticsJob(this, sources);
		if (jobChangeListener != null) {
			job.addJobChangeListener(jobChangeListener);
		}
		if (this.listener != null) {
			job.addJobChangeListener(this.listener);
		}
		job.setPriority(Job.LONG);
		if (forceSyncForTesting) {
			job.run(new NullProgressMonitor());
		} else {
			job.schedule();
		}

	}

	public UsageStatisticsSummary getLastParsedSummary() {
		return lastParsedSummary;
	}

	/**
	 * Assuming the file naming convention of <phase>-<version>-usage-<userID>-<date and time>.zip
	 */
	private int getUserId(File source) {
		String userIDText = source.getName();
		int userId = -1;
		String prefix = "-usage-";

		if (source.getName().indexOf(prefix) >= 0) {
			try {
				userIDText = userIDText.substring(userIDText.indexOf(prefix) + prefix.length(), userIDText.length());
				userIDText = userIDText.substring(0, userIDText.indexOf("-"));
				userId = Integer.valueOf(userIDText);
			} catch (Throwable t) {
				StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
						"Could not parse user ID from source file", t));
			}
		}

		return userId;
	}

	private String getPhase(File source) {
		String userIDText = source.getName();
		String phase = "unknown";
		String terminator = "-";

		if (source.getName().indexOf(terminator) >= 0) {
			try {
				phase = userIDText.substring(0, userIDText.indexOf(terminator) - 1);
			} catch (Throwable t) {
				StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
						"Could not parse user ID from source file", t));
			}
		}
		return phase;
	}

	class GenerateStatisticsJob extends Job {

		private static final String JOB_LABEL = "Mylyn Usage Summary Generation";

		private final ReportGenerator generator;

		private final List<File> sources;

		public GenerateStatisticsJob(ReportGenerator generator, List<File> sources) {
			super("Generate statistics job");
			this.generator = generator;
			this.sources = sources;
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {

			if (saveAllUserEvents) {
				allUserEvents = new HashMap<Integer, Map<String, SortedSet<InteractionEvent>>>();
			}

			UsageStatisticsSummary statistics = new UsageStatisticsSummary();
			Map<Integer, Map<String, InteractionEventSummary>> summaryMap = new HashMap<Integer, Map<String, InteractionEventSummary>>();

			Map<Integer, List<File>> filesPerUser = new HashMap<Integer, List<File>>();
			try {

				// Go through the files to determine which users we have to
				// process (by user id)
				for (File source : sources) {
					int userId = getUserId(source);
					userIds.add(userId);
					List<File> filesForUser = null;
					if (filesPerUser.containsKey(userId)) {
						filesForUser = filesPerUser.get(userId);
					}
					if (filesForUser == null) {
						filesForUser = new ArrayList<File>();
					}
					filesForUser.add(source);
					filesPerUser.put(userId, filesForUser);
				}
			} catch (Throwable t) {
				StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
						"Could not generate usage report", t));
			}

			try {
				// There are three processing events per user
				monitor.beginTask(JOB_LABEL, userIds.size() * 3);

				// Process the files for each user
				for (Integer aUser : filesPerUser.keySet()) {
					Map<String, SortedSet<InteractionEvent>> userEvents = new HashMap<String, SortedSet<InteractionEvent>>();

					for (File aFile : filesPerUser.get(aUser)) {
						String phase = getPhase(aFile);

						// orderedEvents must be a set because the
						// monitor-log.xml file contains some duplicate
						// events and we want to be sure that we ignore the
						// duplicates in the reporting. Sets
						// cannot contain duplicates, so orderedEvents will only
						// accept the unique events.
						SortedSet<InteractionEvent> orderedEvents;
						if (userEvents.get(phase) == null) {
							orderedEvents = new TreeSet<InteractionEvent>(new InteractionEventComparator());
							orderedEvents.addAll(this.generator.logger.getHistoryFromFile(aFile));
						} else {
							orderedEvents = userEvents.get(phase);
							orderedEvents.addAll(this.generator.logger.getHistoryFromFile(aFile));
						}
						userEvents.put(phase, orderedEvents);
					}
					monitor.worked(1);

					// If there are scanners registered, give each event to each
					// scanner in turn
					if (this.generator.scanners != null && this.generator.scanners.size() > 0) {

						for (Map.Entry<String, SortedSet<InteractionEvent>> eventsPerPhase : userEvents.entrySet()) {
							// String phaseToProcess = eventsPerPhase.getKey();
							SortedSet<InteractionEvent> events = eventsPerPhase.getValue();

							for (InteractionEvent event : events) {
								for (IUsageScanner scanner : this.generator.scanners) {
									scanner.scanEvent(event, aUser);
								}
							}
						}
					}
					monitor.worked(1);

					if (allUserEvents != null) {
						allUserEvents.put(aUser, userEvents);
					}

					// Consume all events
					for (Map.Entry<String, SortedSet<InteractionEvent>> eventsPerPhase : userEvents.entrySet()) {
						// String phaseToProcess = eventsPerPhase.getKey();
						SortedSet<InteractionEvent> events = eventsPerPhase.getValue();

						for (InteractionEvent event : events) {

							if (event.getKind().isUserEvent()) { // TODO:
								// some
								// collectors
								// may want
								// non-user
								// events
								for (IUsageCollector collector : this.generator.collectors) {
									collector.consumeEvent(event, aUser);
								}
							}
							createUsageTableData(summaryMap, event, aUser);
						}
					}
					monitor.worked(1);
				}

				for (IUsageCollector collector : this.generator.collectors) {
					statistics.add(collector);
				}

				// Flatten the summaries for the command usage table
				List<InteractionEventSummary> flattenedSummaries = new ArrayList<InteractionEventSummary>();
				Map<String, InteractionEventSummary> combinedUserSummary = new HashMap<String, InteractionEventSummary>();

				// Go through the summary for each user and combine the
				// information into a table sorted by the command
				for (Map<String, InteractionEventSummary> userSummary : summaryMap.values()) {
					for (InteractionEventSummary aSummary : userSummary.values()) {
						if (!combinedUserSummary.containsKey(aSummary.getName())) {
							combinedUserSummary.put(aSummary.getName(), new InteractionEventSummary(aSummary));
						} else {
							// Could be simplified; but written this way for
							// clarity
							InteractionEventSummary combinedSummary = combinedUserSummary.get(aSummary.getName());
							combinedSummary.combine(aSummary);
							combinedUserSummary.put(aSummary.getName(), combinedSummary);
						}
					}

				}

				flattenedSummaries.addAll(combinedUserSummary.values());

				statistics.setSingleSummaries(flattenedSummaries);
				this.generator.lastParsedSummary = statistics;
				monitor.done();

			} catch (Throwable t) {
				StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
						"Could not generate usage report", t));
			}

			return Status.OK_STATUS;
		}

		private void createUsageTableData(Map<Integer, Map<String, InteractionEventSummary>> summaryMap,
				InteractionEvent event, int userId) {
			Map<String, InteractionEventSummary> usersSummary = summaryMap.get(userId);
			if (usersSummary == null) {
				usersSummary = new HashMap<String, InteractionEventSummary>();
				summaryMap.put(userId, usersSummary);
			}

			InteractionEventSummary summary = usersSummary.get(getIdentifier(event));
			if (summary == null) {
				summary = new InteractionEventSummary(event.getKind().toString(),
						InteractionEventUtil.getCleanOriginId(event), 0);
				usersSummary.put(getIdentifier(event), summary);
			}
			summary.setUsageCount(summary.getUsageCount() + 1);
			summary.setInterestContribution(summary.getInterestContribution() + event.getInterestContribution());
			summary.setDelta(event.getDelta());
			summary.addUserId(userId);
		}

		public String getIdentifier(InteractionEvent event) {
			return event.getKind().toString() + ':' + InteractionEventUtil.getCleanOriginId(event);
		}

	}

	public static String formatPercentage(float percentage) {
		String percentageString = "" + percentage;
		int indexOf2ndDecimal = percentageString.indexOf('.') + 3;
		if (indexOf2ndDecimal <= percentageString.length()) {
			percentageString = percentageString.substring(0, indexOf2ndDecimal);
		}
		return percentageString;
	}

	public List<IUsageCollector> getCollectors() {
		return collectors;
	}

	public Map<Integer, Map<String, SortedSet<InteractionEvent>>> getAllUsers() {
		return allUserEvents;
	}

	public void forceSyncForTesting(boolean syncForTesting) {
		this.forceSyncForTesting = syncForTesting;

	}
}
