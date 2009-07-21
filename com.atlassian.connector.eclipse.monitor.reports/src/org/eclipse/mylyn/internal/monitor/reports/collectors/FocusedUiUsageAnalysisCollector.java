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

package org.eclipse.mylyn.internal.monitor.reports.collectors;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.DateUtil;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.monitor.reports.MonitorReportsPlugin;
import org.eclipse.mylyn.internal.monitor.usage.ReportGenerator;
import org.eclipse.mylyn.internal.tasks.ui.actions.TaskActivateAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.TaskDeactivateAction;
import org.eclipse.mylyn.monitor.core.InteractionEvent;

/**
 * Delegates to other collectors for additional info.
 * 
 * @author Mik Kersten
 */
public class FocusedUiUsageAnalysisCollector extends AbstractMylynUsageCollector {

	public static final int BASELINE_EDITS_THRESHOLD = 1000;

	private static final int MYLYN_EDITS_THRESHOLD = 3000;

	private static final int NUM_VIEWS_REPORTED = 5;

	private float summaryEditRatioDelta = 0;

	private final List<Integer> usersImproved = new ArrayList<Integer>();

	private final List<Integer> usersDegraded = new ArrayList<Integer>();

	private final Map<Integer, Date> startDates = new HashMap<Integer, Date>();

	private final Map<Integer, Integer> numMylynActiveJavaEdits = new HashMap<Integer, Integer>();

	private final Map<Integer, Date> endDates = new HashMap<Integer, Date>();

	private final Map<Integer, Integer> baselineSelections = new HashMap<Integer, Integer>();

	private final Map<Integer, Integer> baselineEdits = new HashMap<Integer, Integer>();

	private final Map<Integer, Integer> mylynInactiveSelections = new HashMap<Integer, Integer>();

	private final Map<Integer, Integer> mylynInactiveEdits = new HashMap<Integer, Integer>();

	private final Map<Integer, Integer> mylynSelections = new HashMap<Integer, Integer>();

	private final Map<Integer, Integer> mylynEdits = new HashMap<Integer, Integer>();

	private final Map<Integer, Integer> baselineCurrentNumSelectionsBeforeEdit = new HashMap<Integer, Integer>();

	private final Map<Integer, Integer> baselineTotalSelectionsBeforeEdit = new HashMap<Integer, Integer>();

	private final Map<Integer, Integer> baselineTotalEditsCounted = new HashMap<Integer, Integer>();

	private final Map<Integer, Integer> mylynCurrentNumSelectionsBeforeEdit = new HashMap<Integer, Integer>();

	private final Map<Integer, Integer> mylynTotalSelectionsBeforeEdit = new HashMap<Integer, Integer>();

	private final Map<Integer, Integer> mylynTotalEditsCounted = new HashMap<Integer, Integer>();

	private final Map<Integer, InteractionEvent> lastUserEvent = new HashMap<Integer, InteractionEvent>();

	private final Map<Integer, Long> timeMylynActive = new HashMap<Integer, Long>();

	private final Map<Integer, Long> timeMylynInactive = new HashMap<Integer, Long>();

	private final Map<Integer, Long> timeBaseline = new HashMap<Integer, Long>();

	private final FocusedUiViewUsageCollector viewUsageCollector = new FocusedUiViewUsageCollector();

	public FocusedUiUsageAnalysisCollector() {
		viewUsageCollector.setMaxViewsToReport(NUM_VIEWS_REPORTED);
		super.getDelegates().add(viewUsageCollector);
	}

	@Override
	public String getReportTitle() {
		return "Mylyn Usage";
	}

