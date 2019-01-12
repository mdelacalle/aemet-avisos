package com.brownietech.aemet_alerts;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import java.util.UUID;

import io.realm.Realm;

public class AddAlertDialog extends DialogFragment {

    private AlertasRealmAdapter _alertasAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup containerVg, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, containerVg, savedInstanceState);
        ConstraintLayout container = (ConstraintLayout) inflater.inflate(R.layout.new_aviso_dialog, null);

        Spinner levelSpinner = container.findViewById(R.id.level_spinner);
        Spinner whereSpinner = container.findViewById(R.id.where_spinner);
      //  Spinner whenSpinner = container.findViewById(R.id.when_spinner);

        container.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String level = levelSpinner.getSelectedItem().toString();
                String where = AemetAPIConstants.getCodeByProvince(whereSpinner.getSelectedItem().toString());
           //     String when = whenSpinner.getSelectedItem().toString();

                if(levelSpinner.getSelectedItemId()==0 || whereSpinner.getSelectedItemId()==0/*||whenSpinner.getSelectedItemId()==0*/){

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.error_creating_alert_message)
                            .setTitle(R.string.erro_dialog_title);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }else{

                    Realm realm = Realm.getDefaultInstance();
                    Alerta alerta = new Alerta();

                    alerta.setAlertId(UUID.randomUUID().toString());
                    alerta.setActiva(true);
                    alerta.setAreaAviso(where);
                  //  alerta.setFrecuencia(when);
                    alerta.setNivelAviso(level);
                    realm.beginTransaction();
                    realm.copyToRealm(alerta);
                    realm.commitTransaction();
                    Log.e("***", "We must create the alert because everything is OK");

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.ok_creating_alert_message)
                            .setTitle(R.string.ok_dialog_title);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    _alertasAdapter.notifyDataSetChanged();

                    dismiss();
                }
            }
        });


        return container;
    }

    public void setAlertasAdapter(AlertasRealmAdapter alertasAdapter) {
        _alertasAdapter = alertasAdapter;
    }
}
