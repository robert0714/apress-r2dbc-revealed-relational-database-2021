package com.example.sample;

import org.mariadb.r2dbc.api.MariadbConnection;
import org.mariadb.r2dbc.api.MariadbStatement;
import reactor.core.publisher.Mono;

/**
 * Listing 14-1: Reset the tasks table to a known state.
 */
public class ResetTaskTableSample implements TransactionSample {

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public Mono<Void> execute(MariadbConnection connection) {
        MariadbStatement statement = connection.createStatement(
                "TRUNCATE TABLE tasks; INSERT INTO tasks (description) VALUES ('Task A'), ('Task B'), ('Task C');");

        return statement.execute()
                .doOnNext(result -> System.out.println("Task table reset"))
                .then();
    }
}