	@Override
	public void consumeEvent(InteractionEvent event, int userId) {
		super.consumeEvent(event, userId);
		if (!startDates.containsKey(userId)) {
			startDates.put(userId, event.getDate());
		}
		endDates.put(userId, event.getDate());

		// Mylyn is active
		if (mylynUserIds.contains(userId) && !mylynInactiveUserIds.contains(userId)) {
			accumulateDuration(event, userId, timeMylynActive);
			if (isJavaEdit(event)) {
				incrementCount(userId, numMylynActiveJavaEdits);
			}
			if (isSelection(event)) {
				incrementCount(userId, mylynSelections);
				incrementCount(userId, mylynCurrentNumSelectionsBeforeEdit);
			} else if (isEdit(event)) {
				incrementCount(userId, mylynEdits);

				if (mylynCurrentNumSelectionsBeforeEdit.containsKey((userId))) {
					int num = mylynCurrentNumSelectionsBeforeEdit.get(userId);
					if (num > 0) {
						incrementCount(userId, mylynTotalEditsCounted);
						incrementCount(userId, mylynTotalSelectionsBeforeEdit, num);
						mylynCurrentNumSelectionsBeforeEdit.put(userId, 0);
					}
				}
			}
			// Mylyn is inactive
		} else if (mylynInactiveUserIds.contains(userId)) {
			accumulateDuration(event, userId, timeMylynInactive);
			if (isSelection(event)) {
				incrementCount(userId, mylynInactiveSelections);
			} else if (isEdit(event)) {
				incrementCount(userId, mylynInactiveEdits);
			}
			// Baseline
		} else {
			accumulateDuration(event, userId, timeBaseline);
			if (isSelection(event)) {
				incrementCount(userId, baselineSelections);

				incrementCount(userId, baselineCurrentNumSelectionsBeforeEdit);
			} else if (isEdit(event)) {
				incrementCount(userId, baselineEdits);

				if (baselineCurrentNumSelectionsBeforeEdit.containsKey((userId))) {
					int num = baselineCurrentNumSelectionsBeforeEdit.get(userId);
					if (num > 0) {
						incrementCount(userId, baselineTotalEditsCounted);
						incrementCount(userId, baselineTotalSelectionsBeforeEdit, num);
						baselineCurrentNumSelectionsBeforeEdit.put(userId, 0);
					}
				}
			}
		}
	}

	private void accumulateDuration(InteractionEvent event, int userId, Map<Integer, Long> timeAccumulator) {
		// Restart accumulation if greater than 5 min has elapsed between events
		if (lastUserEvent.containsKey(userId)) {
			long elapsed = event.getDate().getTime() - lastUserEvent.get(userId).getDate().getTime();

			if (elapsed < 5 * 60 * 1000) {
				if (!timeAccumulator.containsKey(userId)) {
					timeAccumulator.put(userId, new Long(0));
				}
				timeAccumulator.put(userId, timeAccumulator.get(userId) + elapsed);
			}
		}
		lastUserEvent.put(userId, event);
	}

	public static boolean isEdit(InteractionEvent event) {
		return event.getKind().equals(InteractionEvent.Kind.EDIT)
				|| (event.getKind().equals(InteractionEvent.Kind.SELECTION) && isSelectionInEditor(event));
	}

	public static boolean isSelection(InteractionEvent event) {
		return event.getKind().equals(InteractionEvent.Kind.SELECTION) && !isSelectionInEditor(event);
	}

	public static boolean isSelectionInEditor(InteractionEvent event) {
		return event.getOriginId().contains("Editor") || event.getOriginId().contains("editor")
				|| event.getOriginId().contains("source");
	}

	public static boolean isJavaEdit(InteractionEvent event) {
		return event.getKind().equals(InteractionEvent.Kind.EDIT)
				&& (event.getOriginId().contains("java") || event.getOriginId().contains("jdt.ui"));
	}

	private void incrementCount(int userId, Map<Integer, Integer> map, int count) {
		if (!map.containsKey(userId)) {
			map.put(userId, 0);
		}
		map.put(userId, map.get(userId) + count);
	}

	private void incrementCount(int userId, Map<Integer, Integer> map) {
		incrementCount(userId, map, 1);
	}

