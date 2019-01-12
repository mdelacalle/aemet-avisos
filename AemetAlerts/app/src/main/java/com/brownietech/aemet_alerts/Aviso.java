package com.brownietech.aemet_alerts;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Aviso extends RealmObject {
    String sent;
    String msgType;
    String urgency;
    String severity;
    String certainty;
    String fenomeno;
    String nivel;
    String parametro;
    String effective;
    String onset;
    String expires;
    String headline;
    String event;
    String description;
    String instruction;
    String zona;
    RealmList<Area> areas;


    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getCertainty() {
        return certainty;
    }

    public void setCertainty(String certainty) {
        this.certainty = certainty;
    }

    public String getEffective() {
        return effective;
    }

    public void setEffective(String effective) {
        this.effective = effective;
    }

    public String getOnset() {
        return onset;
    }

    public void setOnset(String onset) {
        this.onset = onset;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public RealmList<Area> getAreas() {
        return areas;
    }

    public void setAreas(RealmList<Area> areas) {
        this.areas = areas;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getFenomeno() {
        return fenomeno;
    }

    public void setFenomeno(String fenomeno) {
        this.fenomeno = fenomeno;
    }

    public String getParametro() {
        return parametro;
    }

    public void setParametro(String parametro) {
        this.parametro = parametro;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    @Override
    public String toString() {
        return "Aviso{" +
                "sent='" + sent + '\'' +
                ", msgType='" + msgType + '\'' +
                ", urgency='" + urgency + '\'' +
                ", severity='" + severity + '\'' +
                ", certainty='" + certainty + '\'' +
                ", fenomeno='" + fenomeno + '\'' +
                ", nivel='" + nivel + '\'' +
                ", parametro='" + parametro + '\'' +
                ", effective='" + effective + '\'' +
                ", onset='" + onset + '\'' +
                ", expires='" + expires + '\'' +
                ", headline='" + headline + '\'' +
                ", event='" + event + '\'' +
                ", description='" + description + '\'' +
                ", instruction='" + instruction + '\'' +
                ", zona='" + zona + '\'' +
                ", areas=" + areas +
                '}';
    }
}
