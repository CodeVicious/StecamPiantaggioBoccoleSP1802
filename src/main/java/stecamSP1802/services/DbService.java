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

    public void connectDB() {
        try {
            conLDB = DriverManager.getConnection(ConfigurationManager.getInstance().getConnessioneLOCALSERVER());
            Logger.info("LOCAL DB CONNECTED");
            statusManager.setLocalDbStatus(StatusManager.LocalDbStatus.LOCAL_DB_CONNECTED);
        } catch (SQLException e1) {
            Logger.error("LOCAL DB NOT CONNECTED");
            Logger.error(e1);
            statusManager.setLocalDbStatus(StatusManager.LocalDbStatus.LOCAL_DB_CONNECTING);
        }

        try {
            conGDB = DriverManager.getConnection(ConfigurationManager.getInstance().getConnessioneSERVER());
            Logger.info("GLOBAL DB CONNECTED");
            statusManager.setGlobalDbStatus(StatusManager.GlobalDbStatus.GLOBAL_DB_CONNECTED);
        } catch (SQLException e) {
            Logger.error("GLOBAL DB NOT AVAILABLE");
            Logger.error(e);
            statusManager.setGlobalDbStatus(StatusManager.GlobalDbStatus.GLOBAL_DB_CONNECTING);
        }
    }

    public void synckUSERS() {
        String SQLSELECT = "SELECT * FROM [dbo].[b_OperatoreIMP]";
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

    }


    public  void storePiantaggio(String IOP, String PRG, String WO, String ESITO) {
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

    public  void storeWO(WorkOrder wo) {

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

            WorkOrder wo = new WorkOrder();
            Map<String, Parte> listaParti = Maps.newHashMap();

            while (rs.next()) {
                if (rs.getString("TipoArt").matches("PF")) {
                    wo.setBarCodeWO(rs.getString("wo"));
                    wo.setCodiceRicetta(rs.getString("Articolo"));
                    wo.setDescrizione(rs.getString("Descrizione"));
                } else {
                    listaParti.put(rs.getString("Articolo"), new Parte(rs.getString("Articolo"),
                            rs.getString("Descrizione"), rs.getBoolean("Checked")));
                }

            }
            wo.setListaParti(listaParti);

            return wo;

        } catch (SQLException e) {
            Logger.error(e);
        }
        return null;
    }

    public void login(String Matricola, String Password) {

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
        String SQLSELECT = "SELECT * FROM StecamSP1802.dbo.b_Operatore_SYNK where Matricola = "+matricola;
        Statement stmt = null;
        stmt = conLDB.createStatement();
        return (stmt.executeQuery(SQLSELECT));
    }

    public ResultSet queryMatricolaPassword(StringBuilder matricola, StringBuilder password) throws SQLException {
        String SQLSELECT = "SELECT * FROM StecamSP1802.dbo.b_Operatore_SYNK where Matricola = "+matricola+
                " AND HashPassword = '"+ PasswordMD5Converter.getMD5(password.toString())+"'";
        Statement stmt = null;
        stmt = conLDB.createStatement();
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
                "      ,[fk_ricetta] FROM StecamSP1802.dbo.ricette_dettaglio where fk_ricetta ="+id;
        Statement stmt = null;
        stmt = conLDB.createStatement();
        return (stmt.executeQuery(SQLSELECT));
    }

    public boolean insertRicette(String cod, String des) throws SQLException {

        String SQLINSERT = " INSERT INTO [dbo].[ricette]" +
                "([codice],[descrizione])" +
                "     VALUES (?,?)";

        PreparedStatement preparedStmt = conLDB.prepareStatement(SQLINSERT);
        preparedStmt.setString(1, cod) ;
        preparedStmt.setString(2, des);
        return  preparedStmt.execute();
    }
}