	@Override
	public List<String> getReport() {
		usersImproved.clear();
		usersDegraded.clear();
		int acceptedUsers = 0;
		int rejectedUsers = 0;
		summaryEditRatioDelta = 0;
		List<String> report = new ArrayList<String>();
		for (int id : userIds) {
			if (acceptUser(id)) {
				report.add("<h3>USER ID: " + id + " (from: " + getStartDate(id) + " to " + getEndDate(id) + ")</h3>");
				acceptedUsers++;

				float baselineRatio = getBaselineRatio(id);
				float mylynInactiveRatio = getMylynInactiveRatio(id);
				float mylynActiveRatio = getMylynRatio(id);
				float combinedMylynRatio = mylynInactiveRatio + mylynActiveRatio;

				float ratioPercentage = (combinedMylynRatio - baselineRatio) / baselineRatio;
				if (ratioPercentage > 0) {
					usersImproved.add(id);
				} else {
					usersDegraded.add(id);
				}
				summaryEditRatioDelta += ratioPercentage;
				String baselineVsMylynRatio = "Baseline vs. Mylyn edit ratio: " + baselineRatio + ", mylyn: "
						+ combinedMylynRatio + ",  ";
				String ratioChange = ReportGenerator.formatPercentage(100 * ratioPercentage);
				baselineVsMylynRatio += " <b>change: " + ratioChange + "%</b>";
				report.add(baselineVsMylynRatio + "<br>");

				report.add("<h4>Activity</h4>");
				float editsActive = getNumMylynEdits(id);
				float editsInactive = getNumInactiveEdits(id);
				report.add("Proportion Mylyn active (by edits): <b>"
						+ ReportGenerator.formatPercentage(100 * ((editsActive) / (editsInactive + editsActive)))
						+ "%</b><br>");

				report.add("Elapsed time baseline: " + getTime(id, timeBaseline) + ", active: "
						+ getTime(id, timeMylynActive) + ", inactive: " + getTime(id, timeMylynInactive) + "<br>");

				report.add("Selections baseline: " + getNumBaselineSelections(id) + ", Mylyn active: "
						+ getNumMylynSelections(id) + ", inactive: " + getNumMylynInactiveSelections(id) + "<br>");
				report.add("Edits baseline: " + getNumBaselineEdits(id) + ", Mylyn active: " + getNumMylynEdits(id)
						+ ", inactive: " + getNumInactiveEdits(id) + "<br>");

				int numTaskActivations = commandUsageCollector.getCommands().getUserCount(id, TaskActivateAction.ID);
				int numTaskDeactivations = commandUsageCollector.getCommands()
						.getUserCount(id, TaskDeactivateAction.ID);
				report.add("Task activations: " + numTaskActivations + ", ");
				report.add("deactivations: " + numTaskDeactivations + "<br>");

				int numIncrement = commandUsageCollector.getCommands().getUserCount(id,
						"org.eclipse.mylyn.ui.interest.increment");
				int numDecrement = commandUsageCollector.getCommands().getUserCount(id,
						"org.eclipse.mylyn.ui.interest.decrement");
				report.add("Interest increments: " + numIncrement + ", ");
				report.add("Interest decrements: " + numDecrement + "<br>");

				report.addAll(viewUsageCollector.getSummary(id, true));
				report.add(ReportGenerator.SUMMARY_SEPARATOR);
			} else {
				rejectedUsers++;
			}
		}
		report.add("<h3>Summary</h3>");
		String acceptedSummary = " (based on " + acceptedUsers + " accepted, " + rejectedUsers + " rejected users)";
		float percentage = summaryEditRatioDelta / acceptedUsers;
		String ratioChange = ReportGenerator.formatPercentage(100 * (percentage - 1));
		if (percentage >= 1) {
			report.add("Overall edit ratio improved by: " + ratioChange + "% " + acceptedSummary + "<br>");
		} else {
			report.add("Overall edit ratio degraded by: " + ratioChange + "% " + acceptedSummary + "<br>");
		}
		report.add("degraded: " + usersDegraded.size() + ", improved: " + usersImproved.size() + "<br>");
		report.add(ReportGenerator.SUMMARY_SEPARATOR);
		return report;
	}

