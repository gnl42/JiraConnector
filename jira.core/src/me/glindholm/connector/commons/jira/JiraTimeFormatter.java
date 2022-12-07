package me.glindholm.connector.commons.jira;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * User: kalamon
 * Date: May 12, 2009
 * Time: 1:26:22 PM
 */
public final class JiraTimeFormatter {

    private JiraTimeFormatter() {
    }

    public static String formatShortTimeFromJiraTimeString(final String dateString, final Locale locale) {
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", locale);
        final DateFormat ds = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        String t;
        if (dateString == null) {
            return "";
        }
        try {
            t = ds.format(df.parse(dateString));
        } catch (final ParseException e) {
            // maybe it is JIRA 4.1 EAP format? try it
            df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", locale);
            try {
                t = ds.format(df.parse(dateString));
            } catch (final ParseException e2) {
                t = "Invalid";
            }
        }

        return t;
    }

    public static String formatDateTimeFromJiraTimeString(final String dateString, final Locale locale) {
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", locale);
        final DateFormat ds = new SimpleDateFormat("dd/MMM/yy HH:mm");
        String t;
        try {
            t = ds.format(df.parse(dateString));
        } catch (final ParseException e) {
            // maybe it is JIRA 4.1 EAP format? try it
            df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", locale);
            try {
                t = ds.format(df.parse(dateString));
            } catch (final ParseException e2) {
                t = "Invalid";
            }
        }

        return t;
    }

    public static String formatDateFromJiraTimeString(final String dateString, final Locale locale) {
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", locale);
        final DateFormat ds = new SimpleDateFormat("dd/MMM/yy", locale);
        String t;
        try {
            t = ds.format(df.parse(dateString));
        } catch (final ParseException e) {
            // maybe it is JIRA 4.1 EAP format? try it
            df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", locale);
            try {
                t = ds.format(df.parse(dateString));
            } catch (final ParseException e2) {
                df = new SimpleDateFormat("yyyy-MM-dd", locale);
                try {
                    t = ds.format(df.parse(dateString));
                } catch (final ParseException e3) {
                    t = "Invalid";
                }
            }
        }

        return t;
    }

}