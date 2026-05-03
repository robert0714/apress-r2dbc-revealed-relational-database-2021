package com.example;

import org.mariadb.r2dbc.MariadbConnectionConfiguration;
import org.mariadb.r2dbc.MariadbConnectionFactory;
import org.mariadb.r2dbc.api.MariadbConnection;
import reactor.core.publisher.Mono;

/**
 * Centralized connection lifecycle management.
 * Single Responsibility: only handles connection creation and teardown.
 */
public class ConnectionProvider {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 3306;
    private static final String USERNAME = "app_user";
    private static final String PASSWORD = "Password123!";
    private static final String DATABASE = "todo";

    private final MariadbConnectionFactory connectionFactory;

    public ConnectionProvider() {
        MariadbConnectionConfiguration config = MariadbConnectionConfiguration.builder()
                .host(HOST)
                .port(PORT)
                .username(USERNAME)
                .password(PASSWORD)
                .database(DATABASE)
                .allowMultiQueries(true)
                .build();
        this.connectionFactory = new MariadbConnectionFactory(config);
    }

    public MariadbConnection getConnection() {
        return connectionFactory.create().block();
    }

    public void closeConnection(MariadbConnection connection) {
        if (connection != null) {
            Mono.from(connection.close()).block();
        }
    }
}
