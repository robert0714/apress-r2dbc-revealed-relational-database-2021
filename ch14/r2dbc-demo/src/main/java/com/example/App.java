package com.example;

import java.util.LinkedHashMap;
import java.util.Map;

import org.mariadb.r2dbc.api.MariadbConnection;

import com.example.sample.*;

/**
 * Chapter 14 - Transaction Management Samples.
 *
 * Usage: java com.example.App [sample-name]
 *
 * Available samples: reset, commit, rollback, imperative, declarative, savepoint
 * Default (no args): runs "reset" sample.
 */
public class App {

    public static void main(String[] args) {
        Map<String, TransactionSample> samples = registerSamples();

        String sampleName = args.length > 0 ? args[0] : "reset";
        TransactionSample sample = samples.get(sampleName);

        if (sample == null) {
            System.out.println("Unknown sample: " + sampleName);
            System.out.println("Available: " + samples.keySet());
            return;
        }

        ConnectionProvider provider = new ConnectionProvider();
        MariadbConnection connection = null;

        try {
            connection = provider.getConnection();
            System.out.println("=== Executing: " + sample.getName() + " ===");
            sample.execute(connection).block();
            System.out.println("=== Done ===");
        } catch (Exception e) {
            System.err.println("Error executing sample [" + sampleName + "]: " + e.getMessage());
            e.printStackTrace();
        } finally {
            provider.closeConnection(connection);
        }
    }

    private static Map<String, TransactionSample> registerSamples() {
        Map<String, TransactionSample> samples = new LinkedHashMap<>();
        samples.put("reset", new ResetTaskTableSample());
        samples.put("commit", new CommitTransactionSample());
        samples.put("rollback", new RollbackTransactionSample());
        samples.put("imperative", new ImperativeTransactionSample(true));
        samples.put("declarative", new DeclarativeTransactionSample());
        samples.put("savepoint", new SavePointSample(false));
        return samples;
    }
}
