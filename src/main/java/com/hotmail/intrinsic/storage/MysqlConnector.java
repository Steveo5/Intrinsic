package com.hotmail.intrinsic.storage;

import com.hotmail.intrinsic.Region;
import org.bukkit.Location;

import java.sql.*;
import java.util.ArrayList;
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

        this.initialiseTables();
    }

    public void saveRegion(Region region) {
        try {
            openConnection();

            String query = "INSERT INTO regions(owner, created_at, priority, radius, center_x, center_y, center_z, " +
                    "world, min_x, min_y, min_z, max_x, max_y, max_z, name) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement statement = connection.prepareStatement(query);

            java.util.Date dt = new java.util.Date();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");

            String currentTime = sdf.format(dt);
            Location min = region.getBounds()[0];
            Location max = region.getBounds()[1];
            statement.setString(1, region.getOwner().getUniqueId().toString());
            statement.setTimestamp(2, Timestamp.valueOf(currentTime));
            statement.setInt(3, 1);
            statement.setInt(4, region.getType().getRadius());
            statement.setInt(5, region.getLocation().getBlockX());
            statement.setInt(6, region.getLocation().getBlockY());
            statement.setInt(7, region.getLocation().getBlockZ());
            statement.setString(8, region.getLocation().getWorld().getName());
            statement.setInt(9, min.getBlockX());
            statement.setInt(10, min.getBlockY());
            statement.setInt(11, min.getBlockZ());
            statement.setInt(12, max.getBlockX());
            statement.setInt(13, max.getBlockY());
            statement.setInt(14, max.getBlockZ());
            statement.setString(15, region.getType().getName());

            statement.execute();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Region> getIntersecting(Location location) {
        List<Region> intersecting = new ArrayList<Region>();

        try {
            openConnection();
            Statement statement = connection.createStatement();
            String query = "SELECT * FROM regions ";
            query += "INNER JOIN locations ON regions.center_loc = locations.id";
            query += "INNER JOIN locations ON regions.min_loc = locations.id";
            query += "INNER JOIN locations ON regions.max_loc = locations.id";
            ResultSet rs = statement.executeQuery(query);
            while(rs.next()) {

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return intersecting;
    }

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
/*            Statement statement = connection.createStatement();
            String locationsTableQuery = "CREATE TABLE IF NOT EXISTS locations (";
            locationsTableQuery += "id INT AUTO_INCREMENT,";
            locationsTableQuery += "x INT NOT NULL,";
            locationsTableQuery += "y INT NOT NULL,";
            locationsTableQuery += "z INT NOT NULL,";
            locationsTableQuery += "world TEXT NOT NULL,";
            locationsTableQuery += "PRIMARY KEY (id));";*/

            String regionsTableQuery = "CREATE TABLE IF NOT EXISTS regions (";
            regionsTableQuery += "id INT AUTO_INCREMENT,";
            regionsTableQuery += "owner VARCHAR(255) NOT NULL,";
            regionsTableQuery += "created_at DATETIME NOT NULL,";
            regionsTableQuery += "priority INT NOT NULL,";
            regionsTableQuery += "radius INT NOT NULL,";
            regionsTableQuery += "center_x INT NOT NULL,";
            regionsTableQuery += "center_y INT NOT NULL,";
            regionsTableQuery += "center_z INT NOT NULL,";
            regionsTableQuery += "world VARCHAR(255) NOT NULL,";
            regionsTableQuery += "min_x INT NOT NULL,";
            regionsTableQuery += "min_y INT NOT NULL,";
            regionsTableQuery += "min_z INT NOT NULL,";
            regionsTableQuery += "max_x INT NOT NULL,";
            regionsTableQuery += "max_y INT NOT NULL,";
            regionsTableQuery += "max_z INT NOT NULL,";
            regionsTableQuery += "name VARCHAR(255) NOT NULL,";
            regionsTableQuery += "PRIMARY KEY (id));";

            statement.execute(regionsTableQuery);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
