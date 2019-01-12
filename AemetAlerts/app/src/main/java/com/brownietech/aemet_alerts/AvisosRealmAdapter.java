package com.brownietech.aemet_alerts;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmList;
import io.realm.RealmRecyclerViewAdapter;

public class AvisosRealmAdapter extends RealmRecyclerViewAdapter<Aviso,AvisosRealmAdapter.ViewHolder> {

    private Context mContext;
    public AvisosRealmAdapter(@Nullable OrderedRealmCollection<Aviso> data, boolean autoUpdate) {
        super(data, autoUpdate);
    }


    public AvisosRealmAdapter(@Nullable OrderedRealmCollection<Aviso> data,boolean autoUpdate,Context context) {
        super(data, autoUpdate);
        this.mContext =  context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(mContext)
                .inflate(R.layout.avisos_item, parent, false);
        ViewHolder vh = new ViewHolder(layout);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(position);
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(int position) {
            Aviso aviso = getItem(position);

            itemView.findViewById(R.id.parent).setBackgroundColor(AemetAPIConstants.getBackgroundColor(aviso.getNivel()));

            ((TextView)itemView.findViewById(R.id.event)).setText(""+aviso.getEvent());
            ((TextView)itemView.findViewById(R.id.urgency)).setText(""+AemetAPIConstants.getUrgencyString(aviso.getUrgency()));
            ((TextView)itemView.findViewById(R.id.description)).setText(""+aviso.getDescription());

            RealmList<Area> areas = aviso.getAreas();
            String areasSt = "";
            for(Area area:areas){
                areasSt = areasSt +" "+ area.getAreaDesc();
            }
            
            ((TextView)itemView.findViewById(R.id.areas)).setText(areasSt.trim());
            ((TextView)itemView.findViewById(R.id.from)).setText(""+aviso.getOnset());
            ((TextView)itemView.findViewById(R.id.expire)).setText(""+aviso.getExpires());

            ((TextView)itemView.findViewById(R.id.certainty)).setText(""+AemetAPIConstants.getCertaintyString(aviso.getCertainty()));



            ((TextView)itemView.findViewById(R.id.instructions)).setText(""+aviso.getInstruction());
        }
    }
}
