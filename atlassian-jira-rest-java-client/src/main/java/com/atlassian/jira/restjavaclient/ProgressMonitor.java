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

package com.atlassian.jira.restjavaclient;

/**
 * All remote operations take a parameter of this interface.<br>
 * Firstly it serves as a clear marker of a remote call.
 * Secondly, in the future, we plan to actually make this interface capable of reporting the progress
 * and cancelling (where possible) remote requests taking too much time.
 *
 * So while you may blame this class for the time being, the plan is that it will actually be beneficial
 * one day without any need to break API compatibility. 
 *
 * @since v0.1
 */
public interface ProgressMonitor {
}
