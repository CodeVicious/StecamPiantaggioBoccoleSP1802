package sample;

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
            String SQL = "SELECT TOP 10 * FROM Person.Contact";
            ResultSet rs = stmt.executeQuery(SQL);

            // Iterate through the data in the result set and display it.
            while (rs.next()) {
                System.out.println(rs.getString("FirstName") + " " + rs.getString("LastName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}