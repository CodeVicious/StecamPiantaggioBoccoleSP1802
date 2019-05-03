package stecamSP1802.services;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.ConfigurationManager;
import stecamSP1802.helper.PasswordMD5Converter;
import stecamSP1802.services.barcode.Parte;
import stecamSP1802.services.barcode.WorkOrder;

import java.sql.*;
import java.util.Calendar;
import java.util.Map;

public class DbService {
    private final StatusManager statusManager;
    private Logger Logger = LogManager.getLogger(DbService.class);
    Connection conLDB, conGDB;

    public DbService(StatusManager statusManager) {
        Preconditions.checkNotNull(statusManager);
        this.statusManager = statusManager;
    }

    public void connectLocalDB() {
        try {
            conLDB = DriverManager.getConnection(ConfigurationManager.getInstance().getConnessioneLOCALSERVER());
            Logger.info("LOCAL DB CONNECTED");
            statusManager.setLocalDbStatus(StatusManager.LocalDbStatus.LOCAL_DB_CONNECTED);
        } catch (SQLException e1) {
            Logger.error("LOCAL DB NOT CONNECTED");
            Logger.error(e1);
            statusManager.setLocalDbStatus(StatusManager.LocalDbStatus.LOCAL_DB_DISCONNECTED);
        }

    }

    public void synckUSERS() {
        if (statusManager.getGlobalDbStatus() == StatusManager.GlobalDbStatus.GLOBAL_DB_CONNECTED) {
            String SQLSELECT = "SELECT * FROM [dbo].[b_Operatore]";
            String SQLDROP = "TRUNCATE TABLE [dbo].[b_Operatore_SYNK]";
            String SQLINSERT = " INSERT INTO [dbo].[b_Operatore_SYNK]" +
                    "           ([Matricola]" +
                    "           ,[NomeOperatore]" +
                    "           ,[ConduttoreDiLinea]" +
                    "           ,[HashPassword])" +
                    "     VALUES" +
                    "           (?,?,?,?)";

            PreparedStatement preparedStmt = null;
            try {
                preparedStmt = conLDB.prepareStatement(SQLINSERT);

                Statement stmt = conLDB.createStatement();
                stmt.executeUpdate(SQLDROP);
                ResultSet rs = stmt.executeQuery(SQLSELECT);
                // Iterate through the data in the result set and display it.
                while (rs.next()) {
                    preparedStmt.setString(1, rs.getString("Matricola"));
                    preparedStmt.setString(2, rs.getString("NomeOperatore"));
                    preparedStmt.setInt(3, rs.getInt("ConduttoreDiLinea"));
                    preparedStmt.setString(4, rs.getString("HashPassword"));
                    preparedStmt.execute();
                    // execute the preparedstatement
                }
            } catch (SQLException e) {
                Logger.error(e);
            }
        } else
            Logger.warn("DB SPAL DISCONNESSO. SINCRONIZZAZIONE UTENTI IMPOSSIIBLE.");

    }


