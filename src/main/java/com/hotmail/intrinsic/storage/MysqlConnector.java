package com.hotmail.intrinsic.storage;

import com.hotmail.intrinsic.Intrinsic;
import com.hotmail.intrinsic.Region;
import com.hotmail.intrinsic.RegionType;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.UUID;

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

        Chunk center = region.getCenter();

        final int x = center.getX(),
                z = center.getZ(),
                minX = region.getBounds()[0].getX(),
                minZ = region.getBounds()[0].getZ(),
                maxX = region.getBounds()[1].getX(),
                maxZ = region.getBounds()[1].getZ();
        final String world = center.getWorld().getUID().toString();

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

                this.saveRegionLocation(x, z, minX, minZ, maxX, maxZ, world);
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
     * Save a regions chunk to database, this includes
     * center only
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private void saveRegionLocation(int x,
                                    int z,
                                    int minX,
                                    int minZ,
                                    int maxX,
                                    int maxZ,
                                    String world
    ) throws SQLException {
        String insertLocation = "INSERT INTO locations(id, x, z, min_x, min_z, max_z, max_z owner, world) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);";
        String id = x + "" + z + "" + world;

        Connection connection = pool.getConnection();

        /** Create the location **/
        PreparedStatement insertLocationStatement = connection.prepareStatement(insertLocation);
        insertLocationStatement.setString(1, id);
        insertLocationStatement.setString(2, String.valueOf(x));
        insertLocationStatement.setString(3, String.valueOf(z));
        insertLocationStatement.setString(4, String.valueOf(minX));
        insertLocationStatement.setString(5, String.valueOf(minZ));
        insertLocationStatement.setString(6, String.valueOf(maxX));
        insertLocationStatement.setString(7, String.valueOf(maxZ));
        insertLocationStatement.setString(8, world);

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

    /**
     * Get regions intersecting a chunk and populate a callback ASync
     * @param chunk
     * @param callback
     */
    public void getIntersecting(Chunk chunk, IntersectingCallback callback) {

        int x = chunk.getX(), z = chunk.getZ();
        String world = chunk.getWorld().getUID().toString();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet rs = null;

            try {
                connection = pool.getConnection();

                String query = "SELECT * FROM chunks ";
                query += "INNER JOIN regions ON chunks.id = regions.id ";
                query += "INNER JOIN types ON locations.id = types.id ";
                query += "WHERE world LIKE ? AND ? > min_x AND ? < max_x AND ? > min_z AND ? < max_z;";

                statement = connection.prepareStatement(query);
                statement.setString(1, world);
                statement.setInt(2, x);
                statement.setInt(3, x);
                statement.setInt(4, z);
                statement.setInt(5, z);

                final ResultSet results = statement.executeQuery();

                this.getIntersecting(results, callback);

            } catch (SQLException e) {
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            } finally {
                pool.close(connection, statement, rs);
            }
        });
    }

    /**
     * Get intersecting regions and populate a callback based on a result set
     * @param results
     * @param callback
     * @throws SQLException
     * @throws InvalidConfigurationException
     */
    private void getIntersecting(ResultSet results, IntersectingCallback callback) throws SQLException, InvalidConfigurationException {
        while(results.next()) {

            final boolean last = results.last();

            final String chunkId = results.getString(1);
            final int centerX = results.getInt(2), centerZ = results.getInt(3);
            final String chunkWorld = results.getString(4);
            // Skip ID as chunkId is the same
            final String owner = results.getString(6);
            final String createdAt = results.getString(7);
            final int priority = results.getInt(8);
            // Skip ID as typeId is the same
            final String typeName = results.getString(10);
            final int radius = results.getInt(11);

            // Deserialize the RegionType item
            YamlConfiguration restoreConfig = new YamlConfiguration();
            restoreConfig.loadFromString(results.getString(12));
            final ItemStack typeItem = restoreConfig.getItemStack("item");

            // Assemble our Bukkit stuff back in the main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                Chunk center = Bukkit.getWorld(UUID.fromString(chunkWorld)).getChunkAt(centerX, centerZ);
                OfflinePlayer offlineOwner = Bukkit.getOfflinePlayer(UUID.fromString(owner));

                RegionType regionType = new RegionType(typeName, typeItem, radius);

                // Finally make our region
                Region region = new Region(regionType, center, offlineOwner, priority);
                callback.regions.add(region);

                if(last) callback.run();
            });

        }
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
            String locationsTableQuery = "CREATE TABLE IF NOT EXISTS chunks (";
            locationsTableQuery += "id VARCHAR(255),";
            locationsTableQuery += "x INT NOT NULL,";
            locationsTableQuery += "z INT NOT NULL,";
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
