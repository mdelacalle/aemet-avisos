package com.brownietech.aemet_alerts;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Alerta extends RealmObject {

    boolean activa;
    String nivelAviso;
    String frecuencia;
    String areaAviso;
    RealmList<Alerta> alertas;

    String alertId;




    public String getNivelAviso() {
        return nivelAviso;
    }

    public void setNivelAviso(String nivelAviso) {
        this.nivelAviso = nivelAviso;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }

    public RealmList<Alerta> getAlertas() {
        return alertas;
    }

    public void setAlertas(RealmList<Alerta> alertas) {
        this.alertas = alertas;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public String getAreaAviso() {
        return areaAviso;
    }

    public void setAreaAviso(String areaAviso) {
        this.areaAviso = areaAviso;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }
}
