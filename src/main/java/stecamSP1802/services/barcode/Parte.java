package stecamSP1802.services.barcode;

public class Parte {
    private  String codiceUdM;
    private  String codice;
    private  String descrizione;
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

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public String getCodiceUdM() {
        return codiceUdM;
    }

    public void setCodiceUdM(String codiceUdM) {
        this.codiceUdM = codiceUdM;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Boolean getVerificato() {
        return verificato;
    }
    public void setVerificato(Boolean verificato) {
        this.verificato = verificato;
    }
}
