package com.james.playground.parsing;

import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Test {
  public static void main(String[] args) {
    String dateTime = "2024-06-19T20:49:00";
    System.out.println(ZonedDateTime.parse(dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME));

    String timeOfDay = "10:15:30";
    System.out.println(OffsetTime.parse(timeOfDay, DateTimeFormatter.ISO_OFFSET_TIME));
  }
}
