package com.aengdulab.trenditem;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Fixture {
    public static LocalDateTime NOW = LocalDateTime.of(2024, 11, 14, 12, 0, 0);

    public static LocalDateTime MINUS_ONE_HOUR = NOW.minusHours(1);
    public static LocalDateTime MINUS_TWO_HOURS = NOW.minusHours(2);
    public static LocalDateTime MINUS_THREE_HOURS = NOW.minusHours(3);
    public static LocalDateTime MINUS_FOUR_HOURS = NOW.minusHours(4);
    public static LocalDateTime MINUS_FIVE_HOURS = NOW.minusHours(5);

    public static Instant NOW_INSTANT = NOW.atZone(ZoneId.systemDefault()).toInstant();
    public static Clock FIXED_CLOCK = Clock.fixed(NOW_INSTANT, ZoneId.systemDefault());
}
