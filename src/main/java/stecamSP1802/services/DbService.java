package stecamSP1802.services;

import stecamSP1802.ConfigurationManager;

import java.sql.*;

public class DbService {
    private static DbService ourInstance = new DbService();
    public static DbService getInstance() {
        return ourInstance;
    }

    // Create a variable for the connection string.
    String connectionUrl = ConfigurationManager.getInstance().getJDBCString();

    Connection con;

    private DbService()
    {
        try {
            con = DriverManager.getConnection(connectionUrl);
            Statement stmt = con.createStatement();
            String SQL = "SELECT * FROM StecamSP1802.dbo.utentiStecam";
            ResultSet rs = stmt.executeQuery(SQL);

            // Iterate through the data in the result set and display it.
            while (rs.next()) {
                System.out.println(rs.getString("Matricola") + " " + rs.getString("NomeOperatore"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}