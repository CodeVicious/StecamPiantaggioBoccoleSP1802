package stecamSP1802.controllers;

import javafx.beans.property.SimpleStringProperty;

public class RicettaDett {
    SimpleStringProperty idDett;
    SimpleStringProperty codiceDett;
    SimpleStringProperty descrizioneDett;
    SimpleStringProperty fkRicettaDett;

    RicettaDett(String idDett, String codiceDett, String descrizioneDett, String fkRicetta) {
        this.idDett = new SimpleStringProperty(idDett);
        this.codiceDett = new SimpleStringProperty(codiceDett);
        this.descrizioneDett = new SimpleStringProperty(descrizioneDett);
        this.fkRicettaDett = new SimpleStringProperty(fkRicetta);
    }

    public String getIdDett() {
        return idDett.get();
    }

    public void setIdDett(String idDett) {
        this.idDett.set(idDett);
    }

    public String getCodiceDett() {
        return codiceDett.get();
    }

    public void setCodiceDett(String codiceDett) {
        this.codiceDett.set(codiceDett);
    }

    public String getDescrizioneDett() {
        return descrizioneDett.get();
    }

    public void setDescrizioneDett(String descrizioneDett) {
        this.descrizioneDett.set(descrizioneDett);
    }

    public String getFkRicettaDett() {
        return fkRicettaDett.get();
    }

    public void setFkRicettaDett(String fkRicettaDett) {
        this.fkRicettaDett.set(fkRicettaDett);
    }
}