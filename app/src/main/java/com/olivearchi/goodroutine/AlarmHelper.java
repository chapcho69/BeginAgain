package com.olivearchi.goodroutine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmHelper {

    public static void scheduleAlarm(Context context, TodoItem item) {
        if (item.isDone()) return;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date startDate = sdf.parse(item.getStartDateTime());
            if (startDate == null) return;

            long alarmTime = startDate.getTime() - (60 * 60 * 1000); // 1 hour before

            // If time already passed, and it's repeating, find the next occurrence
            if (alarmTime <= System.currentTimeMillis() && item.isRepeating()) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(startDate);
                while (cal.getTimeInMillis() - (60 * 60 * 1000) <= System.currentTimeMillis()) {
                    switch (item.getRepeatType()) {
                        case TodoItem.REPEAT_DAY: cal.add(Calendar.DAY_OF_YEAR, 1); break;
                        case TodoItem.REPEAT_WEEK: cal.add(Calendar.WEEK_OF_YEAR, 1); break;
                        case TodoItem.REPEAT_MONTH: cal.add(Calendar.MONTH, 1); break;
                        case TodoItem.REPEAT_YEAR: cal.add(Calendar.YEAR, 1); break;
                        default: return; // Should not happen if isRepeating is true
                    }
                }
                alarmTime = cal.getTimeInMillis() - (60 * 60 * 1000);
            } else if (alarmTime <= System.currentTimeMillis()) {
                return;
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra("id", item.getId());
            intent.putExtra("subject", item.getSubject());
            intent.putExtra("detail", item.getDetail());
            intent.putExtra("time", item.getStartDateTime());
            intent.putExtra("repeatType", item.getRepeatType());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 
                    (int) item.getId(), 
                    intent, 
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                    } else {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                }
            }
        } catch (Exception e) {
            Log.e("AlarmHelper", "Error scheduling alarm", e);
        }
    }

    public static void cancelAlarm(Context context, TodoItem item) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 
                (int) item.getId(), 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
