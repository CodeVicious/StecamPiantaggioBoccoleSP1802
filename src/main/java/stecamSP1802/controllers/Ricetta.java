package stecamSP1802.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;

public class Ricetta {
    SimpleStringProperty id;
    SimpleStringProperty codice;
    SimpleStringProperty descrizione;

    public Ricetta(String id, String codice, String descrizione) {
        this.id = new SimpleStringProperty(id);
        this.codice = new SimpleStringProperty(codice);
        this.descrizione = new SimpleStringProperty(descrizione);
    }

    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getCodice() {
        return codice.get();
    }

    public void setCodice(String codice) {
        this.codice.set(codice);
    }

    public String getDescrizione() {
        return descrizione.get();
    }

    public void setDescrizione(String descrizione) {
        this.descrizione.set(descrizione);
    }
}
