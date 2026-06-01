package io.dkakunsi.bitapp.common;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public interface DateTimeConverter {

  ZoneId UTC_ZONE = ZoneId.of("UTC");

  int MINUTES_IN_HOUR = 60;

  default LocalDate parseDate(Long epochMilli) {
    if (epochMilli == null) {
      return Instant.now().atZone(UTC_ZONE).toLocalDate();
    }

    try {
      return Instant.ofEpochMilli(epochMilli).atZone(UTC_ZONE).toLocalDate();
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  default LocalTime parseTime(Integer timeSinceMidnight) {
    if (timeSinceMidnight == null) {
      return Instant.now().atZone(UTC_ZONE).toLocalTime();
    }

    var hour = timeSinceMidnight / MINUTES_IN_HOUR;
    var minute = timeSinceMidnight % MINUTES_IN_HOUR;
    try {
      return LocalTime.of(hour, minute);
    } catch (DateTimeException e) {
      throw new IllegalArgumentException(e);
    }
  }

  default Long toEpochMilli(LocalDate date) {
    return epochMilli(date);
  }

  default Integer toMinutesSinceMidnight(LocalTime time) {
    return minutesSinceMidnight(time);
  }

  static Long epochMilli(LocalDate date) {
    return date.atStartOfDay(UTC_ZONE).toInstant().toEpochMilli();
  }

  static Integer minutesSinceMidnight(LocalTime time) {
    return time.getHour() * MINUTES_IN_HOUR + time.getMinute();
  }
}
