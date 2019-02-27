package stecamSP1802.services;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.ConfigurationManager;
import stecamSP1802.helper.PasswordMD5Converter;

import java.sql.*;

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
        String SQLSELECT = "SELECT * FROM StecamSP1802.dbo.userSPAL";
        String SQLDROP = "DELETE * FROM [dbo].[utentiREMOTI]";
        String SQLINSERT = " INSERT INTO [dbo].[utentiREMOTI]" +
                "           ([Matricola]" +
                "           ,[NomeOperatore]" +
                "           ,[ConduttoreDiLinea]" +
                "           ,[HashPassword])" +
                "     VALUES" +
                "           (?,?,?,?,?)";

        try {
            PreparedStatement preparedStmt = conLDB.prepareStatement(SQLINSERT);
            preparedStmt.execute();

            Statement stmt = conLDB.createStatement();
            stmt.executeQuery(SQLDROP);
            ResultSet rs = stmt.executeQuery(SQLSELECT);
            // Iterate through the data in the result set and display it.
            while (rs.next()) {
                preparedStmt.setString(1, rs.getString("Matricola"));
                preparedStmt.setString(2, rs.getString("NomeOperatore"));
                preparedStmt.setInt(4, rs.getInt("ConduttoreDiLinea"));
                preparedStmt.setString(5, rs.getString("HashPassword"));

                // execute the preparedstatement
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
}