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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.ConfigurationManager;
import stecamSP1802.services.DbService;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class RicetteController implements Initializable, ControlledScreen {
    private static Logger Logger = LogManager.getLogger(RicetteController.class);
    ConfigurationManager conf = ConfigurationManager.getInstance();
    DbService dbService;
    ScreensController myController;

    @FXML
    private TableView<RicettaDett> tblRicettaDett;

    @FXML
    private TableColumn<RicettaDett, String> tblRicettaDettId;

    @FXML
    private TableColumn<RicettaDett, String> tblRicettaDettCode;

    @FXML
    private TableColumn<RicettaDett, String> tblRicettaDettDes;

    @FXML
    private TableView<Ricetta> tblRicetta;

    @FXML
    private TableColumn<Ricetta, String> tblRicettaId;

    @FXML
    private TableColumn<Ricetta, String> tblRicettaCod;

    @FXML
    private TableColumn<Ricetta, String> tblRicettaDes;

    @FXML
    private TextField txtCodRicetta;

    @FXML
    private TextField txtDesRicetta;

    @FXML
    private TextField txtCodComponente;

    @FXML
    private TextField txtDesComponente;

    @FXML
    private Button btnSAVE;

    @FXML
    private Button btnCANCEL;


    @FXML
    private Button btnDelComponente;

    @FXML
    private Button btnAddComponente;

    @FXML
    private Button btnDelRicetta;

    @FXML
    private Button btnAddRicetta;

    @FXML
    private Button btnModificaRicetta;

    @FXML
    private Button btnModificaDettaglio;

    @FXML
    private Label errCodRicetta;

    @FXML
    private Label errCodComponente;


    private ObservableList<Ricetta> ricettaData;
    private ObservableList<RicettaDett> ricettaDettagliData;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ricettaData = FXCollections.observableArrayList();
        ricettaDettagliData = FXCollections.observableArrayList();

        tblRicettaId.setCellValueFactory(new PropertyValueFactory<Ricetta, String>("id"));
        tblRicettaCod.setCellValueFactory(new PropertyValueFactory<Ricetta, String>("codice"));
        tblRicettaDes.setCellValueFactory(new PropertyValueFactory<Ricetta, String>("descrizione"));


        tblRicettaDettId.setCellValueFactory(new PropertyValueFactory<RicettaDett, String>("idDett"));
        tblRicettaDettCode.setCellValueFactory(new PropertyValueFactory<RicettaDett, String>("codiceDett"));
        tblRicettaDettDes.setCellValueFactory(new PropertyValueFactory<RicettaDett, String>("descrizioneDett"));

        tblRicetta.setItems(ricettaData);
        tblRicettaDett.setItems(ricettaDettagliData);


        setTxtValueAndRefreshFromClick();
    }

    public void loadDataFromDB() {
        ricettaData.clear();
        try {
            ResultSet rs = dbService.caricaRicette();

            while (rs.next()) {
                ricettaData.add(new Ricetta("" + rs.getInt(1), rs.getString(2), rs.getString(3)));
            }
        } catch (SQLException e) {
            Logger.error(e);
        }
        tblRicetta.setItems(ricettaData);
    }

    private void setTxtValueAndRefreshFromClick() {
        tblRicetta.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Ricetta ric = tblRicetta.getItems().get(tblRicetta.getSelectionModel().getSelectedIndex());
                txtCodRicetta.setText(ric.getCodice());
                txtDesRicetta.setText(ric.getDescrizione());
                loadRicettaDettaglio(ric.getId());
            }
        });

        tblRicettaDett.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                RicettaDett ricDet = tblRicettaDett.getItems().get(tblRicettaDett.getSelectionModel().getSelectedIndex());
                txtCodComponente.setText(ricDet.getCodiceDett());
                txtDesComponente.setText(ricDet.getDescrizioneDett());
            }
        });
    }

    private void loadRicettaDettaglio(String id) {

        ricettaDettagliData.clear();
        try {
            ResultSet rs = dbService.caricaRicettaDettaglio(id);

            while (rs.next()) {
                ricettaDettagliData.add(new RicettaDett("" + rs.getInt("id"),
                        rs.getString("codice"), rs.getString("descrizione"), rs.getString("fk_ricetta")));
            }
        } catch (SQLException e) {
            Logger.error(e);
        }
        tblRicettaDett.setItems(ricettaDettagliData);
    }

    @Override
    public void setScreenParent(ScreensController screenController) {
        myController = screenController;

    }

    public void setDbService(DbService dbService) {
        this.dbService = dbService;
    }

    public void onModDettaglio(ActionEvent actionEvent) {
        boolean isValidCode = TextFieldValidation.isValidCodeProduct(txtCodComponente, errCodComponente, "Immetere un codice valido");
        RicettaDett ric = tblRicettaDett.getItems().get(tblRicettaDett.getSelectionModel().getSelectedIndex());

        if (isValidCode) {

            Alert alert = new Alert(Alert.AlertType.WARNING,"Modificare il componente?",ButtonType.YES,ButtonType.NO);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {

                try {
                    dbService.modificaDettaglioRicetta(txtCodComponente.getText(), txtDesComponente.getText(),ric.getIdDett());
                    loadRicettaDettaglio(ric.getFkRicettaDett());


                } catch (SQLException e) {
                    Logger.error(e);
                }
            }
        }

    }

    public void onModRicetta(ActionEvent actionEvent) {
        boolean isValidCode = TextFieldValidation.isValidCodeProduct(txtCodRicetta, errCodRicetta, "Modificare con un codice valido");
        Ricetta ric = tblRicetta.getItems().get(tblRicetta.getSelectionModel().getSelectedIndex());

        if (isValidCode) {

            Alert alert = new Alert(Alert.AlertType.WARNING,"Modificare la ricetta?",ButtonType.YES,ButtonType.NO);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {

                try {
                    dbService.modificaRicetta(txtCodRicetta.getText(), txtDesRicetta.getText(),ric.getId());
                    loadDataFromDB();


                } catch (SQLException e) {
                    Logger.error(e);
                }
            }
        }

    }

    public void onAddRicetta(ActionEvent actionEvent) {
        boolean isValidCode = TextFieldValidation.isValidCodeProduct(txtCodRicetta, errCodRicetta, "Immettere un codice valido");

        if (isValidCode) {

            Alert alert = new Alert(Alert.AlertType.WARNING,"Inserire una nuova ricetta?",ButtonType.YES,ButtonType.NO);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {

                try {
                    dbService.insertRicette(txtCodRicetta.getText(), txtDesRicetta.getText());
                    loadDataFromDB();


                } catch (SQLException e) {
                    Logger.error(e);
                }
            }
        }
    }

    public void onDelRicetta(ActionEvent actionEvent) {
        boolean isNotEmptyId = TextFieldValidation.isValidCodeProduct(txtCodRicetta, errCodRicetta, "Selezionare almeno una ricetta da eliminare");
        Ricetta ric = tblRicetta.getItems().get(tblRicetta.getSelectionModel().getSelectedIndex());

        if (isNotEmptyId) {

            Alert alert = new Alert(Alert.AlertType.WARNING,"Eliminare la ricetta "+txtCodRicetta.getText()+" e tutti i componenti ?",ButtonType.YES,ButtonType.NO);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {

                try {
                    dbService.deleteRicetta(ric.getId());
                    loadDataFromDB();
                    loadRicettaDettaglio(ric.getId());


                } catch (SQLException e) {
                    Logger.error(e);
                }
            }
        }
    }

    public void onAddDettaglio(ActionEvent actionEvent) {
        boolean isValidCode = TextFieldValidation.isValidCodeProduct(txtCodComponente, errCodComponente, "immettere un codice componente valido");
        Ricetta ric = tblRicetta.getItems().get(tblRicetta.getSelectionModel().getSelectedIndex());


        if (isValidCode) {

            Alert alert = new Alert(Alert.AlertType.WARNING,"Inserire un nuovo componente?",ButtonType.YES,ButtonType.NO);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {

                try {
                    dbService.insertDettaglioRicetta(txtCodComponente.getText(), txtDesComponente.getText(),ric.getId());
                    loadRicettaDettaglio(ric.getId());


                } catch (SQLException e) {
                    Logger.error(e);
                }
            }
        }

    }


    public void onDelDettaglio(ActionEvent actionEvent) {
        boolean isNotEmptyId = TextFieldValidation.isValidCodeProduct(txtCodComponente, errCodComponente, "Selezionare almeno un componente da eliminare");
        RicettaDett ric = tblRicettaDett.getItems().get(tblRicettaDett.getSelectionModel().getSelectedIndex());

        if (isNotEmptyId) {

            Alert alert = new Alert(Alert.AlertType.WARNING,"Eliminare il compente "+txtCodComponente.getText()+" ?",ButtonType.YES,ButtonType.NO);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {

                try {
                    dbService.deleteRicettaDettaglio(ric.getIdDett());

                    loadRicettaDettaglio(ric.getFkRicettaDett());

                } catch (SQLException e) {
                    Logger.error(e);
                }
            }
        }
    }
}
