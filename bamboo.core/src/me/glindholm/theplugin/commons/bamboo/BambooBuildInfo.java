/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.glindholm.theplugin.commons.bamboo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.glindholm.connector.commons.api.ConnectionCfg;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public final class BambooBuildInfo implements BambooBuild {
	private final Date pollingTime;
	private final ConnectionCfg server;
	@Nullable
	private final String projectName;
    @Nullable
    private final String projectKey;

	private final String planName;
	@Nonnull
	private final String planKey;
    @Nullable
    private final String masterPlanKey;

	private final boolean enabled;
	@Nonnull
	private final BuildStatus status;
	@Nullable
	private final Integer number;
	@Nullable
	private final String reason;
	private final String relativeBuildDate;
	@Nullable
	private final String durationDescription;
	private final String testSummary;
	private final String commitComment;
	private final int testsPassedCount;
	private final int testsFailedCount;
	private final Throwable exception;

	private final String errorMessage;
	@Nullable
	private final Date startDate;
	private final Date completionDate;
	private final Set<String> commiters;
	@Nullable
	private final PlanState planState;

	public BambooBuildInfo(@Nonnull String planKey, @Nullable String planName, @Nullable String masterPlanKey, @Nonnull ConnectionCfg serverData,
			@Nonnull Date pollingTime, @Nullable String projectName, boolean isEnabled, @Nullable Integer number,
			@Nonnull BuildStatus status, @Nullable PlanState planState, @Nullable String reason, @Nullable Date startDate,
			@Nullable String testSummary, @Nullable String commitComment, final int testsPassedCount,
			final int testsFailedCount, @Nullable Date completionDate, @Nullable String errorMessage,
			final Throwable exception, @Nullable String relativeBuildDate, @Nullable String durationDescription,
			@Nullable Collection<String> commiters) {
        this.masterPlanKey = masterPlanKey;
        String[] split = planKey.split("-");
        this.projectKey = split.length > 0 ? split[0] : null;
        this.exception = exception;
		this.pollingTime = new Date(pollingTime.getTime());
		this.planKey = planKey;
		this.planName = planName;
		this.planState = planState;
		this.server = serverData;
		this.projectName = projectName;
		this.enabled = isEnabled;
		this.number = number;
		this.status = status;
		this.reason = reason;
		this.testSummary = testSummary;
		this.commitComment = commitComment;
		this.testsPassedCount = testsPassedCount;
		this.testsFailedCount = testsFailedCount;
		this.errorMessage = errorMessage;
		this.relativeBuildDate = relativeBuildDate;
		this.durationDescription = durationDescription;
		this.startDate = (startDate != null) ? new Date(startDate.getTime()) : null;
		this.completionDate = (completionDate != null) ? new Date(completionDate.getTime()) : null;
		if (commiters != null) {
			this.commiters = new TreeSet<String>(commiters);
		} else {
			this.commiters = new HashSet<String>();
		}
	}

	public ConnectionCfg getServer() {
		return server;
	}

	@Nullable
	public Date getCompletionDate() {
		return completionDate == null ? null : new Date(completionDate.getTime());
	}

    @Nullable
    public String getProjectKey() {
        return projectKey;
    }

    @Nullable
    public String getMasterPlanKey() {
        return masterPlanKey;
    }

    public String getServerUrl() {
		return server.getUrl();
	}

	public String getBuildUrl() {
		return getServerUrl() + "/browse/" + this.planKey;
	}

	public String getResultUrl() {
		String url = getServerUrl() + "/browse/" + this.planKey;
		if (this.getStatus() != BuildStatus.UNKNOWN && this.number != null) {
			url += "-" + this.number;
		}

		return url;
	}

	@Nullable
	public String getProjectName() {
		return projectName;
	}

	public String getPlanName() {
		return planName;
	}

	@Nonnull
	public String getPlanKey() {
		return planKey;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public boolean isValid() {
		return number != null;
	}

	/**
	 * @return build number
	 * @throws UnsupportedOperationException in case this object represents invalid build
	 */
	public int getNumber() throws UnsupportedOperationException {
		if (number == null) {
			throw new UnsupportedOperationException("This build has no number information");
		}
		return number;
	}

	@Nullable
	public String getReason() {
		return reason;
	}

	public String getRelativeBuildDate() {
		return relativeBuildDate;
	}

	@Nullable
	public String getDurationDescription() {
		return durationDescription;
	}

	public String getTestSummary() {
		return testSummary;
	}

	public String getCommitComment() {
		return commitComment;
	}

	@Nonnull
	public BuildStatus getStatus() {
		return status;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public Throwable getException() {
		return exception;
	}

	public int getTestsPassed() {
		return this.testsPassedCount;
	}

	public int getTestsFailed() {
		return this.testsFailedCount;
	}

	@Nullable
	public Date getStartDate() {
		return startDate != null ? new Date(this.startDate.getTime()) : null;
	}

	@Override
	public String toString() {
		return projectName
				+ " " + planName
				+ " " + planKey
				+ " " + status
				+ " " + reason
				+ " " + startDate
				+ " " + durationDescription
				+ " " + testSummary
				+ " " + commitComment;
	}

	/**
	 * @return whether I'm one of the committers to that build or not
	 */
	public boolean isMyBuild() {
		return commiters.contains(server.getUsername());
	}

	/**
	 * @return list of committers for this build
	 */
	public Set<String> getCommiters() {
		return commiters;
	}

	@Nonnull
	public Date getPollingTime() {
		return new Date(pollingTime.getTime());
	}

	@Nullable
	public PlanState getPlanState() {
		return planState;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((number == null) ? 0 : number.hashCode());
		result = prime * result + planKey.hashCode();
		result = prime * result + ((planName == null) ? 0 : planName.hashCode());
		result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
		result = prime * result + ((server == null) ? 0 : server.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		BambooBuildInfo other = (BambooBuildInfo) obj;

		if (number == null) {
			if (other.number != null) {
				return false;
			}
		} else if (!number.equals(other.number)) {
			return false;
		}
		if (!planKey.equals(other.planKey)) {
			return false;
		}
		if (planName == null) {
			if (other.planName != null) {
				return false;
			}
		} else if (!planName.equals(other.planName)) {
			return false;
		}
		if (projectName == null) {
			if (other.projectName != null) {
				return false;
			}
		} else if (!projectName.equals(other.projectName)) {
			return false;
		}
		if (server == null) {
			if (other.server != null) {
				return false;
			}
		} else if (!server.equals(other.server)) {
			return false;
		}
		return true;
	}

	@SuppressWarnings({"InnerClassFieldHidesOuterClassField"})
	public static class Builder {
		private final String planKey;
		private final String planName;
        @Nullable
        private String masterPlanKey;
		private final ConnectionCfg serverData;
		private final String projectName;
		private final Integer buildNumber;
		@Nonnull
		private final BuildStatus buildState;
		private boolean isEnabled = true;
		private String message;
		private Date startTime;
		private Collection<String> commiters;
		@Nonnull
		private Date pollingTime = new Date();
		private String buildReason;
		@Nullable
		private String testSummary;
		@Nullable
		private String commitComment;
		private int testsPassedCount;
		private int testsFailedCount;
		private Date completionTime;
		@Nullable
		private String relativeBuildDate;
		@Nullable
		private String durationDescription;
		private Throwable exception;
		@Nullable
		private PlanState planState;

		public Builder(@Nonnull String planKey, @Nonnull ConnectionCfg serverData, @Nonnull BuildStatus state) {
			this.planKey = planKey;
			this.serverData = serverData;
			this.buildState = state;
			planName = null;
			projectName = null;
			buildNumber = null;
		}

		public Builder(@Nonnull String planKey, @Nullable String planName, @Nonnull ConnectionCfg serverData,
				@Nullable String projectName, @Nullable Integer buildNumber, @Nonnull BuildStatus state) {
			this.planKey = planKey;
            this.planName = planName;
			this.serverData = serverData;
			this.projectName = projectName;
			this.buildNumber = buildNumber;
			this.buildState = state;
		}

        public Builder masterPlanKey(String aMasterPlanKey) {
            this.masterPlanKey = aMasterPlanKey;
            return this;
        }

		public Builder enabled(boolean aIsEnabled) {
			isEnabled = aIsEnabled;
			return this;
		}

		public Builder reason(String aReason) {
			this.buildReason = aReason;
			return this;
		}

		public Builder errorMessage(String aMessage) {
			this.message = aMessage;
			return this;
		}

		public Builder errorMessage(String aMessage, Throwable aException) {
			this.message = aMessage;
			this.exception = aException;
			return this;
		}

		public Builder startTime(Date aStartTime) {
			this.startTime = aStartTime;
			return this;
		}

		public Builder completionTime(Date aCompletionTime) {
			this.completionTime = aCompletionTime;
			return this;
		}

		public Builder commiters(final Collection<String> aCommiters) {
			this.commiters = aCommiters;
			return this;
		}

		public Builder testSummary(@Nullable final String aTestSummary) {
			this.testSummary = aTestSummary;
			return this;
		}

		public Builder commitComment(@Nullable final String aCommitComment) {
			this.commitComment = aCommitComment;
			return this;
		}

		public Builder pollingTime(@Nonnull final Date aPollingTime) {
			this.pollingTime = aPollingTime;
			return this;
		}

		public Builder testsPassedCount(int aTestsPassedCount) {
			this.testsPassedCount = aTestsPassedCount;
			return this;
		}

		public Builder testsFailedCount(int aTestsFailedCount) {
			this.testsFailedCount = aTestsFailedCount;
			return this;
		}

		public Builder relativeBuildDate(@Nullable String aRelativeBuildDate) {
			this.relativeBuildDate = aRelativeBuildDate;
			return this;
		}

		public Builder durationDescription(@Nullable String aDurationDescription) {
			this.durationDescription = aDurationDescription;
			return this;
		}

		public Builder planState(PlanState aPlanState) {
			this.planState = aPlanState;
			return this;
		}

		public BambooBuildInfo build() {
			return new BambooBuildInfo(
                planKey, planName, masterPlanKey, serverData, pollingTime, projectName, isEnabled, buildNumber,
                buildState, planState, buildReason, startTime, testSummary, commitComment, testsPassedCount,
                testsFailedCount, completionTime, message, exception, relativeBuildDate, durationDescription, commiters);
		}
	}

}
