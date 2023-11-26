package it;

import com.atlassian.jira.nimblefunctests.framework.NimbleFuncTestCase;
import com.atlassian.jira.rest.client.IntegrationTestUtil;
import com.atlassian.jira.rest.client.internal.json.TestConstants;
import com.atlassian.jira.testkit.client.Backdoor;
import com.atlassian.jira.testkit.client.util.TestKitLocalEnvironmentData;
import org.junit.Ignore;
import org.junit.Test;

public class RestoreDataDevHelperButNotRealTest extends NimbleFuncTestCase {

    private final Backdoor backdoor = new Backdoor(new TestKitLocalEnvironmentData());

    @Ignore // test disabled on CI, enable before use
    @Test
    public void restoreDataForUnitTests() throws Exception {
        IntegrationTestUtil.restoreAppropriateJiraData(TestConstants.DATA_FOR_UNIT_TESTS_FILE, administration);
    }

    @Ignore // test disabled on CI, enable before use
    @Test
    public void restoreDataWithFilters() throws Exception {
        IntegrationTestUtil.restoreAppropriateJiraData(TestConstants.JIRA_DUMP_WITH_FILTERS_FILE, administration);
    }

    @Ignore // test disabled on CI, enable before use
    @Test
    public void restoreDataForCreatingIssueTests() throws Exception {
        IntegrationTestUtil.restoreAppropriateJiraData(TestConstants.JIRA_DUMP_CREATING_ISSUE_TESTS_FILE, administration);
    }
}
