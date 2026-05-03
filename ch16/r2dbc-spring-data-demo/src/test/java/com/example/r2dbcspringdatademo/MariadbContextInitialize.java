package com.example.r2dbcspringdatademo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.mariadb.MariaDBContainer;

import java.util.Map;


class MariadbContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(MariadbContextInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        final MariaDBContainer mariadbContainer = new MariaDBContainer("mariadb:12.2-ubi");
//                    .withCopyFileToContainer(MountableFile.forClasspathResource("init.sql"), "/docker-entrypoint-initdb.d/init.sql");
        mariadbContainer.start();
        log.info(" container.getFirstMappedPort():: {}", mariadbContainer.getFirstMappedPort());

        ctx.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent) {
                mariadbContainer.stop();
            }
        });

        ctx.getEnvironment().getPropertySources()
                .addLast(
                        new MapPropertySource(
                                "r2dbc",
                                Map.of("r2dbc.host", mariadbContainer.getHost(),
                                        "r2dbc.port", mariadbContainer.getFirstMappedPort(),
                                        "r2dbc.databaseName", mariadbContainer.getDatabaseName(),
                                        "r2dbc.username", mariadbContainer.getUsername(),
                                        "r2dbc.password",  mariadbContainer.getPassword()
                                )
                        )
                );
    }
}