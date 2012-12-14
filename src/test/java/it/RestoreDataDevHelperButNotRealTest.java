package it;

import com.atlassian.jira.nimblefunctests.annotation.Restore;
import com.atlassian.jira.nimblefunctests.framework.NimbleFuncTestCase;
import org.junit.Ignore;
import org.junit.Test;

public class RestoreDataDevHelperButNotRealTest extends NimbleFuncTestCase {

	@Ignore // test disabled on CI, enable before use
	@Restore("data-for-unit-tests.xml")
	@Test
	public void restoreDataForUnitTests() throws Exception {
		// do nothing, just restore
	}

	@Ignore // test disabled on CI, enable before use
	@Restore("jira4-export-with-filters.xml")
	@Test
	public void restoreDataWithFilters() throws Exception {
		// do nothing, just restore
	}
}
