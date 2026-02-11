package com.database.postgrescheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * PostgreSQL 数据库监控应用程序
 */
@SpringBootApplication
@EnableScheduling
public class PostgresCheckApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostgresCheckApplication.class, args);
    }
}
