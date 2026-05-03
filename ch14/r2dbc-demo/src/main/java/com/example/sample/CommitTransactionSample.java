package com.example.sample;

import org.mariadb.r2dbc.api.MariadbConnection;
import org.mariadb.r2dbc.api.MariadbStatement;
import reactor.core.publisher.Mono;

/**
 * Listings 14-4 and 14-5: Imperative transaction commit example.
 * Demonstrates beginTransaction → execute → commitTransaction → verify.
 */
public class CommitTransactionSample implements TransactionSample {

    @Override
    public String getName() {
        return "commit";
    }

    @Override
    public Mono<Void> execute(MariadbConnection connection) {
        return connection.beginTransaction()
                .then(executeInsertAndSelect(connection))
                .then(connection.commitTransaction())
                .then(verifyAfterCommit(connection));
    }

    private Mono<Void> executeInsertAndSelect(MariadbConnection connection) {
        MariadbStatement statement = connection.createStatement(
                "INSERT INTO tasks (description) VALUES ('New Task X');SELECT * FROM tasks");

        return statement.execute()
                .flatMap(result -> result.map((row, metadata) ->
                        String.format("%s - %s", row.get("id"), row.get("description"))))
                .doOnNext(System.out::println)
                .then();
    }

    private Mono<Void> verifyAfterCommit(MariadbConnection connection) {
        MariadbStatement statement = connection.createStatement("SELECT * FROM tasks");

        return statement.execute()
                .flatMap(result -> result.map((row, metadata) ->
                        String.format("[After Commit] %s - %s", row.get("id"), row.get("description"))))
                .doOnNext(System.out::println)
                .then();
    }
}
