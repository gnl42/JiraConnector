/*
 * Copyright (C) 2010 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.jira.rest.restjavaclient.internal.json;

import com.atlassian.jira.rest.restjavaclient.domain.Attachment;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

import static com.atlassian.jira.rest.restjavaclient.TestUtil.toUri;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class AttachmentJsonParserTest {
	@Test
	public void testParse() throws Exception {
		final AttachmentJsonParser parser = new AttachmentJsonParser();
		final Attachment attachment = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/attachment/valid.json"));

		Assert.assertEquals(new Attachment(toUri("http://localhost:8090/jira/rest/api/latest/attachment/10031"),
				"snipe.png", TestConstants.USER_ADMIN, new DateTime(2010, 7, 26, 13, 31, 35, 577, DateTimeZone.forOffsetHours(2)),
				31020, "image/png", toUri("http://localhost:8090/jira/secure/attachment/10031/snipe.png"),
				toUri("http://localhost:8090/jira/secure/thumbnail/10031/10031_snipe.png")), attachment);
	}

}
