package com.learninge.app.ui.common;

import android.content.Context;

import com.learninge.app.LearningEApp;
import com.learninge.app.R;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Thời điểm cho người dùng đọc: "20:41 hôm nay" hoặc "20:41 09/11".
 */
public final class TimeText {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter TIME_DAY = DateTimeFormatter.ofPattern("HH:mm dd/MM");

    private TimeText() {
    }

    public static String clock(Context context, long epochMillis) {
        Clock clock = LearningEApp.services(context).clock();
        ZonedDateTime time = Instant.ofEpochMilli(epochMillis).atZone(clock.getZone());
        return time.toLocalDate().equals(LocalDate.now(clock))
                ? context.getString(R.string.time_today, TIME.format(time)) : TIME_DAY.format(time);
    }

    /**
     * "hôm nay" hoặc "09/11".
     */
    public static String day(Context context, long epochMillis) {
        Clock clock = LearningEApp.services(context).clock();
        LocalDate day = Instant.ofEpochMilli(epochMillis).atZone(clock.getZone()).toLocalDate();
        return day.equals(LocalDate.now(clock)) ? context.getString(R.string.today) : DAY.format(day);
    }
}
