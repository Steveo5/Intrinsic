package com.hotmail.intrinsic.storage;

import com.hotmail.intrinsic.Intrinsic;
import com.hotmail.intrinsic.Region;
import com.hotmail.intrinsic.RegionType;
import org.bukkit.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        final int priority = region.getPriority();
        int radius = region.getType().getRadius();

        Location loc = region.getLocation();

        final int x = loc.getBlockX();
        final int y = loc.getBlockY();
        final int z = loc.getBlockZ();
        final int minX = region.getBounds()[0].getX();
        final int minZ = region.getBounds()[0].getZ();
        final int maxX = region.getBounds()[1].getX();
        final int maxZ = region.getBounds()[1].getZ();
        final String world = loc.getWorld().getUID().toString();
        final List<UUID> whitelist = region.getWhitelist();

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

                this.saveRegionLocation(id, x, y, z, minX, minZ, maxX, maxZ, world);
                this.saveRegionType(id, typeName, radius, serializedItem);
                this.saveWhitelist(id, whitelist);

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
    private void saveRegionLocation(String id,
                                    int x,
                                    int y,
                                    int z,
                                    int minX,
                                    int minZ,
                                    int maxX,
                                    int maxZ,
                                    String world
    ) throws SQLException {
        String insertLocation = "INSERT INTO chunks(id, x, y, z, min_x, min_z, max_x, max_z, world) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);";

        Connection connection = pool.getConnection();

        /** Create the location **/
        PreparedStatement insertLocationStatement = connection.prepareStatement(insertLocation);
        insertLocationStatement.setString(1, id);
        insertLocationStatement.setString(2, String.valueOf(x));
        insertLocationStatement.setString(3, String.valueOf(y));
        insertLocationStatement.setString(4, String.valueOf(z));
        insertLocationStatement.setString(5, String.valueOf(minX));
        insertLocationStatement.setString(6, String.valueOf(minZ));
        insertLocationStatement.setString(7, String.valueOf(maxX));
        insertLocationStatement.setString(8, String.valueOf(maxZ));
        insertLocationStatement.setString(9, world);

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

    public void saveWhitelist(String regionId, List<UUID> whitelist) {
        String sql = "INSERT IGNORE INTO whitelist (uuid, region_id) VALUES (?, ?);";

        try {
            Connection connection = pool.getConnection();

            for (UUID uuid : whitelist) {
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, uuid.toString());
                statement.setString(2, regionId);
                statement.execute();
                statement.close();
            }
            pool.close(connection, null, null);
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get regions intersecting a chunk and populate a callback ASync
     * @param chunk
     * @param callback
     */
    public void getIntersecting(Chunk chunk, int radius, IntersectingCallback callback) {

        int x = chunk.getX(), z = chunk.getZ();
        int minX = x - radius, maxX = x + radius;
        int minZ = z - radius, maxZ = z + radius;

        String world = chunk.getWorld().getUID().toString();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet rs = null;

            try {
                connection = pool.getConnection();

                String query = "SELECT * FROM chunks ";
                query += "INNER JOIN regions ON chunks.id = regions.id ";
                query += "INNER JOIN types ON chunks.id = types.id ";
                query += "WHERE (world LIKE ? AND ? >= min_x AND ? <= max_x AND ? >= min_z AND ? <= max_z) ";
                query += "OR (world LIKE ? AND min_x >= ? AND max_x <= ? AND min_Z >= ? AND max_z <= ?);";

                statement = connection.prepareStatement(query);
                statement.setString(1, world);
                statement.setInt(2, minX);
                statement.setInt(3, maxX);
                statement.setInt(4, minZ);
                statement.setInt(5, maxZ);
                statement.setString(6, world);
                statement.setInt(7, minX);
                statement.setInt(8, maxX);
                statement.setInt(9, minZ);
                statement.setInt(10, maxZ);

                final ResultSet results = statement.executeQuery();

                this.getIntersecting(results, callback);

            } catch (SQLException e) {
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
    private void getIntersecting(ResultSet results, IntersectingCallback callback) throws SQLException {

        List<HashMap<String, String>> regionStringList = new ArrayList<>();

        while(results.next()) {

            HashMap<String, String> values = new HashMap<>();

            values.put("chunkId", results.getString(1));
            values.put("centerX", results.getString(2));
            values.put("centerY", results.getString(3));
            values.put("centerZ", results.getString(4));
            values.put("chunkWorld", results.getString(9));
            values.put("regionId", results.getString(10));
            values.put("owner", results.getString(11));
            values.put("createdAt", results.getString(12));
            values.put("priority", results.getString(13));
            values.put("typeName", results.getString(15));
            values.put("radius", results.getString(16));
            values.put("typeItem", results.getString(17));

            regionStringList.add(values);

        }


        // Assemble our Bukkit stuff back in the main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            for(HashMap<String, String> regionStrings : regionStringList) {
                try {
                    Region region = getRegionFromHashmap(regionStrings);
                    callback.rs.add(region);
                } catch (InvalidConfigurationException e) {
                    e.printStackTrace();
                }
            }

            callback.run();
        });

    }

    /**
     * Get a region from a list of hashmap strings, values supplied must match the required
     * values of a region such as radius, typeItem etc
     * @param map
     * @return
     * @throws InvalidConfigurationException
     */
    private Region getRegionFromHashmap(HashMap<String, String> map) throws InvalidConfigurationException {
        World w = Bukkit.getWorld(UUID.fromString(map.get("chunkWorld")));
        int x = Integer.valueOf(map.get("centerX"));
        int y = Integer.valueOf(map.get("centerY"));
        int z = Integer.valueOf(map.get("centerZ"));
        Location loc = new Location(w, x, y, z);
        OfflinePlayer offlineOwner = Bukkit.getOfflinePlayer(UUID.fromString(map.get("owner")));

        // Deserialize the RegionType item
        YamlConfiguration restoreConfig = new YamlConfiguration();
        restoreConfig.loadFromString(map.get("typeItem"));
        final ItemStack typeItem = restoreConfig.getItemStack("item");

        RegionType regionType = new RegionType(map.get("typeName"), typeItem, Integer.valueOf(map.get("radius")));
        List<UUID> whitelist = getWhitelist(map.get("regionId"));
        int priority = Integer.valueOf(map.get("priority"));

        // Finally make our region
        return new Region(regionType, loc, offlineOwner, priority, whitelist);

    }

    /**
     * Get all whitelisted UUIDs for a specific region id
     * @param regionId
     * @return
     */
    private List<UUID> getWhitelist(String regionId) {
        List<UUID> whitelist = new ArrayList<>();

        PreparedStatement statement = null;
        Connection connection = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM whitelist WHERE region_id LIKE ?";

        try {
            connection = pool.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, regionId);

            rs = statement.executeQuery();

            while(rs.next()) {
                UUID uuid = UUID.fromString(rs.getString(1));
                whitelist.add(uuid);
            }
        } catch(SQLException e) {
            return whitelist;
        } finally {
            pool.close(connection, statement, rs);
        }

        return whitelist;
    }

    /**
     * Remove a region from the database
     * @param r
     */
    public void destroyRegion(Region r) {
        String id = r.getId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            String deleteRegionQuery = "DELETE FROM regions WHERE id=?;";

            Connection connection = null;
            PreparedStatement statement = null;

            try {
                this.destroyChunk(id);
                this.destroyTypes(id);

                connection = pool.getConnection();
                statement = connection.prepareStatement(deleteRegionQuery);
                statement.setString(1, id);
                statement.execute();

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                pool.close(connection, statement, null);
            }
        });
    }

    /**
     * Delete a chunk by id
     * @param id
     */
    private void destroyChunk(String id) throws SQLException {
        String deleteChunkQuery = "DELETE FROM chunks WHERE id=?";

        Connection connection = pool.getConnection();
        PreparedStatement statement = connection.prepareStatement(deleteChunkQuery);

        statement.setString(1, id);
        statement.execute();
        pool.close(connection, statement, null);
    }

    /**
     * Destroy region types by id
     * @param id
     */
    private void destroyTypes(String id) throws SQLException {
        String deleteTypesQuery = "DELETE FROM types WHERE id=?";

        Connection connection = pool.getConnection();
        PreparedStatement statement = connection.prepareStatement(deleteTypesQuery);

        statement.setString(1, id);
        statement.execute();
        pool.close(connection, statement, null);
    }

    public boolean testConnection() {
        try {
            pool.getConnection();
            return true;
        } catch (SQLException e) { return false; }
    }

    public void initialiseTables() {
        Connection connection = null;
        Statement statement;
        try {
            connection = pool.getConnection();
            statement = connection.createStatement();
            String locationsTableQuery = "CREATE TABLE IF NOT EXISTS chunks (";
            locationsTableQuery += "id VARCHAR(255),";
            locationsTableQuery += "x INT NOT NULL,";
            locationsTableQuery += "y INT NOT NULL,";
            locationsTableQuery += "z INT NOT NULL,";
            locationsTableQuery += "min_x INT NOT NULL,";
            locationsTableQuery += "min_z INT NOT NULL,";
            locationsTableQuery += "max_x INT NOT NULL,";
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

            String whitelistTableQuery = "CREATE TABLE IF NOT EXISTS whitelist (";
            whitelistTableQuery += "uuid VARCHAR(255) NOT NULL,";
            whitelistTableQuery += "region_id VARCHAR(255) NOT NULL,";
            whitelistTableQuery += "PRIMARY KEY (uuid, region_id));";

            statement.execute(locationsTableQuery);
            statement.execute(typesTableQuery);
            statement.execute(regionsTableQuery);
            statement.execute(whitelistTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(connection, null, null);
        }
    }

}
