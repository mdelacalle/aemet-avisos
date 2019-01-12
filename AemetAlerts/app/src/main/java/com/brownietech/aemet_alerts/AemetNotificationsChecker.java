package com.brownietech.aemet_alerts;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmResults;

public class AemetNotificationsChecker extends BroadcastReceiver implements AemetAPIListener {

    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        AemetAPI.retrieveAllAvisos(AemetNotificationsChecker.this);
        mContext = context;
        Log.d("***", "---> CHECKING AVISOS FROM AEMET API");
    }

    @Override
    public void onSuccess() {

        Realm realm = Realm.getDefaultInstance();
        RealmResults<Alerta> alertas = realm.where(Alerta.class).findAll();
        Long numAvisos = 0L;
        String zonas = "";
        String maxLevel = "";


        for (Alerta alerta : alertas) {
            if (alerta.isActiva()) {
                String nivelAviso = alerta.getNivelAviso();
                String severity = "Minor";
                switch (nivelAviso) {
                    case "Amarillo":
                        if(!maxLevel.equals("Severe") && !maxLevel.equals("Extreme")) {
                            severity = "Moderate";
                            maxLevel = severity;
                        }
                        break;
                    case "Naranja":
                        if(!maxLevel.equals("Extreme")) {
                            severity = "Severe";
                            maxLevel = severity;
                        }
                        break;
                    case "Roja":
                        severity = "Extreme";
                        maxLevel = severity;
                        break;
                }
                long numAvisosAlert = realm.where(Aviso.class).equalTo("zona", alerta.getAreaAviso()).equalTo("severity", severity).count();


                if (numAvisosAlert > 0) {
                    String areaByCode = AemetAPIConstants.getAreaByCode(alerta.getAreaAviso());
                    if (!zonas.startsWith(", ")) {
                        zonas = zonas + ", " + areaByCode;
                    } else {
                        zonas = areaByCode;
                    }
                }

                numAvisos = numAvisos + numAvisosAlert;
            }
        }

        if (numAvisos > 0) {

            Intent notificationIntent = new Intent(mContext, MainActivity.class);

            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent intent = PendingIntent.getActivity(mContext, 0,
                    notificationIntent, 0);


            NotificationChannel mChannel = null;

            NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            // The id of the channel.
            String id = "my_channel_01";
            // The user-visible name of the channel.
            CharSequence name = "AEMET";
            // The user-visible description of the channel.
            String description = "AEMET Alerts";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            mChannel = new NotificationChannel(id, name, importance);

            // Configure the notification channel.
            mChannel.setDescription(description);

            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);

            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});


            mNotificationManager.createNotificationChannel(mChannel);


            String contextText = mContext.getString(R.string.message_notification) + " " + zonas;

            Bitmap bitmap = null;
            if(maxLevel.equals("Moderate")){
                 bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.orange_icon);
            }
            if(maxLevel.equals("Severe")){
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.yellow_icon);
            }
            if(maxLevel.equals("Extreme")){
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.red_icon);
            }


            Notification notification = new NotificationCompat.Builder(mContext, mChannel.getId())
                    .setSmallIcon(R.drawable.aemet_alerts)
                    .setContentTitle(mContext.getResources().getString(R.string.app_name))
                    .setContentText(contextText)
                    .setLargeIcon(bitmap)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(intent)
                    .build();

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);

// notificationId is a unique int for each notification that you must define
            notificationManager.notify(0, notification);
            Log.e("***", contextText);
        }


    }

    @Override
    public void onError() {

    }


}
