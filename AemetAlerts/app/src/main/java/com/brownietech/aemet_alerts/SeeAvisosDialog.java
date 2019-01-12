package com.brownietech.aemet_alerts;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmResults;

public class SeeAvisosDialog extends DialogFragment implements AemetAPIListener {


    private Alerta mAlerta;
    private ConstraintLayout _container;
    String mAlertaId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup containerVg, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, containerVg, savedInstanceState);
        _container = (ConstraintLayout) inflater.inflate(R.layout.see_avisos_dialog, null);

        String lastAvisos = getString(R.string.last_avisos);
        String fromLevel = getString(R.string.from_level);
        ((TextView) _container.findViewById(R.id.last_avisos_title)).setText(lastAvisos + " " + AemetAPIConstants.getAreaByCode(mAlerta.areaAviso) + " " + fromLevel + " " + mAlerta.getNivelAviso() );
        mAlertaId = mAlerta.getAlertId();

        AemetAPI.retrieveAvisosFromArea(SeeAvisosDialog.this, mAlerta.areaAviso);

        return _container;
    }

    public void setAlerta(Alerta alerta){
        mAlerta = alerta;
    }

    @Override
    public void onSuccess() {


        Realm realm = Realm.getDefaultInstance();

        Alerta alerta = realm.where(Alerta.class).equalTo("alertId",mAlertaId).findFirst();

        String zona = alerta.getAreaAviso();
        String nivelAviso = alerta.getNivelAviso();
        String severity = "Minor";
        switch (nivelAviso){
            case "Amarillo":
                severity = "Moderate";
                break;
            case "Naranja":
                severity = "Severe";
                break;
            case "Roja":
                severity = "Extreme";
                break;
        }


        Long numAvisos = realm.where(Aviso.class).equalTo("zona", zona).equalTo("severity", severity).count();


        if(numAvisos==0){
           getActivity().runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   _container.findViewById(R.id.progressBar).setVisibility(View.GONE);
                   ((TextView)_container.findViewById(R.id.msj)).setText(R.string.no_hay_avisos_activos_en_esta_zona_con_el_nivel_seleccionado);
               }
           });

        }else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _container.findViewById(R.id.progress).setVisibility(View.GONE);
                    _container.findViewById(R.id.avisos_list).setVisibility(View.VISIBLE);

                    Realm realm = Realm.getDefaultInstance();

                    RecyclerView avisosRV = _container.findViewById(R.id.avisos_list);
                    avisosRV.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                   if(nivelAviso.equals("Amarillo")) {
                       final RealmResults<Aviso> amarillas = realm.where(Aviso.class).beginGroup().equalTo("severity", "Moderate").or().equalTo("severity", "Severe").or().equalTo("severity", "Extreme").endGroup().and().equalTo("zona",zona).findAll();

                       for(Aviso aviso:amarillas){
                           Log.e("###", "ZONA:"+aviso.getZona());
                       }

                       AvisosRealmAdapter avisosRealmAdapter = new AvisosRealmAdapter(amarillas,true, getContext());
                       avisosRV.setAdapter(avisosRealmAdapter);
                   }
                    if(nivelAviso.equals("Naranja")) {
                        final  RealmResults<Aviso> naranjas = realm.where(Aviso.class).beginGroup().equalTo("severity", "Severe").or().equalTo("severity", "Extreme").endGroup().and().equalTo("zona",zona).findAll();
                        AvisosRealmAdapter avisosRealmAdapter = new AvisosRealmAdapter(naranjas,true, getContext());
                        avisosRV.setAdapter(avisosRealmAdapter);
                    }
                    if(nivelAviso.equals("Rojo")) {
                        final RealmResults<Aviso> rojas = realm.where(Aviso.class).equalTo("severity", "Extreme").and().equalTo("zona",zona).findAll();
                        AvisosRealmAdapter avisosRealmAdapter = new AvisosRealmAdapter(rojas,true, getContext());
                        avisosRV.setAdapter(avisosRealmAdapter);
                    }

                    realm.close();
                }
            });

        }

        realm.close();

    }

    @Override
    public void onError() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.error_retrieving_data)
                .setTitle(R.string.erro_dialog_title);
        AlertDialog dialog = builder.create();
        dialog.show();

        dismiss();
    }
}
