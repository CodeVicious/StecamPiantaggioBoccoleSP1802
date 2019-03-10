package stecamSP1802.controllers;

import javafx.beans.property.SimpleStringProperty;

public class RicettaDett {
    SimpleStringProperty idDett;
    SimpleStringProperty codiceDett;
    SimpleStringProperty descrizioneDett;

    RicettaDett(String idDett, String codiceDett, String descrizioneDett) {
        this.idDett = new SimpleStringProperty(idDett);
        this.codiceDett = new SimpleStringProperty(codiceDett);
        this.descrizioneDett = new SimpleStringProperty(descrizioneDett);
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
}