package com.atlassian.jira.rest.client.internal.async;

import com.atlassian.sal.api.executor.ThreadLocalContextManager;

/**
* No operation ThreadLocalContextManager.
*
* @since v6.4
*/
public class NoOpThreadLocalContextManager implements ThreadLocalContextManager
{
        @Override
        public Object getThreadLocalContext()
        {
                return null;
        }

        @Override
        public void setThreadLocalContext(final Object o)
        {
        }

        @Override
        public void clearThreadLocalContext()
        {
        }
}
