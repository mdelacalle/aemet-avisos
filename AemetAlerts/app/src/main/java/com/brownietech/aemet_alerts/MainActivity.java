package com.brownietech.aemet_alerts;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private AlertasRealmAdapter _alertasAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Realm.init(getApplicationContext());
        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

        Realm realm = Realm.getDefaultInstance();
        RealmResults<Alerta> alertas = realm.where(Alerta.class).findAll();

        RecyclerView alertasRecycler = findViewById(R.id.alert_list);
        alertasRecycler.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
        _alertasAdapter = new AlertasRealmAdapter(alertas, true, MainActivity.this);
        alertasRecycler.setAdapter(_alertasAdapter);

        startCheckIfWeHaveSomethingToNotificate();

        findViewById(R.id.add_alert_button).bringToFront();
        findViewById(R.id.add_alert_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddAlertDialog astp = new AddAlertDialog();
                astp.setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_DayNight_NoActionBar);
                astp.show(getSupportFragmentManager(), "Main Activity");
                astp.setAlertasAdapter(_alertasAdapter);
            }
        });
    }

    private void startCheckIfWeHaveSomethingToNotificate() {

        Intent intent = new Intent(getApplicationContext(), AemetNotificationsChecker.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

    }
}
