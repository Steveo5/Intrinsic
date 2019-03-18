package com.hotmail.intrinsic.storage;

import com.hotmail.intrinsic.Region;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class MysqlConnector {

    private Connection connection;
    private String host, database, username, password;
    private int port;

    public MysqlConnector(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database + "?useSSL=false";
        this.username = username;
        this.password = password;
    }

    public void saveRegion(Region region) {

    }

//    public Region getIntersecting(Location location) {
//        try {
//            openConnection();
//            Statement statement = connection.createStatement();
////            statement.executeQuery("SELECT * FROM regions WHERE ")
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    public void openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database, this.username, this.password);
        }
    }

    public boolean testConnection() {
        try {
            openConnection();
            return true;
        } catch (ClassNotFoundException e) { } catch (SQLException e) { }

        return false;
    }

    public void initialiseTables() {
        try {
            openConnection();
            Statement statement = connection.createStatement();

            String regionsTableQuery = "CREATE TABLE IF NOT EXISTS regions (";
            regionsTableQuery += "id INT AUTO_INCREMENT,";
            regionsTableQuery += "owner VARCHAR(255) NOT NULL,";
            regionsTableQuery += "created_at DATE NOT NULL,";
            regionsTableQuery += "priority INTEGER NOT NULL,";
            regionsTableQuery += "name TEXT NOT NULL,";
            regionsTableQuery += "PRIMARY KEY (id)";
            statement.executeQuery(regionsTableQuery);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