	@Override
	public void exportAsCSVFile(String directory) {
		FileWriter writer;
		try {
			writer = new FileWriter(directory + "/mylyn-usage.csv");
			writer.write("userid, "
					+ "ratio-baseline, ratio-mylyn, "
					+ "ratio-improvement, "
					+ "filtered-explorer, "
					+ "filtered-outline, "
					+ "filtered-problems, "
					+ "edits-active, "
					+ "time-baseline, time-active, time-inactive, "
					+ "task-activations, task-deactivations, sel-interesting, sel-predicted, sel-decayed, sel-new, sel-unknown\n");
			// "filtered-explorer, filtered-outline, filtered-problems, ");

			for (int userId : userIds) {
				if (acceptUser(userId)) {
					writer.write(userId + ", ");
					float baselineRatio = getBaselineRatio(userId);
					float mylynInactiveRatio = getMylynInactiveRatio(userId);
					float mylynActiveRatio = getMylynRatio(userId);
					float combinedMylynRatio = mylynInactiveRatio + mylynActiveRatio;

					writer.write(baselineRatio + ", ");
					writer.write(combinedMylynRatio + ", ");

					float ratioPercentage = (combinedMylynRatio - baselineRatio) / baselineRatio;
					writer.write(100 * ratioPercentage + ", ");

					Map<String, Integer> filteredViewSelections = viewUsageCollector.usersFilteredViewSelections.get(userId);
					Map<String, Integer> normalViewSelections = viewUsageCollector.getUsersNormalViewSelections().get(
							userId);

					String[] views = new String[] { "org.eclipse.jdt.ui.PackageExplorer",
							"org.eclipse.ui.views.ContentOutline", "org.eclipse.ui.views.ProblemView" };
					for (String view : views) {
						if (normalViewSelections.containsKey(view) && filteredViewSelections.containsKey(view)) {
							float normalSelections = normalViewSelections.get(view);
							float filteredSelections = filteredViewSelections.get(view);
							float ratio = filteredSelections / (normalSelections + filteredSelections);
							// int unfilteredSelections = normalSelections -
							// filteredSelections;
							if (ratio >= 0.01) {
								writer.write(ratio + ", ");
							} else {
								writer.write("0, ");
							}
						} else {
							writer.write("0, ");
						}
					}

					float editsActive = getNumMylynEdits(userId);
					float editsInactive = getNumInactiveEdits(userId);
					writer.write(100 * ((editsActive) / (editsInactive + editsActive)) + ", ");

					writer.write(getTime(userId, timeBaseline) + ", ");
					writer.write(getTime(userId, timeMylynActive) + ", ");
					writer.write(getTime(userId, timeMylynInactive) + ", ");

					int numTaskActivations = commandUsageCollector.getCommands().getUserCount(userId,
							TaskActivateAction.ID);
					int numTaskDeactivations = commandUsageCollector.getCommands().getUserCount(userId,
							TaskDeactivateAction.ID);
					writer.write(numTaskActivations + ", ");
					writer.write(numTaskDeactivations + ", ");

					int numNew = 0;
					if (viewUsageCollector.usersNumNew.containsKey(userId)) {
						numNew = viewUsageCollector.usersNumNew.get(userId);
					}
					int numPredicted = 0;
					if (viewUsageCollector.usersNumPredicted.containsKey(userId)) {
						numPredicted = viewUsageCollector.usersNumPredicted.get(userId);
					}
					int numInteresting = 0;
					if (viewUsageCollector.usersNumDefault.containsKey(userId)) {
						numInteresting = viewUsageCollector.usersNumDefault.get(userId);
					}
					int numDecayed = 0;
					if (viewUsageCollector.usersNumDecayed.containsKey(userId)) {
						numDecayed = viewUsageCollector.usersNumDecayed.get(userId);
					}
					int numUnknown = 0;
					if (viewUsageCollector.usersNumUnknown.containsKey(userId)) {
						numUnknown = viewUsageCollector.usersNumUnknown.get(userId);
					}

					// float numSelections = numNew + numPredicted +
					// numInteresting + numDecayed + numUnknown;
					// writer.write(numSelections + ", ");
					writer.write(numInteresting + ", ");
					writer.write(numPredicted + ", ");
					writer.write(numDecayed + ", ");
					writer.write(numNew + ", ");
					writer.write(numUnknown + ", ");

					writer.write("\n");
				}
			}
			writer.close();
		} catch (IOException e) {
			StatusHandler.fail(new Status(IStatus.ERROR, MonitorReportsPlugin.ID_PLUGIN, "Could not generate csv file",
					e));
		}
	}

