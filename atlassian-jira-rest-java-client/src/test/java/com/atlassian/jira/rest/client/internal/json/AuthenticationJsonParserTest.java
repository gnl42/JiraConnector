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

package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.domain.Authentication;
import com.atlassian.jira.rest.client.domain.LoginInfo;
import com.atlassian.jira.rest.client.domain.SessionCookie;
import org.junit.Test;

import static com.atlassian.jira.rest.client.TestUtil.toDateTime;
import static org.junit.Assert.*;


public class AuthenticationJsonParserTest {
    @Test
    public void testParse() throws Exception {
        final AuthenticationJsonParser parser = new AuthenticationJsonParser();
        final Authentication authentication = parser.parse(ResourceUtil.getJsonObjectFromResource("/json/authentication/valid.json"));
        assertEquals(new Authentication(new LoginInfo(54, 23, toDateTime("2010-09-13T17:19:20.752+0300"),
                toDateTime("2010-09-13T17:19:38.220+0900")), new SessionCookie("JSESSIONID", "E5BD072ABEE0082DE4D6C8C2B6D96B79")), authentication);
    }
}
