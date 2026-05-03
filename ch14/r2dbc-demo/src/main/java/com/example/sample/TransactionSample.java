package com.example.sample;

import org.mariadb.r2dbc.api.MariadbConnection;
import reactor.core.publisher.Mono;

/**
 * Contract for all transaction demonstration samples.
 * Open/Closed Principle: add new samples without modifying existing code.
 * Dependency Inversion: App depends on this abstraction, not concrete implementations.
 */
public interface TransactionSample {

    String getName();

    Mono<Void> execute(MariadbConnection connection);
}
