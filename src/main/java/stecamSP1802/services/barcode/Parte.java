package stecamSP1802.services.barcode;

public class Parte {
    private final String codice;
    private final String descrizione;
    private Boolean verificato;

    public Parte(String codice, String descrizione, Boolean verificato) {
        this.codice = codice;
        this.descrizione = descrizione;
        this.verificato = verificato;
    }
    public String getCodice() {
        return codice;
    }
    public String getDescrizione() {
        return descrizione;
    }
    public Boolean getVerificato() {
        return verificato;
    }
    public void setVerificato(Boolean verificato) {
        this.verificato = verificato;
    }
}
