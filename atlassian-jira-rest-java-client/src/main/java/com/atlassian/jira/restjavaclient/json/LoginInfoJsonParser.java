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

import com.atlassian.jira.restjavaclient.domain.LoginInfo;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class LoginInfoJsonParser implements JsonParser<LoginInfo> {
    @Override
    public LoginInfo parse(JSONObject json) throws JSONException {
        final int failedLoginCount = json.getInt("failedLoginCount");
        final int loginCount = json.getInt("loginCount");
        final DateTime lastFailedLoginTime = JsonParseUtil.parseDateTime(json, "lastFailedLoginTime");
        final DateTime previousLoginTime = JsonParseUtil.parseDateTime(json, "previousLoginTime");
        return new LoginInfo(failedLoginCount, loginCount, lastFailedLoginTime, previousLoginTime);
    }
}
