package io.dkakunsi.bitapp;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public interface DateTimeConverter {

  ZoneId UTC_ZONE = ZoneId.of("UTC");

  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  int MINUTES_IN_HOUR = 60;

  static ZonedDateTime nowAtUTC() {
    return Instant.now().atZone(UTC_ZONE);
  }

  default Long toEpochMilli(String datetime) {
    if (datetime == null) {
      return toEpochMilli(nowAtUTC().toLocalDate());
    }
    try {
      var localDateTime = LocalDateTime.parse(datetime, formatter);
      return toEpochMilli(localDateTime.toLocalDate());
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  default Long toEpochMilli(LocalDate date) {
    return epochMilli(date);
  }

  static Long epochMilli(LocalDate date) {
    return date.atStartOfDay(UTC_ZONE).toInstant().toEpochMilli();
  }

  default Integer toMinutesSinceMidnight(String datetime) {
    if (datetime == null) {
      var now = nowAtUTC().toLocalTime();
      return minutesSinceMidnight(now);
    }
    try {
      var localDateTime = LocalDateTime.parse(datetime, formatter);
      var localTime = localDateTime.toLocalTime();
      return minutesSinceMidnight(localTime);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  default Integer toMinutesSinceMidnight(LocalTime time) {
    return minutesSinceMidnight(time);
  }

  static Integer minutesSinceMidnight(LocalTime time) {
    return time.getHour() * MINUTES_IN_HOUR + time.getMinute();
  }

  default LocalDate parseDate(Long epochMilli) {
    if (epochMilli == null) {
      return nowAtUTC().toLocalDate();
    }

    try {
      return Instant.ofEpochMilli(epochMilli).atZone(UTC_ZONE).toLocalDate();
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  default LocalTime parseTime(Integer timeSinceMidnight) {
    if (timeSinceMidnight == null) {
      return nowAtUTC().toLocalTime();
    }

    var hour = timeSinceMidnight / MINUTES_IN_HOUR;
    var minute = timeSinceMidnight % MINUTES_IN_HOUR;
    try {
      return LocalTime.of(hour, minute);
    } catch (DateTimeException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
