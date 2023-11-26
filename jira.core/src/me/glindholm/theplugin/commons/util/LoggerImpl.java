/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.glindholm.theplugin.commons.util;

public abstract class LoggerImpl implements Logger {

    /**
     * For backward compatibility, support the overriding the factory with a singleton instance of the
     * logger.
     */
    private static Logger singleton;
    public static final String LOGGER_CATEGORY = "me.glindholm.theplugin";

    @Override
    public boolean isDebugEnabled() {
        return debug;
    }

    public static Logger getInstance() {
        if (singleton != null) {
            return singleton;
        } else {
            System.out.println("Logger not initialized");
            return new NullLogger();
        }
    }

    private static boolean debug;

    private static boolean verbose;

    /**
     * If set, this instance will be returned by all requests made to getInstance, overriding the
     * factory implementation and thereby providing backward compatibility.
     *
     * @param instance instance
     */
    public static void setInstance(final Logger instance) {
        singleton = instance;
    }

    protected LoggerImpl() {
    }

    // these values mirror Ant's values
    public static final int LOG_ERR = 0;
    public static final int LOG_WARN = 1;
    public static final int LOG_INFO = 2;
    public static final int LOG_VERBOSE = 3;
    public static final int LOG_DEBUG = 4;

    public static void setDebug(final boolean debug) {
        LoggerImpl.debug = debug;
    }

    public static boolean isDebug() {
        return LoggerImpl.debug;
    }

    public static boolean isVerbose() {
        return LoggerImpl.verbose;
    }

    public static void setVerbose(final boolean verbose) {
        LoggerImpl.verbose = verbose;
    }

    @Override
    public void error(final String msg) {
        log(LOG_ERR, msg, null);
    }

    @Override
    public void error(final String msg, final Throwable t) {
        log(LOG_ERR, msg, t);
    }

    @Override
    public void error(final Throwable t) {
        log(LOG_ERR, t != null ? t.getMessage() : "Exception", t);
    }

    @Override
    public void warn(final String msg) {
        log(LOG_WARN, msg, null);
    }

    @Override
    public void warn(final String msg, final Throwable t) {
        log(LOG_WARN, msg, t);
    }

    @Override
    public void warn(final Throwable t) {
        log(LOG_WARN, t != null ? t.getMessage() : "Exception", t);
    }

    @Override
    public void info(final String msg) {
        log(LOG_INFO, msg, null);
    }

    @Override
    public void info(final String msg, final Throwable t) {
        log(LOG_INFO, msg, t);
    }

    @Override
    public void info(final Throwable t) {
        log(LOG_INFO, t != null ? t.getMessage() : "Exception", t);
    }

    @Override
    public void verbose(final String msg) {
        log(LOG_VERBOSE, msg, null);
    }

    @Override
    public void verbose(final String msg, final Throwable t) {
        log(LOG_VERBOSE, msg, t);
    }

    @Override
    public void verbose(final Throwable t) {
        log(LOG_VERBOSE, t != null ? t.getMessage() : "Exception", t);
    }

    @Override
    public void debug(final String msg) {
        log(LOG_DEBUG, msg, null);
    }

    @Override
    public void debug(final String msg, final Throwable t) {
        log(LOG_DEBUG, msg, t);
    }

    @Override
    public void debug(final Throwable t) {
        log(LOG_DEBUG, t != null ? t.getMessage() : "Exception", t);
    }

    public static boolean canIgnore(final int level) {
        if (!debug && level == LOG_DEBUG) {
            return true;
        }
        return !(verbose || debug) && level == LOG_VERBOSE;
    }

    static class NullLogger extends LoggerImpl {

        @Override
        public void log(final int level, final String msg, final Throwable t) {
            // no-op
        }
    }
}
