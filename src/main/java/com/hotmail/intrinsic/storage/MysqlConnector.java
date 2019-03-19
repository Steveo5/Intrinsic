package com.hotmail.intrinsic.storage;

import com.hotmail.intrinsic.Intrinsic;
import com.hotmail.intrinsic.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MysqlConnector {

    private final ConnectionPoolManager pool;

    private Intrinsic plugin;

    public MysqlConnector(Intrinsic plugin) {
        this.plugin = plugin;
        pool = new ConnectionPoolManager(plugin);
        this.initialiseTables();
    }

    public void onDisable() {
        pool.closePool();
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

            Connection connection = null;
            PreparedStatement insertRegionStatement = null;

            try {

                connection = pool.getConnection();

                String insertRegion = "INSERT INTO regions(id, owner, created_at, priority) " +
                        "VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE owner=?, priority=?";

                this.saveRegionLocation(id, x, y, z, minX, minY, minZ, maxX, maxY, maxZ, world);
                this.saveRegionType(id, typeName, radius, serializedItem);

                /** Create the region **/
                insertRegionStatement = pool.getConnection().prepareStatement(insertRegion);

                insertRegionStatement.setString(1, id);
                insertRegionStatement.setString(2, owner);
                insertRegionStatement.setTimestamp(3, Timestamp.valueOf(currentTime));
                insertRegionStatement.setInt(4, priority);
                // Duplicate key statements
                insertRegionStatement.setString(5, owner);
                insertRegionStatement.setInt(6, priority);

                insertRegionStatement.execute();

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                pool.close(connection, insertRegionStatement, null);
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

        Connection connection = pool.getConnection();

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

        pool.close(connection, insertLocationStatement, null);
    }

    /**
     * Save a region type to database
     *
     * The block is serialized during execution
     * @param id
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private void saveRegionType(String id, String name, int radius, String item) throws SQLException {
        String insertLocation = "INSERT INTO types(id, name, radius, block) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE" +
                " name=?, radius=?, block=?";

        /// later...
/*        YamlConfiguration restoreConfig = new YamlConfiguration();
        restoreConfig.loadFromString(serialized);
        ItemStack restoredItem = restoreConfig.getItemStack("item");*/

        Connection connection = pool.getConnection();

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

        pool.close(connection, insertLocationStatement, null);
    }

    public List<Region> getIntersecting(Location location, Runnable callback) {
        List<Region> intersecting = new ArrayList<Region>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            connection = pool.getConnection();

            String query = "SELECT * FROM locations ";
            query += "INNER JOIN regions ON locations.id = regions.id ";
            query += "INNER JOIN types ON locations.id = types.id ";
            query += "WHERE world LIKE ? AND ? > min_x AND ? < max_x AND ? > min_y AND ? < max_y AND ? > min_z AND ? < max_z;";

            statement = connection.prepareStatement(query);
            statement.setString(1, location.getWorld().getUID().toString());
            statement.setInt(2, location.getBlockX());
            statement.setInt(3, location.getBlockX());
            statement.setInt(4, location.getBlockY());
            statement.setInt(5, location.getBlockY());
            statement.setInt(6, location.getBlockZ());
            statement.setInt(7, location.getBlockZ());

            rs = statement.executeQuery();

            if(rs.next()) {
                do {
                    System.out.println(rs.getString(1));
                    System.out.println(rs.getString(2));
                    System.out.println(rs.getString(3));
                    System.out.println(rs.getString(4));
                    System.out.println(rs.getString(5));
                    System.out.println(rs.getString(6));
                    System.out.println(rs.getString(7));
                    System.out.println(rs.getString(8));
                    System.out.println(rs.getString(9));
                    System.out.println(rs.getString(10));
                    System.out.println(rs.getString(11));
                    System.out.println(rs.getString(12));
                    System.out.println(rs.getString(13));
                    System.out.println(rs.getString(14));
                    System.out.println(rs.getString(15));
                    System.out.println(rs.getString(16));
                    System.out.println(rs.getString(17));
                    System.out.println(rs.getString(18));
                    System.out.println(rs.getString(19));

                } while (rs.next());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(connection, statement, rs);
        }

        return intersecting;
    }

    public boolean testConnection() {
        try {
            pool.getConnection();
            return true;
        } catch (SQLException e) { }

        return false;
    }

    public void initialiseTables() {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = pool.getConnection();
            statement = connection.createStatement();
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
            regionsTableQuery += "PRIMARY KEY (id));";

            statement.execute(locationsTableQuery);
            statement.execute(typesTableQuery);
            statement.execute(regionsTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(connection, null, null);
        }
    }

}
