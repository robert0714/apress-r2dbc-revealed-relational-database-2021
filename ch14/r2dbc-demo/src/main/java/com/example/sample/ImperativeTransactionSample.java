package com.example.sample;

import java.sql.SQLException;

import org.mariadb.r2dbc.api.MariadbConnection;
import org.mariadb.r2dbc.api.MariadbStatement;
import reactor.core.publisher.Mono;

/**
 * Listing 14-9 (imperative style): Demonstrates commit on success, rollback on exception.
 * Intentionally uses blocking (.block()) style for pedagogical comparison with declarative approach.
 */
public class ImperativeTransactionSample implements TransactionSample {

    private final boolean simulateException;

    public ImperativeTransactionSample(boolean simulateException) {
        this.simulateException = simulateException;
    }

    @Override
    public String getName() {
        return "imperative";
    }

    @Override
    public Mono<Void> execute(MariadbConnection connection) {
        return Mono.fromRunnable(() -> executeBlocking(connection));
    }

    private void executeBlocking(MariadbConnection connection) {
        try {
            connection.beginTransaction().block();

            MariadbStatement statement = connection.createStatement(
                    "DELETE FROM tasks;INSERT INTO tasks (description) VALUES ('New Task D');SELECT * FROM tasks");

            statement.execute()
                    .flatMap(result -> result.map((row, metadata) ->
                            String.format("%s - %s", row.get("id"), row.get("description"))))
                    .doOnNext(System.out::println)
                    .blockLast();

            if (simulateException) {
                throw new SQLException("Simulated SQL Exception");
            }

            connection.commitTransaction().block();
            System.out.println("Transaction committed successfully.");
        } catch (SQLException e) {
            connection.rollbackTransaction().block();
            System.out.println("Transaction rolled back due to: " + e.getMessage());
        }
    }
}
