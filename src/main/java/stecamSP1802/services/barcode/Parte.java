package stecamSP1802.services.barcode;

public class Parte {
    private final String codiceUdM;
    private final String codice;
    private final String descrizione;
    private Boolean verificato;

    public Parte(String codiceUdM, String codice, String descrizione, Boolean verificato) {
        this.codiceUdM = codiceUdM;
        this.codice = codice;
        this.descrizione = descrizione;
        this.verificato = verificato;
    }
    public String getCodice() {
        return codice;
    }

    public String getCodiceUdM() {
        return codiceUdM;
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
