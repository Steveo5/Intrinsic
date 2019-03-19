package com.hotmail.intrinsic.storage;

import com.hotmail.intrinsic.Intrinsic;
import com.hotmail.intrinsic.Region;
import com.hotmail.intrinsic.RegionType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MysqlConnector {

    private Connection connection;
    private String host, database, username, password;
    private int port;
    private Intrinsic plugin;

    public MysqlConnector(Intrinsic plugin, String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database + "?useSSL=false";
        this.username = username;
        this.password = password;
        this.plugin = plugin;

        this.initialiseTables();
    }

    public void saveRegion(Region region) {

        final String id = region.getId();
        final String owner = region.getOwner().getUniqueId().toString();

        java.util.Date dt = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        final String currentTime = sdf.format(dt);
        final int priority = 1, radius = region.getType().getRadius();

        // Region location, min and max bounds
        Location loc = region.getLocation(), min = region.getBounds()[0], max = region.getBounds()[1];

        final int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        final int minX = min.getBlockX(), minY = min.getBlockY(), minZ = min.getBlockZ();
        final int maxX = max.getBlockX(), maxY = max.getBlockY(), maxZ = max.getBlockZ();
        final String world = loc.getWorld().getUID().toString();

        // Serialise the item
        YamlConfiguration itemConfig = new YamlConfiguration();
        itemConfig.set("item", region.getType().getBlock());

        final String serializedItem = itemConfig.saveToString();
        final String typeName = region.getType().getName();

        // Insert our new region ASYNC
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            try {
                openConnection();

                String insertRegion = "INSERT INTO regions(id, owner, created_at, priority) " +
                        "VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE owner=?, priority=?";

                this.saveRegionLocation(id, x, y, z, minX, minY, minZ, maxX, maxY, maxZ, world);
                this.saveRegionType(id, typeName, radius, serializedItem);

                /** Create the region **/
                PreparedStatement insertRegionStatement = connection.prepareStatement(insertRegion);

                insertRegionStatement.setString(1, id);
                insertRegionStatement.setString(2, owner);
                insertRegionStatement.setTimestamp(3, Timestamp.valueOf(currentTime));
                insertRegionStatement.setInt(4, priority);
                // Duplicate key statements
                insertRegionStatement.setString(5, owner);
                insertRegionStatement.setInt(6, priority);

                insertRegionStatement.execute();

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Save a regions locations to database, this includes
     * center location, min and max locations
     * @param id
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private void saveRegionLocation(String id,
                                    int x,
                                    int y,
                                    int z,
                                    int minX,
                                    int minY,
                                    int minZ,
                                    int maxX,
                                    int maxY,
                                    int maxZ,
                                    String world
    ) throws SQLException {
        String insertLocation = "INSERT INTO locations(id, x, y, z, min_x, min_y, min_z, max_x, max_y, max_z, world) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE x=?, y=?, z=?, min_x=?, min_y=?, min_z=?, " +
                "max_x=?, max_y=?, max_z=?, world=?;";

        /** Create the location **/
        PreparedStatement insertLocationStatement = connection.prepareStatement(insertLocation);
        insertLocationStatement.setString(1, id);
        insertLocationStatement.setString(2, String.valueOf(x));
        insertLocationStatement.setString(3, String.valueOf(y));
        insertLocationStatement.setString(4, String.valueOf(z));
        insertLocationStatement.setString(5, String.valueOf(minX));
        insertLocationStatement.setString(6, String.valueOf(minY));
        insertLocationStatement.setString(7, String.valueOf(minZ));
        insertLocationStatement.setString(8, String.valueOf(maxX));
        insertLocationStatement.setString(9, String.valueOf(maxY));
        insertLocationStatement.setString(10, String.valueOf(maxZ));
        insertLocationStatement.setString(11, world);
        // Duplicate keys
        insertLocationStatement.setString(12, String.valueOf(x));
        insertLocationStatement.setString(13, String.valueOf(y));
        insertLocationStatement.setString(14, String.valueOf(z));
        insertLocationStatement.setString(15, String.valueOf(minX));
        insertLocationStatement.setString(16, String.valueOf(minY));
        insertLocationStatement.setString(17, String.valueOf(minZ));
        insertLocationStatement.setString(18, String.valueOf(maxX));
        insertLocationStatement.setString(19, String.valueOf(maxY));
        insertLocationStatement.setString(20, String.valueOf(maxZ));
        insertLocationStatement.setString(21, world);

        insertLocationStatement.execute();
    }

    /**
     * Save a region type to database
     *
     * The block is serialized during execution
     * @param id
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private void saveRegionType(String id, String name, int radius, String item) throws ClassNotFoundException, SQLException {
        String insertLocation = "INSERT INTO types(id, name, radius, block) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE" +
                " name=?, radius=?, block=?";

        /// later...
/*        YamlConfiguration restoreConfig = new YamlConfiguration();
        restoreConfig.loadFromString(serialized);
        ItemStack restoredItem = restoreConfig.getItemStack("item");*/

        /** Create the type **/
        PreparedStatement insertLocationStatement = connection.prepareStatement(insertLocation);
        insertLocationStatement.setString(1, id);
        insertLocationStatement.setString(2, name);
        insertLocationStatement.setInt(3, radius);
        insertLocationStatement.setString(4, item);
        // Duplicate keys
        insertLocationStatement.setString(5, name);
        insertLocationStatement.setInt(6, radius);
        insertLocationStatement.setString(7, item);

        insertLocationStatement.execute();
    }

    public List<Region> getIntersecting(Location location) {
        List<Region> intersecting = new ArrayList<Region>();

        try {
            openConnection();
            Statement statement = connection.createStatement();
            String query = "SELECT * FROM locations ";
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
            String locationsTableQuery = "CREATE TABLE IF NOT EXISTS locations (";
            locationsTableQuery += "id VARCHAR(255),";
            locationsTableQuery += "x INT NOT NULL,";
            locationsTableQuery += "y INT NOT NULL,";
            locationsTableQuery += "z INT NOT NULL,";
            locationsTableQuery += "min_x INT NOT NULL,";
            locationsTableQuery += "min_y INT NOT NULL,";
            locationsTableQuery += "min_z INT NOT NULL,";
            locationsTableQuery += "max_x INT NOT NULL,";
            locationsTableQuery += "max_y INT NOT NULL,";
            locationsTableQuery += "max_z INT NOT NULL,";
            locationsTableQuery += "world TEXT NOT NULL,";
            locationsTableQuery += "PRIMARY KEY (id));";

            String typesTableQuery = "CREATE TABLE IF NOT EXISTS types (";
            typesTableQuery += "id VARCHAR(255) NOT NULL,";
            typesTableQuery += "name VARCHAR(255) NOT NULL,";
            typesTableQuery += "radius INT NOT NULL,";
            typesTableQuery += "block text NOT NULL,";
            typesTableQuery += "PRIMARY KEY (id));";

            String regionsTableQuery = "CREATE TABLE IF NOT EXISTS regions (";
            regionsTableQuery += "id VARCHAR(255),";
            regionsTableQuery += "owner VARCHAR(255) NOT NULL,";
            regionsTableQuery += "created_at DATETIME NOT NULL,";
            regionsTableQuery += "priority INT NOT NULL,";
            regionsTableQuery += "radius INT NOT NULL,";
            regionsTableQuery += "location_id VARCHAR(255) NOT NULL,";
            regionsTableQuery += "name VARCHAR(255) NOT NULL,";
            regionsTableQuery += "PRIMARY KEY (id),";
            regionsTableQuery += "FOREIGN KEY (location_id) REFERENCES locations(id));";

            statement.execute(locationsTableQuery);
            statement.execute(typesTableQuery);
            statement.execute(regionsTableQuery);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
