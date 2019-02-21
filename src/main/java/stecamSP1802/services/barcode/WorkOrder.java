package stecamSP1802.services.barcode;

import com.google.common.collect.Maps;
import java.util.Map;

public class WorkOrder {
    private String barCodeWO;
    private String codiceRicetta;
    private Map<String, Parte> listaParti = Maps.newHashMap();

    public WorkOrder(String barCodeWO, String codiceRicetta, Map<String, Parte> listaParti) {
        this.barCodeWO = barCodeWO;
        this.codiceRicetta = codiceRicetta;
        this.listaParti = listaParti;
    }

    public void addParte(String parte,String descrizione, Boolean verificata){
        this.listaParti.put(parte,new Parte(parte,descrizione,verificata));
    }

    public void setVerificata (String parte){
        this.listaParti.get(parte).setVerificato(true);
    }

    public Boolean checkLavorabile(){
        Boolean lavorabile = true;
        for (String s: listaParti.keySet())
            lavorabile = listaParti.get(s).getVerificato() && lavorabile;
        return lavorabile;
    }


    public String getBarCodeWO() {
        return barCodeWO;
    }

    public void setBarCodeWO(String barCodeWO) {
        this.barCodeWO = barCodeWO;
    }

    public String getCodiceRicetta() {
        return codiceRicetta;
    }

    public void setCodiceRicetta(String codiceRicetta) {
        this.codiceRicetta = codiceRicetta;
    }

    public Map<String, Parte> getListaParti() {
        return listaParti;
    }

    public void setListaParti(Map<String, Parte> listaParti) {
        this.listaParti = listaParti;
    }
}
