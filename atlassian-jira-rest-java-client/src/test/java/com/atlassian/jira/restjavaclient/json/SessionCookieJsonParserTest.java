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

package com.atlassian.jira.restjavaclient.json;

import com.atlassian.jira.restjavaclient.domain.SessionCookie;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class SessionCookieJsonParserTest {
    @Test
    public void testParse() throws Exception {
        final SessionCookieJsonParser parser = new SessionCookieJsonParser();
        assertEquals(new SessionCookie("JSESSIONID", "E5BD072ABEE0082DE4D6C8C2B6D96B79"),
                parser.parse(ResourceUtil.getJsonObjectFromResource("/json/sessionCookie/valid.json")));
    }
}
