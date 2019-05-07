package stecamSP1802.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.ConfigurationManager;
import stecamSP1802.MainStecamPiantaggioBoccoleSP1802;
import stecamSP1802.services.DbService;
import stecamSP1802.services.barcode.Parte;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

public class AggiornaRicetteController extends AbstractController implements Initializable {
    private static Logger Logger = LogManager.getLogger(AggiornaRicetteController.class);
    ConfigurationManager conf = ConfigurationManager.getInstance();

    @FXML
    private TableView<RicettaDett> tblRicettaNUOVA;



    @FXML
    private TableColumn<RicettaDett, String> codiceCOMPONENTENUOVO;

    @FXML
    private TableColumn<RicettaDett, String> descrizioneCOMPONENTENUOVO;

    @FXML
    private TextField txtCodRicetta;

    @FXML
    private Button btnCANCEL;

    @FXML
    private Button btnAGGIORNA;

    @FXML
    private TableView<RicettaDett> tblRicettaVECCCHIA;



    @FXML
    private TableColumn<RicettaDett, String> codiceCOMPONENTEVECCHIO;

    @FXML
    private TableColumn<RicettaDett, String> descrizioneCOMPONENTEVECCHIO;

    private ObservableList<RicettaDett> ricettaDettagliDataVECCHIA;
    private ObservableList<RicettaDett> ricettaDettagliDataNUOVA;

    private Stage stage;
    private boolean Update;
    private String CodiceRicetta;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ricettaDettagliDataVECCHIA = FXCollections.observableArrayList();
        ricettaDettagliDataNUOVA = FXCollections.observableArrayList();

        codiceCOMPONENTENUOVO.setCellValueFactory(new PropertyValueFactory<RicettaDett, String>("codiceDett"));
        descrizioneCOMPONENTENUOVO.setCellValueFactory(new PropertyValueFactory<RicettaDett, String>("descrizioneDett"));

        codiceCOMPONENTEVECCHIO.setCellValueFactory(new PropertyValueFactory<RicettaDett, String>("codiceDett"));
        descrizioneCOMPONENTEVECCHIO.setCellValueFactory(new PropertyValueFactory<RicettaDett, String>("descrizioneDett"));


        tblRicettaNUOVA.setItems(ricettaDettagliDataNUOVA);
        tblRicettaVECCCHIA.setItems(ricettaDettagliDataVECCHIA);



        Update = false;
    }



    public void onBtnBack(ActionEvent event) {
        Update = false;
        closeStage();
    }

    private void closeStage() {
        if(stage!=null) {
            stage.close();
        }
    }

    public void onBtnUdate(ActionEvent actionEvent) {
        Update = true;
        closeStage();
    }


    public boolean isUpdate() {
        return Update;
    }

    public String getCodiceRicetta() {
        return CodiceRicetta;
    }

    public void setCodiceRicetta(String codiceRicetta) {
        CodiceRicetta = codiceRicetta;
        txtCodRicetta.setText(CodiceRicetta);
    }

    public void setStage(Stage popupStage) {
        this.stage = popupStage;
    }

    public void setRicette(Map<String, Parte> listaLocale, Map<String, Parte> listaServer) {
        ricettaDettagliDataNUOVA.clear();
        ricettaDettagliDataVECCHIA.clear();

        for(String rc:listaLocale.keySet()){
            ricettaDettagliDataVECCHIA.add(new RicettaDett("",listaLocale.get(rc).getCodice(),listaLocale.get(rc).getDescrizione(),""));
        }

        for(String rc:listaServer.keySet()){
            ricettaDettagliDataNUOVA.add(new RicettaDett("",listaServer.get(rc).getCodice(),listaServer.get(rc).getDescrizione(),""));
        }

        tblRicettaVECCCHIA.setItems(ricettaDettagliDataVECCHIA);
        tblRicettaNUOVA.setItems(ricettaDettagliDataNUOVA);

    }
}
