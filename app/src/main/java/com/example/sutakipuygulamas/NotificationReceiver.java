package com.example.sutakipuygulamas;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {


    private static final String[] MESSAGES = {
            "Selam! Bir bardak su içmeye ne dersin? 💧",
            "Vücudunun %70'i su, dengeni koru! 🌊",
            "Cildinin parlaması için su vakti! ✨",
            "Odaklanmanı artırmak ister misin? Su iç! 🚀",
            "Küçük bir mola ver ve suyunu yudumla. ☕",
            "Hedefine yaklaşmak üzeresin, sakın pes etme! 🏆",
            "Böbreklerin sana teşekkür edecek. 🩺",
            "Su hayattır, hayatı erteleme. 💙"
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        createNotificationChannel(context);

        SharedPreferences pref = context.getSharedPreferences("WateverData", Context.MODE_PRIVATE);
        int currentIndex = pref.getInt("msg_index", 0); 


        String messageToSend = MESSAGES[currentIndex];
        int nextIndex = currentIndex + 1;
        if (nextIndex >= MESSAGES.length) {
            nextIndex = 0;
        }
        pref.edit().putInt("msg_index", nextIndex).apply();

        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                appIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "water_channel")
                .setSmallIcon(R.drawable.ic_drop_fill)
                .setContentTitle("Su Takip")
                .setContentText(messageToSend)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(1, builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Su Hatırlatıcı";
            String description = "Su içme bildirimleri";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("water_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
