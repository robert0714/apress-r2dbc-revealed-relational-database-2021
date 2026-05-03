package com.example.sample;

import org.mariadb.r2dbc.api.MariadbConnection;
import org.mariadb.r2dbc.api.MariadbStatement;
import reactor.core.publisher.Mono;

/**
 * Listing 14-9 (declarative style): Full reactive chain with error handling via onErrorResume.
 * No blocking calls — pure reactive pipeline.
 */
public class DeclarativeTransactionSample implements TransactionSample {

    @Override
    public String getName() {
        return "declarative";
    }

    @Override
    public Mono<Void> execute(MariadbConnection connection) {
        MariadbStatement statement = connection.createStatement(
                "DELETE FROM tasks;INSERT INTO tasks (description) VALUES ('New Task D');SELECT * FROM tasks");

        return connection.beginTransaction()
                .then(statement.execute()
                        .flatMap(result -> result.map((row, metadata) ->
                                String.format("%s - %s", row.get("id"), row.get("description"))))
                        .doOnNext(System.out::println)
                        .then())
                .then(connection.commitTransaction())
                .onErrorResume(e -> {
                    System.out.println("Error encountered, rolling back: " + e.getMessage());
                    return connection.rollbackTransaction();
                });
    }
}
