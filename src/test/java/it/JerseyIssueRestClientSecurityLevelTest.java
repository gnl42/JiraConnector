package it;

import com.atlassian.jira.nimblefunctests.annotation.JiraBuildNumberDependent;
import com.atlassian.jira.nimblefunctests.annotation.RestoreOnce;
import com.atlassian.jira.rest.client.GetCreateIssueMetadataOptionsBuilder;
import com.atlassian.jira.rest.client.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.domain.CimIssueType;
import com.atlassian.jira.rest.client.domain.CimProject;
import com.atlassian.jira.rest.client.domain.SecurityLevel;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.net.URI;
import java.util.Map;

import static com.atlassian.jira.rest.client.internal.ServerVersionConstants.BN_JIRA_5;
import static org.junit.Assert.assertEquals;

/**
 * User: kalamon
 * Date: 05.12.12
 * Time: 11:09
 */
@RestoreOnce("export-for-security-level-tests.xml")
public class JerseyIssueRestClientSecurityLevelTest extends AbstractJerseyRestClientTest {
    @JiraBuildNumberDependent(BN_JIRA_5)
    @Test
    public void testGetIssueWithSecurityLevel() {
        Map<URI, SecurityLevel> levels = Maps.newHashMap();
        GetCreateIssueMetadataOptionsBuilder builder = new GetCreateIssueMetadataOptionsBuilder();
        builder.withExpandedIssueTypesFields().withProjectKeys("TESTSEC");
        Iterable<CimProject> metadata = client.getIssueClient().getCreateIssueMetadata(builder.build(), pm);
        for (CimProject project : metadata) {
            for (CimIssueType type : project.getIssueTypes()) {
                Map<String, CimFieldInfo> fields = type.getFields();
                CimFieldInfo security = fields.get("security");
                if (security != null) {
                    Iterable<Object> allowedValues = security.getAllowedValues();
                    if (allowedValues == null) {
                        continue;
                    }
                    for (Object level : allowedValues) {
                        SecurityLevel l = (SecurityLevel) level;
                        levels.put(l.getSelf(), l);
                    }
                }
            }
        }
        assertEquals(2, levels.size());
        URI l1 = URI.create("http://localhost:2990/jira/rest/api/2/securitylevel/10000");
        SecurityLevel sl1 = levels.get(l1);
        assertEquals(Long.valueOf(10000), sl1.getId());
        assertEquals("level 1", sl1.getName());
        URI l2 = URI.create("http://localhost:2990/jira/rest/api/2/securitylevel/10001");
        SecurityLevel sl2 = levels.get(l2);
        assertEquals(Long.valueOf(10001), sl2.getId());
        assertEquals("level 2", sl2.getName());
    }
}
