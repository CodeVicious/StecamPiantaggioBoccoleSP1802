package stecamSP1802.services.barcode;

import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class WorkOrder { //Singleton
    private static Logger Logger = LogManager.getLogger(WorkOrder.class);
    private static WorkOrder ourInstance = new WorkOrder();
    public static WorkOrder getInstance() {
        return ourInstance;
    }
    private WorkOrder() {}

    private String descrizione;
    private String barCodeWO;
    private String codiceRicetta;
    private Map<String, Parte> listaParti = Maps.newHashMap();

    public void addParte(String codiceUdM, String parte, String descrizione, Boolean verificata) {
        this.listaParti.put(parte, new Parte(codiceUdM, parte, descrizione, verificata));
    }

    public synchronized void setVerificata(String parte) {
        this.listaParti.get(parte).setVerificato(true);
    }

    public Boolean checkLavorabile() {
        Boolean lavorabile = true;
        for (String s : listaParti.keySet())
            lavorabile = listaParti.get(s).getVerificato() && lavorabile;
        return lavorabile;
    }

    public synchronized void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getBarCodeWO() {
        return barCodeWO;
    }

    public synchronized void setBarCodeWO(String barCodeWO) {
        this.barCodeWO = barCodeWO;
    }

    public String getCodiceRicetta() {
        return codiceRicetta;
    }

    public synchronized void setCodiceRicetta(String codiceRicetta) {
        this.codiceRicetta = codiceRicetta;
    }

    public Map<String, Parte> getListaParti() {
        return listaParti;
    }

    public synchronized void setListaParti(Map<String, Parte> listaParti) {
        this.listaParti = listaParti;
    }


    public synchronized void cleanUP() {
        getListaParti().clear();
        setDescrizione("");
        setCodiceRicetta("");
        setBarCodeWO("");
    }
}
