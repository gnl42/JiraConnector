/*
 * Copyright (C) 2013 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.glindholm.jira.rest.client.internal.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import me.glindholm.jira.rest.client.api.RestClientException;

/**
 * A CompletableFuture wrapper that re-wraps RestClientExceptions from the delegate,
 * providing more useful stack traces.
 */
public class DelegatingPromise<T> extends CompletableFuture<T> {

    public DelegatingPromise(final CompletableFuture<T> delegate) {
        delegate.whenComplete((result, ex) -> {
            if (ex != null) {
                if (ex instanceof RestClientException rce) {
                    completeExceptionally(new RestClientException(rce));
                } else if (ex.getCause() instanceof RestClientException rce) {
                    completeExceptionally(new RestClientException(rce));
                } else {
                    completeExceptionally(ex);
                }
            } else {
                complete(result);
            }
        });
    }

    /**
     * Synchronously waits for and returns the result, similar to the old Promise.claim().
     */
    public T claim() {
        try {
            return get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RestClientException(e);
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RestClientException rce) {
                throw rce;
            }
            throw new RestClientException(cause);
        }
    }
}