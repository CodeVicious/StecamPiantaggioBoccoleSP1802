package stecamSP1802.services;

import com.google.common.base.Preconditions;
import stecamSP1802.ConfigurationManager;

import java.sql.*;

public class DbService {
    String connectionUrl = ConfigurationManager.getInstance().getJDBCString();
    Connection conLDB,conGDB;

    public DbService(StatusManager statusManager)
    {
        Preconditions.checkNotNull(statusManager);
        try {
            conLDB = DriverManager.getConnection(ConfigurationManager.getInstance().getConnessioneLOCALSERVER());
            conGDB = DriverManager.getConnection(ConfigurationManager.getInstance().getConnessioneSERVER());

            } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }


    public void synckUSERS() {
        String SQLSELECT = "SELECT * FROM StecamSP1802.dbo.utentiStecam";
        String SQLINSERT = " insert into users (first_name, last_name, date_created, is_admin, num_points)"
                + " values (?, ?, ?, ?, ?)";

        try {
            PreparedStatement preparedStmt = conLDB.prepareStatement(SQLINSERT);
            preparedStmt.setString (1, "Barney");
            preparedStmt.setString (2, "Rubble");
            preparedStmt.setBoolean(4, false);
            preparedStmt.setInt    (5, 5000);

            // execute the preparedstatement
            preparedStmt.execute();

            Statement stmt = null;
            stmt = conLDB.createStatement();
            ResultSet rs = stmt.executeQuery(SQLSELECT);
            // Iterate through the data in the result set and display it.
            while (rs.next()) {
                System.out.println(rs.getString("Matricola") + " " + rs.getString("NomeOperatore"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}