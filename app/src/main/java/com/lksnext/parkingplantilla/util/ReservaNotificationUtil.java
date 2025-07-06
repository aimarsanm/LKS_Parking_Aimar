package com.lksnext.parkingplantilla.util;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.lksnext.parkingplantilla.R;
import com.lksnext.parkingplantilla.domain.Reserva;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ReservaNotificationUtil {
    public static final String CHANNEL_ID = "reserva_channel";
    public static final int NOTIF_START = 1001;
    public static final int NOTIF_END = 1002;

    public static void scheduleNotifications(Context context, Reserva reserva) {
        // Notificación 30 min antes de inicio
        scheduleNotification(context, reserva, true);
        // Notificación 15 min antes de fin
        scheduleNotification(context, reserva, false);
    }

    private static void scheduleNotification(Context context, Reserva reserva, boolean beforeStart) {
        String fecha = reserva.getFecha();
        int hora = reserva.getHoraInicio().getHoraInicio();
        int minuto = reserva.getHoraInicio().getMinuto();
        int horaFin = reserva.getHoraInicio().getHoraFin();
        int minutoFin = reserva.getHoraInicio().getMinutoFin();
        LocalDate date = LocalDate.parse(fecha);
        LocalTime time = beforeStart ? LocalTime.of(hora, minuto) : LocalTime.of(horaFin, minutoFin);
        time = beforeStart ? time.minusMinutes(30) : time.minusMinutes(15);
        // Usar ZonedDateTime para obtener el tiempo en milisegundos correctamente en Android
        long triggerAtMillis = java.time.ZonedDateTime.of(date, time, java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        Intent intent = new Intent(context, ReservaNotificationReceiver.class);
        intent.putExtra("reserva_id", reserva.getId());
        intent.putExtra("before_start", beforeStart);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            beforeStart ? NOTIF_START : NOTIF_END,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Reservas";
            String description = "Notificaciones de reservas";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static class ReservaNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean beforeStart = intent.getBooleanExtra("before_start", true);
            String title = beforeStart ? "Tu reserva comienza pronto" : "Tu reserva termina pronto";
            String message = beforeStart ? "Quedan 30 minutos para tu reserva." : "Quedan 15 minutos para que termine tu reserva.";
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(beforeStart ? NOTIF_START : NOTIF_END, builder.build());
        }
    }
}
