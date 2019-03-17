package stecamSP1802.controllers;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.Callback;

public class WOTable {
    private SimpleStringProperty uDm;
    private SimpleStringProperty articolo;
    private SimpleStringProperty descrizione;
    private SimpleStringProperty check;

    public WOTable(String uDM, String art, String descrizione, Boolean ok) {
        this.uDm = new SimpleStringProperty(uDM);
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

    public void setCheck(Boolean verificato) {
        if (verificato)
            setCheck("OK");
        else
            setCheck("KO");
    }

    public String getuDm() {
        return uDm.get();
    }

    public void setuDm(String uDm) {
        this.uDm.set(uDm);
    }

    public SimpleStringProperty uDmProperty() {
        if (uDm == null)
            return new SimpleStringProperty();
        return uDm;
    }

    public SimpleStringProperty articoloProperty() {
        if (articolo == null)
            new SimpleStringProperty();
        return articolo;
    }

    public SimpleStringProperty descrizioneProperty() {
        if (descrizione == null)
            return new SimpleStringProperty();
        return descrizione;
    }

    public SimpleStringProperty checkProperty() {
        if (check == null)
            return new SimpleStringProperty();
        return check;
    }

    public static Callback<WOTable, Observable[]> extractor() {
        return (WOTable p) -> new Observable[]{p.uDmProperty(), p.articoloProperty(), p.descrizioneProperty(), p.checkProperty()};
    }
}