    public void storePiantaggio(String IOP, String PRG, String WO, String ESITO) {
        try {
            String SQLINSERT = " INSERT INTO [dbo].[piantaggi]" +
                    "([TS],[IOP],[PRG],[WO],[ESITO])" +
                    "     VALUES (?,?,?,?,?)";

            PreparedStatement preparedStmt = conLDB.prepareStatement(SQLINSERT);
            preparedStmt.setTimestamp(1, new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
            preparedStmt.setString(2, IOP);
            preparedStmt.setString(3, PRG);
            preparedStmt.setString(4, WO);
            preparedStmt.setString(5, ESITO);
            preparedStmt.execute();


        } catch (SQLException e) {
            Logger.error(e);
        }

    }

    public void storeWO(WorkOrder wo) {
        String SQLDROP = "TRUNCATE TABLE [dbo].[cache]";

        try {

            Statement stmt = conLDB.createStatement();
            stmt.executeUpdate(SQLDROP);

            String SQLINSERT = " INSERT INTO [dbo].[cache]" +
                    "([wo],[TipoArt],[Articolo],[Descrizione],[Checked])" +
                    "     VALUES (?,?,?,?,?)";

            PreparedStatement preparedStmt = conLDB.prepareStatement(SQLINSERT);
            preparedStmt.setString(1, wo.getBarCodeWO());
            preparedStmt.setString(2, "PF");
            preparedStmt.setString(3, wo.getCodiceRicetta());
            preparedStmt.setString(4, wo.getDescrizione());
            preparedStmt.setBoolean(5, false);

            preparedStmt.execute();

            Map<String, Parte> listaParti = wo.getListaParti();
            for (String s : listaParti.keySet()) {
                preparedStmt.setString(1, s);
                preparedStmt.setString(2, "Componente");
                preparedStmt.setString(3, listaParti.get(s).getCodice());
                preparedStmt.setString(4, listaParti.get(s).getDescrizione());
                preparedStmt.setBoolean(5, listaParti.get(s).getVerificato());
                preparedStmt.execute();
            }


        } catch (SQLException e) {
            Logger.error(e);
        }
    }

    public WorkOrder loadWO() {

        try {
            String SQLSELECT = "SELECT * FROM [dbo].[cache]";
            Statement stmt = conLDB.createStatement();
            ResultSet rs = stmt.executeQuery(SQLSELECT);

            WorkOrder wo = WorkOrder.getInstance();
            Map<String, Parte> listaParti = Maps.newHashMap();

            while (rs.next()) {
                if (rs.getString("TipoArt").matches("PF")) {
                    wo.setBarCodeWO(rs.getString("wo"));
                    wo.setCodiceRicetta(rs.getString("Articolo"));
                    wo.setDescrizione(rs.getString("Descrizione"));
                } else {
                    listaParti.put(rs.getString("Articolo"), new Parte("", rs.getString("Articolo"), rs.getString("Descrizione"), rs.getBoolean("checked")));
                }

            }
            wo.setListaParti(listaParti);

            return wo;

        } catch (SQLException e) {
            Logger.error(e);
        }
        return null;
    }


    public void close() {
        try {
            conLDB.close();
        } catch (SQLException e) {
            Logger.error(e);
        }
        try {
            conGDB.close();
        } catch (SQLException e) {
            Logger.error(e);
        }
    }

    public ResultSet queryMatricola(StringBuilder matricola) throws SQLException {
        String SQLSELECT = "SELECT * FROM StecamSP1802.dbo.b_Operatore_SYNK where Matricola = " + matricola;
        Statement stmt = null;
        if (statusManager.getGlobalDbStatus() == StatusManager.GlobalDbStatus.GLOBAL_DB_DISCONNECTED) {
            stmt = conLDB.createStatement();
        } else {
            stmt = conGDB.createStatement();
        }
        return (stmt.executeQuery(SQLSELECT));
    }

    public ResultSet queryMatricolaPassword(StringBuilder matricola, StringBuilder password) throws SQLException {
        String SQLSELECT = "SELECT * FROM StecamSP1802.dbo.b_Operatore_SYNK where Matricola = " + matricola +
                " AND HashPassword = '" + PasswordMD5Converter.getMD5(password.toString()) + "'";
        Statement stmt = null;
        if (statusManager.getGlobalDbStatus() == StatusManager.GlobalDbStatus.GLOBAL_DB_DISCONNECTED) {
            stmt = conLDB.createStatement();
        } else {
            stmt = conGDB.createStatement();
        }
        return (stmt.executeQuery(SQLSELECT));
    }

    public ResultSet caricaRicette() throws SQLException {
        String SQLSELECT = "SELECT * FROM StecamSP1802.dbo.ricette";
        Statement stmt = null;
        stmt = conLDB.createStatement();
        return (stmt.executeQuery(SQLSELECT));
    }

    public ResultSet caricaRicettaDettaglio(String id) throws SQLException {
        String SQLSELECT = "SELECT [id]" +
                "      ,[codice]" +
                "      ,[descrizione]" +
                "      ,[fk_ricetta] FROM StecamSP1802.dbo.ricette_dettaglio where fk_ricetta =" + id;
        Statement stmt = null;
        stmt = conLDB.createStatement();
        return (stmt.executeQuery(SQLSELECT));
    }

    public boolean insertRicette(String cod, String des) throws SQLException {

        String SQLINSERT = " INSERT INTO [dbo].[ricette]" +
                "([codice],[descrizione])" +
                "     VALUES (?,?)";

        PreparedStatement preparedStmt = conLDB.prepareStatement(SQLINSERT);
        preparedStmt.setString(1, cod);
        preparedStmt.setString(2, des);
        return preparedStmt.execute();
    }

    public void deleteRicetta(String id) throws SQLException {

        String SQLDEL = " DELETE FROM [dbo].[ricette] WHERE id = ?";

        PreparedStatement preparedStmt = conLDB.prepareStatement(SQLDEL);
        preparedStmt.setString(1, id);

        String SQLDETAILDEL = " DELETE FROM [dbo].[ricette_dettaglio] WHERE fk_ricetta = ?";
        PreparedStatement preparedStmtDettaglio = conLDB.prepareStatement(SQLDETAILDEL);
        preparedStmtDettaglio.setString(1, id);

        try {
            preparedStmtDettaglio.execute();
            preparedStmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void insertDettaglioRicetta(String codice, String des, String id) throws SQLException {
        String SQLINSERT = " INSERT INTO [dbo].[ricette_dettaglio]" +
                "([codice],[descrizione],[fk_ricetta])" +
                "     VALUES (?,?,?)";

        PreparedStatement preparedStmt = conLDB.prepareStatement(SQLINSERT);
        preparedStmt.setString(1, codice);
        preparedStmt.setString(2, des);
        preparedStmt.setString(3, id);
        preparedStmt.execute();

    }

    public void deleteRicettaDettaglio(String idDett) throws SQLException {

        String SQLDETAILDEL = " DELETE FROM [dbo].[ricette_dettaglio] WHERE id = ?";
        PreparedStatement preparedStmtDettaglio = conLDB.prepareStatement(SQLDETAILDEL);
        preparedStmtDettaglio.setString(1, idDett);
        preparedStmtDettaglio.execute();


    }

    public void modificaRicetta(String cod, String desc, String id) throws SQLException {

        String SQLMODRIC = "UPDATE [dbo].[ricette] SET [codice] = ?, [descrizione] = ?" +
                " WHERE id = ? ";

        PreparedStatement preparedStmt = conLDB.prepareStatement(SQLMODRIC);
        preparedStmt.setString(1, cod);
        preparedStmt.setString(2, desc);
        preparedStmt.setString(3, id);
        preparedStmt.execute();

    }

    public void modificaDettaglioRicetta(String cod, String desc, String idDett) throws SQLException {

        String SQLMODRIC = "UPDATE [dbo].[ricette_dettaglio] SET [codice] = ?, [descrizione] = ?" +
                " WHERE id = ? ";

        PreparedStatement preparedStmtDettaglio = conLDB.prepareStatement(SQLMODRIC);
        preparedStmtDettaglio.setString(1, cod);
        preparedStmtDettaglio.setString(2, desc);
        preparedStmtDettaglio.setString(3, idDett);
        preparedStmtDettaglio.execute();
    }


    public boolean queryLocalPassword(String password) {
        String SQLSELECT = "SELECT * FROM StecamSP1802.dbo.parametri where chiave = localadmin";

        Statement stmt = null;
        try {
            stmt = conLDB.createStatement();
            ResultSet rs = stmt.executeQuery(SQLSELECT);
            if (rs.getString("value") == PasswordMD5Converter.getMD5(password.toString()))
                return true;
            else
                return false;

        } catch (SQLException e) {
            Logger.error(e);
            return false;
        }
    }

    public String queryParametro(String chiave) {
        String SQLSELECT = "SELECT * FROM StecamSP1802.dbo.parametri where chiave = ?";

        PreparedStatement preparedStmtDettaglio = null;
        try {
            preparedStmtDettaglio = conLDB.prepareStatement(SQLSELECT);
            preparedStmtDettaglio.setString(1, chiave);
            ResultSet res = preparedStmtDettaglio.executeQuery();
            return res.getString("value");
        } catch (SQLException e) {
            Logger.error(e);
            return "";
        }
    }

    public Map<String, String> queryParametri() throws SQLException {
        String SQLSELECT = "SELECT * FROM StecamSP1802.dbo.parametri";
        Map<String, String> parametri = Maps.newHashMap();

        PreparedStatement preparedStmtDettaglio = null;

        preparedStmtDettaglio = conLDB.prepareStatement(SQLSELECT);
        ResultSet res = preparedStmtDettaglio.executeQuery();
        while (res.next()) {
            parametri.put(res.getString("chiave"), res.getString("value"));
        }
        return parametri;

    }

    public boolean loadRicetta(String barCode) {

        //Carico la ricetta

        String SQLSELECT = "SELECT * FROM StecamSP1802.dbo.ricette where codice=?";


        PreparedStatement preparedStmtDettaglio = null;
        try {
            preparedStmtDettaglio = conLDB.prepareStatement(SQLSELECT);
            preparedStmtDettaglio.setString(1, barCode);
            ResultSet res = preparedStmtDettaglio.executeQuery();
            if (!res.next())
                return false;

            ResultSet dett = caricaRicettaDettaglio(res.getString("id"));

            WorkOrder wo = WorkOrder.getInstance();

            wo.setCodiceRicetta(barCode);
            wo.setDescrizione(res.getString("descrizione"));

            Map<String, Parte> listaParti = Maps.newHashMap();

            while (dett.next()) {
                listaParti.put(dett.getString("codice"), new Parte("", dett.getString("codice"), dett.getString("descrizione"), false));
            }

            wo.setListaParti(listaParti);

            return true;


        } catch (SQLException e) {
            Logger.error(e);
        }


        return false;
    }

    public void saveParametri(Map<String, String> param) throws SQLException {

        String SQLINSERT = " UPDATE [dbo].[parametri]" +
                "SET [value] = ?  WHERE [chiave] = ?";

        PreparedStatement preparedStmt = null;
        preparedStmt = conLDB.prepareStatement(SQLINSERT);

        Statement stmt = conLDB.createStatement();

        for (String p : param.keySet()) {
            preparedStmt.setString(1, param.get(p));
            preparedStmt.setString(2, p);
            preparedStmt.addBatch();
            // execute the preparedstatement
        }
        preparedStmt.executeBatch();
    }

    public void connectRemoteDB() {
        try {
            conGDB = DriverManager.getConnection(ConfigurationManager.getInstance().getConnessioneSERVER());
            Logger.info("GLOBAL DB CONNECTED");
            statusManager.setGlobalDbStatus(StatusManager.GlobalDbStatus.GLOBAL_DB_CONNECTED);
        } catch (SQLException e) {
            Logger.error("GLOBAL DB NOT AVAILABLE");
            Logger.error(e);
            statusManager.setGlobalDbStatus(StatusManager.GlobalDbStatus.GLOBAL_DB_DISCONNECTED);
        }
    }

    public boolean isGlobalOffline() {
        try {
            if ((conGDB!=null) && conGDB.isValid(3)) {
                statusManager.setGlobalDbStatus(StatusManager.GlobalDbStatus.GLOBAL_DB_CONNECTED);
                return false;
            }
        } catch (SQLException e) {
            Logger.error("CONNESSION AL DB GLOBALE IMPOSSIBILE");
            statusManager.setGlobalDbStatus(StatusManager.GlobalDbStatus.GLOBAL_DB_DISCONNECTED);
            return false;
        }
        statusManager.setGlobalDbStatus(StatusManager.GlobalDbStatus.GLOBAL_DB_DISCONNECTED);
        return true;
    }
}