package com.james.playground.temporal.dsl.dto;

import io.temporal.client.schedules.ScheduleCalendarSpec;
import io.temporal.client.schedules.ScheduleRange;
import io.temporal.client.schedules.ScheduleSpec;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.util.streamex.StreamEx;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringSchedule {
  private Frequency frequency;
  private List<Integer> months; // For ANNUALLY frequency, values: 1-12
  private List<Integer> daysOfMonth; // For ANNUALLY & MONTHLY frequency
  private List<DayOfWeek> daysOfWeek; // For WEEKLY frequency
  private String timeOfDay; // For all kinds of frequency, ISO format: 10:15 in UTC+0:00

  public ScheduleSpec toScheduleSpec() {
    return switch (this.frequency) {
      case DAILY -> this.toDailyScheduleSpec();
      case WEEKLY -> this.toWeeklyScheduleSpec();
      case MONTHLY -> this.toMonthlyScheduleSpec();
      case ANNUALLY -> this.toAnnuallyScheduleSpec();
    };
  }

  public ScheduleSpec toAnnuallyScheduleSpec() {
    LocalTime time = LocalTime.parse(this.timeOfDay, DateTimeFormatter.ISO_LOCAL_TIME);

    ScheduleCalendarSpec calendar = ScheduleCalendarSpec.newBuilder()
        .setMonth(
            StreamEx.of(this.months)
                .map(ScheduleRange::new)
                .toList()
        )
        .setDayOfMonth(
            StreamEx.of(this.daysOfMonth)
                .map(ScheduleRange::new)
                .toList()
        )
        .setHour(List.of(new ScheduleRange(time.getHour())))
        .setMinutes(List.of(new ScheduleRange(time.getMinute())))
        .build();

    return ScheduleSpec.newBuilder()
        .setCalendars(List.of(calendar))
        .build();
  }

  public ScheduleSpec toMonthlyScheduleSpec() {
    LocalTime time = LocalTime.parse(this.timeOfDay, DateTimeFormatter.ISO_LOCAL_TIME);

    ScheduleCalendarSpec calendar = ScheduleCalendarSpec.newBuilder()
        .setDayOfMonth(
            StreamEx.of(this.daysOfMonth)
                .map(ScheduleRange::new)
                .toList()
        )
        .setHour(List.of(new ScheduleRange(time.getHour())))
        .setMinutes(List.of(new ScheduleRange(time.getMinute())))
        .build();

    return ScheduleSpec.newBuilder()
        .setCalendars(List.of(calendar))
        .build();
  }

  public ScheduleSpec toWeeklyScheduleSpec() {
    LocalTime time = LocalTime.parse(this.timeOfDay, DateTimeFormatter.ISO_LOCAL_TIME);

    ScheduleCalendarSpec calendar = ScheduleCalendarSpec.newBuilder()
        .setDayOfWeek(
            StreamEx.of(this.daysOfWeek)
                .map(dayOfWeek -> dayOfWeek.getValue() % 7)
                .map(ScheduleRange::new)
                .toList()
        )
        .setHour(List.of(new ScheduleRange(time.getHour())))
        .setMinutes(List.of(new ScheduleRange(time.getMinute())))
        .build();

    return ScheduleSpec.newBuilder()
        .setCalendars(List.of(calendar))
        .build();
  }

  public ScheduleSpec toDailyScheduleSpec() {
    LocalTime time = LocalTime.parse(this.timeOfDay, DateTimeFormatter.ISO_LOCAL_TIME);

    ScheduleCalendarSpec calendar = ScheduleCalendarSpec.newBuilder()
        .setHour(List.of(new ScheduleRange(time.getHour())))
        .setMinutes(List.of(new ScheduleRange(time.getMinute())))
        .build();

    return ScheduleSpec.newBuilder()
        .setCalendars(List.of(calendar))
        .build();
  }

  public enum Frequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    ANNUALLY
  }
}
