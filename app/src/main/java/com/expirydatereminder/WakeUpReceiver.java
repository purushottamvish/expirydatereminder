package com.expirydatereminder;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.time.LocalDate;

public class WakeUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseHandler dbh = new DatabaseHandler(context);
        NotificationDatabase ndb = new NotificationDatabase(context);
        int z = 0;
        int notifyDays = 0;

        for (ItemModel a : dbh.getAllItems()) {
            LocalDate ld = LocalDate.of(a.getYear(), a.getMonth(), a.getDate());

            String itemName = a.getItem();
            String days = a.getNotifyDays();
            notifyDays = Integer.parseInt(days.replaceAll("[^0-9]", ""));

            System.out.println("days----->" + notifyDays);
            LocalDate ld2 = ld.minusDays(notifyDays);
            LocalDate today = LocalDate.now();
            if (today.isAfter(ld2) || today.isEqual(ld2)) {
                z++;
              //  notifyDays--;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "edr_channel_1")
                .setSmallIcon(R.drawable.black_logo)
                .setContentTitle("Expiry Date Reminder")
                .setContentText("You have " + z + " items expiring within " + notifyDays + " days!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);

        if (ndb.getCurrentSetting() == 1 && z > 0) {
            notificationManager.notify(notifyDays, builder.build());
        }
    }
}
