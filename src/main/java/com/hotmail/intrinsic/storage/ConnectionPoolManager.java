package com.hotmail.intrinsic.storage;

import com.hotmail.intrinsic.Intrinsic;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectionPoolManager {

    private final Intrinsic plugin;

    private String hostname;
    private int port;
    private String database;
    private String username;
    private String password;

    private HikariDataSource dataSource;

    private int minimumConnections;
    private int maximumConnections;
    private long connectionTimeout;
    private String testQuery;


    public ConnectionPoolManager(Intrinsic plugin) {
        this.plugin = plugin;
        init();
        setupPool();
    }

    private void init() {
        this.hostname = plugin.getConfig().getString("storage.mysql.host");
        this.port = plugin.getConfig().getInt("storage.mysql.port");
        this.database = plugin.getConfig().getString("storage.mysql.database") + "?useSSL=false";
        this.username = plugin.getConfig().getString("storage.mysql.username");
        this.password = plugin.getConfig().getString("storage.mysql.password");

        minimumConnections = 5;
        maximumConnections = 10;
        connectionTimeout = 5000;
        testQuery = "SELECT version();";
    }

    private void setupPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(
                "jdbc:mysql://" +
                        hostname +
                        ":" +
                        port +
                        "/" +
                        database
        );
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setUsername(username);
        config.setPassword(password);
        config.setMinimumIdle(minimumConnections);
        config.setMaximumPoolSize(maximumConnections);
        config.setConnectionTimeout(connectionTimeout);
        config.setConnectionTestQuery(testQuery);
        dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close(Connection conn, PreparedStatement ps, ResultSet res) {
        if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
        if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
        if (res != null) try { res.close(); } catch (SQLException ignored) {}
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }


}
