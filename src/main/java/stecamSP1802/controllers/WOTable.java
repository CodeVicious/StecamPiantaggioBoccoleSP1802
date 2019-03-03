package stecamSP1802.controllers;

import javafx.beans.property.SimpleStringProperty;
import stecamSP1802.services.barcode.Parte;

public class WOTable {
    private SimpleStringProperty articolo;
    private SimpleStringProperty descrizione;
    private SimpleStringProperty check;

    public WOTable(String art, String descrizione, Boolean ok) {
        this.articolo = new SimpleStringProperty(art);
        this.descrizione = new SimpleStringProperty(descrizione);

        if (ok)
            check = new SimpleStringProperty("OK");
        else
            check = new SimpleStringProperty("KO");
    }
    public String getArticolo() {
        return articolo.get();
    }

    public void setArticolo(String articolo) {
        this.articolo.set(articolo);
    }

    public String getDescrizione() {
        return descrizione.get();
    }

    public void setDescrizione(String descrizione) {
        this.descrizione.set(descrizione);
    }

    public String getCheck() {
        return check.get();
    }

    public void setCheck(String check) {
        this.check.set(check);
    }
}
