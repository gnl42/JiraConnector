package com.atlassian.jira.rest.client.internal.json;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Date;

public class Dates {

    private static final DateTimeFormatter ISO_DATE_TIME_FORMAT = ISODateTimeFormat.dateTime();

    /**
     * Converts the given DateTime object to ISO String.
     */
    public static String asISODateTimeString(@Nullable DateTime dateTime)
    {
        return ISO_DATE_TIME_FORMAT.print(dateTime);
    }
}
