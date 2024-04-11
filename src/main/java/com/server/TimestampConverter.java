package com.server;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
* Provides different conversion methods for dates.
*/
public class TimestampConverter {

    private TimestampConverter() {}

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyy-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneOffset.UTC);

    /**
    * Converts the given date to ZonedDateTime from Unix Time.
    * @param dateAsEpoch the posting date as Epoch (Unix Time).
    * @return the date as ZonedDateTime.
    */
    public static ZonedDateTime convertToZonedDateTime(long dateAsEpoch) {
        ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(dateAsEpoch), ZoneOffset.UTC);
        return date;
    }

    /**
    * Converts the given date to ZonedDateTime from a String.
    * @param dateAsString the date as a String. It should follow this format: yyyy-MM-dd'T'HH:mm:ss.SSSX
    * @return the date as ZonedDateTime.
    */
    public static ZonedDateTime convertToZonedDateTime(String dateAsString) {
        ZonedDateTime date = ZonedDateTime.parse(dateAsString, formatter);
        return date;
    }

    /**
    * Converts the given date to long from ZonedDateTime.
    * @param date the ZonedDateTime.
    * @return the date as Epoch long. Example: 1708506099097
    */
    public static  long convertToLong(ZonedDateTime date) {
        return date.toInstant().toEpochMilli();
    }

    /**
    * Converts the given date to String from ZonedDateTime.
    * @param date the ZonedDateTime.
    * @return the date as String. Example: "2020-12-21T07:57:47.123Z"
    */
    public static String convertToString(ZonedDateTime date) {
        return date.format(formatter);
    }

    /**
    * Converts the given date to String from long (Epoch).
    * @param dateAsEpoch the date as Epoch (Unix Time).
    * @return the date as String. Example: "2020-12-21T07:57:47.123Z"
    */
    public static String convertToString(long dateAsEpoch) {
        ZonedDateTime date = convertToZonedDateTime(dateAsEpoch);
        return date.format(formatter);
    }
}
