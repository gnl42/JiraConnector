package com.atlassian.jira.rest.client.domain.input;

import com.atlassian.jira.rest.client.TestUtil;
import org.junit.Test;

import java.net.URI;

import static com.atlassian.jira.rest.client.domain.input.WorklogInput.AdjustEstimate;
import static com.atlassian.jira.rest.client.domain.input.WorklogInputBuilder.ESTIMATE_UNIT_MINUTES;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

public class WorklogInputBuilderTest {

	private URI dummyUri = TestUtil.toUri("http://jira.atlassian.com/");

	@Test
	public void testSetAdjustEstimateNewMinutes() throws Exception {
		final int newEstimateMinutes = 12345;

		final WorklogInput worklogInput = new WorklogInputBuilder(dummyUri)
				.setAdjustEstimateNew(newEstimateMinutes)
				.build();

		assertThat(worklogInput.getAdjustEstimate(), equalTo(AdjustEstimate.NEW));
		assertThat(worklogInput.getAdjustEstimateValue(), equalTo(newEstimateMinutes + ESTIMATE_UNIT_MINUTES));
	}

	@Test
	public void testSetAdjustEstimateNew() throws Exception {
		final String newEstimate = "1w 2d 3h 5m";

		final WorklogInput worklogInput = new WorklogInputBuilder(dummyUri)
				.setAdjustEstimateNew(newEstimate)
				.build();

		assertThat(worklogInput.getAdjustEstimate(), equalTo(AdjustEstimate.NEW));
		assertThat(worklogInput.getAdjustEstimateValue(), equalTo(newEstimate));
	}

	@Test
	public void testSetAdjustEstimateLeave() throws Exception {
		final WorklogInput worklogInput = new WorklogInputBuilder(dummyUri)
				.setAdjustEstimateLeave()
				.build();

		assertThat(worklogInput.getAdjustEstimate(), equalTo(AdjustEstimate.LEAVE));
		assertThat(worklogInput.getAdjustEstimateValue(), nullValue());
	}

	@Test
	public void testSetAdjustEstimateManualMinutes() throws Exception {
		final int reduceEstimateByMinutes = 54321;

		final WorklogInput worklogInput = new WorklogInputBuilder(dummyUri)
				.setAdjustEstimateManual(reduceEstimateByMinutes)
				.build();

		assertThat(worklogInput.getAdjustEstimate(), equalTo(AdjustEstimate.MANUAL));
		assertThat(worklogInput.getAdjustEstimateValue(), equalTo(reduceEstimateByMinutes + ESTIMATE_UNIT_MINUTES));
	}

	@Test
	public void testSetAdjustEstimateManual() throws Exception {
		final String reduceEstimateBy = "1w 2d 3h 5m";

		final WorklogInput worklogInput = new WorklogInputBuilder(dummyUri)
				.setAdjustEstimateManual(reduceEstimateBy)
				.build();

		assertThat(worklogInput.getAdjustEstimate(), equalTo(AdjustEstimate.MANUAL));
		assertThat(worklogInput.getAdjustEstimateValue(), equalTo(reduceEstimateBy));
	}

	@Test
	public void testSetAdjustEstimateAuto() throws Exception {
		final WorklogInput worklogInput = new WorklogInputBuilder(dummyUri)
				.setAdjustEstimateAuto()
				.build();

		assertThat(worklogInput.getAdjustEstimate(), equalTo(AdjustEstimate.AUTO));
		assertThat(worklogInput.getAdjustEstimateValue(), nullValue());
	}
}
