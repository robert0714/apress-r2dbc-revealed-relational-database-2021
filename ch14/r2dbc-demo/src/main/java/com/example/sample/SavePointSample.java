package com.example.sample;

import org.mariadb.r2dbc.api.MariadbConnection;
import org.mariadb.r2dbc.api.MariadbStatement;
import reactor.core.publisher.Mono;

/**
 * Listings 14-12 and 14-13: Demonstrates savepoint creation, optional rollback to savepoint, and commit.
 */
public class SavePointSample implements TransactionSample {

    private final boolean rollbackToSavepoint;

    public SavePointSample(boolean rollbackToSavepoint) {
        this.rollbackToSavepoint = rollbackToSavepoint;
    }

    @Override
    public String getName() {
        return "savepoint";
    }

    @Override
    public Mono<Void> execute(MariadbConnection connection) {
        MariadbStatement insertStatement = connection.createStatement(
                "INSERT INTO tasks (description) VALUES ('TASK X')");
        MariadbStatement deleteStatement = connection.createStatement(
                "DELETE FROM tasks WHERE id = 2");

        return connection.beginTransaction()
                .then(insertStatement.execute().then())
                .then(connection.createSavepoint("savepoint_1"))
                .then(deleteStatement.execute().then())
                .then(rollBackOrCommit(connection))
                .doOnSuccess(v -> System.out.println(
                        rollbackToSavepoint
                                ? "Rolled back to savepoint, then committed."
                                : "Committed without rollback to savepoint."));
    }

    private Mono<Void> rollBackOrCommit(MariadbConnection connection) {
        if (rollbackToSavepoint) {
            return connection.rollbackTransactionToSavepoint("savepoint_1")
                    .then(connection.commitTransaction());
        } else {
            return connection.commitTransaction();
        }
    }
}
