package com.brownietech.aemet_alerts;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

public class AlertasRealmAdapter extends RealmRecyclerViewAdapter<Alerta,AlertasRealmAdapter.ViewHolder> {

    private FragmentActivity mParentActivity;

    public AlertasRealmAdapter(@Nullable OrderedRealmCollection<Alerta> data, boolean autoUpdate) {
        super(data, autoUpdate);
    }


    public AlertasRealmAdapter(@Nullable OrderedRealmCollection<Alerta> data, boolean autoUpdate, FragmentActivity activity) {
        super(data, autoUpdate);
        mParentActivity = activity;
    }

    @NonNull
    @Override
    public AlertasRealmAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(mParentActivity)
                .inflate(R.layout.alert_item, parent, false);
        AlertasRealmAdapter.ViewHolder vh = new AlertasRealmAdapter.ViewHolder(layout);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull AlertasRealmAdapter.ViewHolder viewHolder, int position) {
        viewHolder.bind(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(int position) {

            Alerta alerta = getItem(position);
            TextView alertLevel = itemView.findViewById(R.id.alert);
            TextView alertWhere = itemView.findViewById(R.id.where);
            TextView alertWhen = itemView.findViewById(R.id.when);



            String nivelAviso = alerta.getNivelAviso();
            itemView.setBackgroundColor(AemetAPIConstants.getBackgroundColor(nivelAviso));
            alertLevel.setText(nivelAviso);
            alertWhere.setText(AemetAPIConstants.getAreaByCode(alerta.getAreaAviso()));
            alertWhen.setText(alerta.getFrecuencia());


            ((Switch)itemView.findViewById(R.id.activo)).setChecked(alerta.isActiva());
            ((Switch)itemView.findViewById(R.id.activo)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    boolean active = true;
                    if (b){
                        active =true;
                    }else{
                        active = false;
                    }
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    realm.where(Alerta.class).equalTo("alertId",alerta.getAlertId()).findFirst().setActiva(active);
                    realm.commitTransaction();
                    realm.refresh();
                    realm.close();

                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SeeAvisosDialog astp = new SeeAvisosDialog();
                    astp.setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_DayNight_NoActionBar);
                    astp.show(mParentActivity.getSupportFragmentManager(), "Main Activity");
                    astp.setAlerta(alerta);
                }
            });

        }
    }
}
