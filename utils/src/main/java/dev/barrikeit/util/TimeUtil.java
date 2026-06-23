package dev.barrikeit.util;

import dev.barrikeit.exception.UnexpectedException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public final class TimeUtil {

  public static final String PATTERN_DATE = "yyyy-MM-dd";
  public static final String PATTERN_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ssXXX";
  public static final String PATTERN_DATE_TIME_MILLI = "yyyy-MM-dd HH:mm:ss.SSS";
  public static final String PATTERN_DATE_DOWNLOAD = "yyyyMMdd";
  public static final String PATTERN_DATE_TIME_DOWNLOAD = "yyyyMMdd_HHmmss";

  private static ZoneId ZONE = ZoneId.of(System.getProperty("user.timezone", "UTC"));

  private TimeUtil() {
    throw new IllegalStateException("TimeUtil class");
  }

  public static void setZone(String zone) {
    ZONE = ZoneId.of(zone);
  }

  public static void setZone(ZoneId zone) {
    ZONE = zone;
  }

  public static ZoneId getZone() {
    return ZONE;
  }

  public static Instant instantNow() {
    return Instant.now().atZone(ZONE).toInstant();
  }

  public static Date dateNow() {
    return Date.from(instantNow());
  }

  public static OffsetDateTime offsetDateTimeNow() {
    return OffsetDateTime.now(ZONE);
  }

  public static LocalDateTime localDateTimeNow() {
    return LocalDateTime.now(ZONE);
  }

  public static LocalDate localDateNow() {
    return LocalDate.now(ZONE);
  }

  public static OffsetDateTime toOffsetDateTime(Date date) {
    if (date == null) return null;
    return date.toInstant().atZone(ZONE).toOffsetDateTime();
  }

  public static OffsetDateTime toOffsetDateTime(Timestamp timestamp) {
    if (timestamp == null) return null;
    return timestamp.toInstant().atZone(ZONE).toOffsetDateTime();
  }

  public static OffsetDateTime toOffsetDateTime(LocalDate date) {
    if (date == null) return null;
    return date.atStartOfDay(ZONE).toOffsetDateTime();
  }

  public static OffsetDateTime toOffsetDateTime(LocalDateTime dateTime) {
    if (dateTime == null) return null;
    return dateTime.atZone(ZONE).toOffsetDateTime();
  }

  public static LocalDateTime toLocalDateTime(OffsetDateTime odt) {
    if (odt == null) return null;
    return odt.toLocalDateTime();
  }

  public static LocalDate toLocalDate(OffsetDateTime odt) {
    if (odt == null) return null;
    return odt.toLocalDate();
  }

  public static OffsetDateTime parseOffsetDateTime(String value) {
    if (value == null || value.isBlank()) return null;
    DateTimeFormatter dateTimeFormat =
        DateTimeFormatter.ofPattern(PATTERN_DATE_TIME).withZone(ZONE);
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(PATTERN_DATE);
    try {
      return OffsetDateTime.parse(value, dateTimeFormat);
    } catch (DateTimeParseException e) {
      try {
        return LocalDate.parse(value, dateFormat).atStartOfDay(ZONE).toOffsetDateTime();
      } catch (DateTimeParseException ex) {
        throw new UnexpectedException("Invalid date/time format: %s", value);
      }
    }
  }

  public static LocalDate parseLocalDate(String value) {
    if (value == null || value.isBlank()) return null;
    try {
      return LocalDate.parse(value, DateTimeFormatter.ofPattern(PATTERN_DATE));
    } catch (DateTimeParseException e) {
      throw new UnexpectedException("Invalid date format: %s", value);
    }
  }

  public static String formatDate(OffsetDateTime date) {
    if (date == null) return null;
    return date.format(DateTimeFormatter.ofPattern(PATTERN_DATE));
  }

  public static String formatDateTime(OffsetDateTime date) {
    if (date == null) return null;
    return date.format(DateTimeFormatter.ofPattern(PATTERN_DATE_TIME_MILLI));
  }

  public static String formatDateDownload(OffsetDateTime date) {
    if (date == null) return null;
    return date.format(DateTimeFormatter.ofPattern(PATTERN_DATE_DOWNLOAD));
  }

  public static String formatDateTimeDownload(OffsetDateTime date) {
    if (date == null) return null;
    return date.format(DateTimeFormatter.ofPattern(PATTERN_DATE_TIME_DOWNLOAD));
  }

  public static String format(OffsetDateTime date, String pattern) {
    if (date == null) return null;
    return date.format(DateTimeFormatter.ofPattern(pattern));
  }

  public static String format(LocalDate date, String pattern) {
    if (date == null) return null;
    return date.format(DateTimeFormatter.ofPattern(pattern));
  }
}
