package com.atlassian.connector.eclipse.monitor.server.model;

import java.util.Date;

public class DailyStatistic {

	private Date day;

	private int uploadRequests;

	private int entriesSucceeded;

	private int entriesFailed;

	private int entriesConflicting;

	public Date getDay() {
		return day;
	}

	public void setDay(Date day) {
		this.day = day;
	}

	public int getUploadRequests() {
		return uploadRequests;
	}

	public void setUploadRequests(int uploadRequests) {
		this.uploadRequests = uploadRequests;
	}

	public void setEntriesSucceeded(int entriesSucceeded) {
		this.entriesSucceeded = entriesSucceeded;
	}

	public void setEntriesFailed(int entriesFailed) {
		this.entriesFailed = entriesFailed;
	}

	public void setEntriesConflicting(int entriesConflicting) {
		this.entriesConflicting = entriesConflicting;
	}

	public DailyStatistic() {
		
	}
	
	public DailyStatistic(Date date, int uploadRequests, int entriesS,
			int entriesF, int entriesC) {
		this.day = date;
		this.uploadRequests = uploadRequests;
		this.entriesSucceeded = entriesS;
		this.entriesConflicting = entriesC;
		this.entriesFailed = entriesF;
	}

	public Date getDate() {
		return day;
	}

	public int getAttempts() {
		return uploadRequests;
	}

	public int getEntriesSucceeded() {
		return entriesSucceeded;
	}

	public int getEntriesFailed() {
		return entriesFailed;
	}

	public int getEntriesConflicting() {
		return entriesConflicting;
	}

	public void update(DailyStatistic st) {
		this.uploadRequests += st.uploadRequests;
		this.entriesConflicting += st.entriesConflicting;
		this.entriesFailed += st.entriesFailed;
		this.entriesSucceeded += st.entriesSucceeded;
	}
}
