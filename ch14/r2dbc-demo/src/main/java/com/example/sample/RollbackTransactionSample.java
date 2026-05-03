package com.example.sample;

import org.mariadb.r2dbc.api.MariadbConnection;
import org.mariadb.r2dbc.api.MariadbStatement;
import reactor.core.publisher.Mono;

/**
 * Listing 14-7: Demonstrates transaction rollback.
 * Deletes data within a transaction, then rolls back, verifying data is unchanged.
 */
public class RollbackTransactionSample implements TransactionSample {

    @Override
    public String getName() {
        return "rollback";
    }

    @Override
    public Mono<Void> execute(MariadbConnection connection) {
        return connection.beginTransaction()
                .then(executeDeleteAndSelect(connection))
                .then(connection.rollbackTransaction())
                .then(verifyAfterRollback(connection));
    }

    private Mono<Void> executeDeleteAndSelect(MariadbConnection connection) {
        MariadbStatement statement = connection.createStatement(
                "DELETE FROM tasks WHERE description = 'New Task X';SELECT * FROM tasks");

        return statement.execute()
                .flatMap(result -> result.map((row, metadata) ->
                        String.format("[In Txn] %s - %s", row.get("id"), row.get("description"))))
                .doOnNext(System.out::println)
                .then();
    }

    private Mono<Void> verifyAfterRollback(MariadbConnection connection) {
        MariadbStatement statement = connection.createStatement("SELECT * FROM tasks");

        return statement.execute()
                .flatMap(result -> result.map((row, metadata) ->
                        String.format("[After Rollback] %s - %s", row.get("id"), row.get("description"))))
                .doOnNext(System.out::println)
                .then();
    }
}