	private String getTime(int id, Map<Integer, Long> timeMap) {
		if (timeMap.containsKey(id)) {
			long timeInSeconds = timeMap.get(id) / 1000;
			long hours, minutes;
			hours = timeInSeconds / 3600;
			timeInSeconds = timeInSeconds - (hours * 3600);
			minutes = timeInSeconds / 60;
			timeInSeconds = timeInSeconds - (minutes * 60);
			return hours + "." + minutes;
		} else {
			return "0";
		}
	}

	public boolean acceptUser(int id) {
		// XXX: delete
		// int[] ACCEPTED = {
		// 1922,
		// 970,
		// 1650,
		// 1548,
		// 1565,
		// 1752,
		// 2194,
		// 2364,
		// 1735,
		// 936,
		// 1803,
		// 2007,
		// 1208,
		// 1684,
		// 919,
		// 2041,
		// 1174
		// };
		// for (int i : ACCEPTED) {
		// if (i == id) return true;
		// }
		// return false;
		if (!numMylynActiveJavaEdits.containsKey(id)) {
			return false;
		} else {
			return getNumBaselineEdits(id) > BASELINE_EDITS_THRESHOLD && getNumMylynEdits(id) > MYLYN_EDITS_THRESHOLD;
		}
	}

	public String getStartDate(int id) {
		Calendar start = Calendar.getInstance();
		start.setTime(startDates.get(id));
		return DateUtil.getIsoFormattedDate(start);
	}

	public String getEndDate(int id) {
		Calendar end = Calendar.getInstance();
		end.setTime(endDates.get(id));
		return DateUtil.getIsoFormattedDate(end);
	}

	public int getNumBaselineSelections(int id) {
		if (baselineSelections.containsKey(id)) {
			return baselineSelections.get(id);
		} else {
			return 0;
		}
	}

	public int getNumBaselineEdits(int id) {
		if (baselineEdits.containsKey(id)) {
			return baselineEdits.get(id);
		} else {
			return 0;
		}
	}

	public int getNumMylynEdits(int id) {
		if (mylynEdits.containsKey(id)) {
			return mylynEdits.get(id);
		} else {
			return 0;
		}
	}

	public int getNumMylynInactiveEdits(int id) {
		if (mylynInactiveEdits.containsKey(id)) {
			return mylynInactiveEdits.get(id);
		} else {
			return 0;
		}
	}

	public int getNumInactiveEdits(int id) {
		if (mylynInactiveEdits.containsKey(id)) {
			return mylynInactiveEdits.get(id);
		} else {
			return 0;
		}
	}

	public int getNumMylynInactiveSelections(int id) {
		if (mylynInactiveSelections.containsKey(id)) {
			return mylynInactiveSelections.get(id);
		} else {
			return 0;
		}
	}

	public int getNumMylynSelections(int id) {
		if (mylynSelections.containsKey(id)) {
			return mylynSelections.get(id);
		} else {
			return 0;
		}
	}

	/**
	 * Public for testing.
	 */
	public float getBaselineRatio(int id) {
		return getEditRatio(id, baselineEdits, baselineSelections);
	}

	public float getMylynInactiveRatio(int id) {
		return getEditRatio(id, mylynInactiveEdits, mylynInactiveSelections);
	}

	/**
	 * Public for testing.
	 */
	public float getMylynRatio(int id) {
		return getEditRatio(id, mylynEdits, mylynSelections);
	}

	private float getEditRatio(int id, Map<Integer, Integer> edits, Map<Integer, Integer> selections) {
		if (edits.containsKey(id) && selections.containsKey(id)) {
			return (float) edits.get(id) / (float) selections.get(id);
		} else {
			return 0f;
		}
	}
}
