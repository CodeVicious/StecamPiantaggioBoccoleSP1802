package stecamSP1802.services;

public class EsitoWebQuery {

    public static enum ESITO {
        OK, KO
    }

    private ESITO esitoQuery;
    private String resultQuery;

    public EsitoWebQuery(ESITO esitoQuery, String resultQuery){
        this.esitoQuery = esitoQuery;
        this.resultQuery = resultQuery;
    }

    public void setEsitoQuery(ESITO esitoQuery) {
        this.esitoQuery = esitoQuery;
    }

    public ESITO getEsitoQuery() {
        return esitoQuery;
    }

    public void setResultQuery(String resultQuery) {
        this.resultQuery = resultQuery;
    }

    public String getResultQuery() {
        return resultQuery;
    }
}
