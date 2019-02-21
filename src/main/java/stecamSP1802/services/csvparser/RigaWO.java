package stecamSP1802.services.csvparser;

import com.univocity.parsers.annotations.NullString;
import com.univocity.parsers.annotations.Parsed;

public class RigaWO {

    // if the value parsed in the quantity column is "?" or "-", it will be replaced
    // by null.
    @NullString(nulls = { "?", "-" })
    // if a value resolves to null, it will be converted to the String "0".

    @Parsed(field = "wo")
    private String wo;

    @Parsed(field = "TipoArt")
    private String TipoArt;

    @Parsed(field = "Articolo")
    private String Articolo;

    @Parsed(field = "Descrizione")
    private String Descrizione;


    public String getWo() {
        return wo;
    }

    public void setWo(String wo) {
        this.wo = wo;
    }

    public String getTipoArt() {
        return TipoArt;
    }

    public void setTipoArt(String tipoArt) {
        TipoArt = tipoArt;
    }

    public String getArticolo() {
        return Articolo;
    }

    public void setArticolo(String articolo) {
        Articolo = articolo;
    }

    public String getDescrizione() {
        return Descrizione;
    }

    public void setDescrizione(String descrizione) {
        Descrizione = descrizione;
    }
}
