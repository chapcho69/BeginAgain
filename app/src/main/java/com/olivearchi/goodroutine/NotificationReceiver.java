package com.olivearchi.goodroutine;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "todo_notification_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra("id", 0);
        String subject = intent.getStringExtra("subject");
        String detail = intent.getStringExtra("detail");
        String time = intent.getStringExtra("time");
        int repeatType = intent.getIntExtra("repeatType", 0);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // ... (rest of channel logic)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Todo Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        Intent mainIntent = new Intent(context, ConfirmationActivity.class);
        mainIntent.putExtra("id", id);
        mainIntent.putExtra("subject", subject);
        mainIntent.putExtra("detail", detail);
        mainIntent.putExtra("time", time);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) id, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("[" + context.getString(R.string.app_name) + "] 할 일 확인")
                .setContentText(subject + "를 수행하셨나요?")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) id, builder.build());

        if (repeatType > 0 && time != null) {
            rescheduleNext(context, id, subject, detail, repeatType, time);
        }
    }

    private void rescheduleNext(Context context, long id, String subject, String detail, int repeatType, String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date startDate = sdf.parse(time);
            if (startDate == null) return;

            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);

            switch (repeatType) {
                case TodoItem.REPEAT_DAY: cal.add(Calendar.DAY_OF_YEAR, 1); break;
                case TodoItem.REPEAT_WEEK: cal.add(Calendar.WEEK_OF_YEAR, 1); break;
                case TodoItem.REPEAT_MONTH: cal.add(Calendar.MONTH, 1); break;
                case TodoItem.REPEAT_YEAR: cal.add(Calendar.YEAR, 1); break;
            }

            String nextTime = sdf.format(cal.getTime());
            long nextAlarmTime = cal.getTimeInMillis() - (60 * 60 * 1000);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent nextIntent = new Intent(context, NotificationReceiver.class);
            nextIntent.putExtra("id", id);
            nextIntent.putExtra("subject", subject);
            nextIntent.putExtra("detail", detail);
            nextIntent.putExtra("time", nextTime);
            nextIntent.putExtra("repeatType", repeatType);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 
                    (int) id, 
                    nextIntent, 
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmTime, pendingIntent);
            }
        } catch (Exception e) {
            Log.e("NotificationReceiver", "Error rescheduling alarm", e);
        }
    }
}
