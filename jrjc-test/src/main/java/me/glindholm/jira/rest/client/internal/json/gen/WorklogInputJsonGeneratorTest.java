/*
 * Copyright (C) 2012 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.glindholm.jira.rest.client.internal.json.gen;

import static me.glindholm.jira.rest.client.TestUtil.toUri;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;

import org.codehaus.jettison.json.JSONException;
import org.junit.Test;

import me.glindholm.jira.rest.client.api.domain.BasicUser;
import me.glindholm.jira.rest.client.api.domain.Visibility;
import me.glindholm.jira.rest.client.api.domain.input.WorklogInput;
import me.glindholm.jira.rest.client.internal.json.JsonParseUtil;
import me.glindholm.jira.rest.client.internal.json.ResourceUtil;
import me.glindholm.jira.rest.client.test.matchers.JSONObjectMatcher;

public class WorklogInputJsonGeneratorTest {

    private final BasicUser USER;
    private final BasicUser ADMIN;
    private final WorklogInputJsonGenerator generator = new WorklogInputJsonGenerator(
            JsonParseUtil.JIRA_DATE_TIME_FORMATTER.withZone(ZoneId.of("+02:00"))
            );

    public WorklogInputJsonGeneratorTest() throws URISyntaxException {
        USER = new BasicUser(new URI("http://localhost:2990/jira/rest/api/2/user?username=wseliga"), "wseliga", "Wojciech Seliga");
        ADMIN = new BasicUser(new URI("http://localhost:2990/jira/rest/api/2/user?username=admin"), "admin", "Administrator");
    }

    @Test
    public void testGenerate() throws JSONException {
        final WorklogInput worklogInput = new WorklogInput(
                toUri("http://localhost:8090/jira/rest/api/latest/worklog/10010"),
                toUri("http://localhost:8090/jira/rest/api/latest/issue/TST-2"), USER, ADMIN, "my first work",
                JsonParseUtil.parseOffsetDateTime("2010-08-15T16:35:00.000+0200"), 60, Visibility.group("some-group"));

        assertThat(generator.generate(worklogInput), JSONObjectMatcher.isEqual(
                ResourceUtil.getJsonObjectFromResource("/json/worklogInput/valid.json")));
    }

    @Test
    public void testGenerateWithoutVisibility() throws JSONException {
        final WorklogInput worklogInput = new WorklogInput(
                toUri("http://localhost:8090/jira/rest/api/latest/worklog/10010"),
                toUri("http://localhost:8090/jira/rest/api/latest/issue/TST-2"), ADMIN, USER, "my first work",
                JsonParseUtil.parseOffsetDateTime("2010-08-15T16:35:00.000+0200"), 43, null);

        assertThat(generator.generate(worklogInput), JSONObjectMatcher.isEqual(
                ResourceUtil.getJsonObjectFromResource("/json/worklogInput/valid-without-visibility.json")));
    }

    @Test
    public void testGenerateWithoutAuthorAndUpdateAuthor() throws JSONException {
        final WorklogInput worklogInput = new WorklogInput(
                toUri("http://localhost:8090/jira/rest/api/latest/worklog/10010"),
                toUri("http://localhost:8090/jira/rest/api/latest/issue/TST-2"), null, null, "my first work",
                JsonParseUtil.parseOffsetDateTime("2010-08-15T16:35:00.000+0200"), 247, Visibility.group("some-group"));

        assertThat(generator.generate(worklogInput), JSONObjectMatcher.isEqual(
                ResourceUtil.getJsonObjectFromResource("/json/worklogInput/valid-without-users.json")));
    }
}
