package dataAccess.mySQLDatabase;

import dataAccess.DatabaseManager;

public class MySQLUtil {

    public static void initializeDatabase() {
        try {
            DatabaseManager.createDatabase();
            try (var conn = DatabaseManager.getConnection()) {
                // Create User table
                var statement = """
                        CREATE TABLE IF NOT EXISTS User (
                            username VARCHAR(64) PRIMARY KEY,
                            password_hash VARCHAR(72) NOT NULL,
                            email VARCHAR(330) NOT NULL
                        )""";
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }

                // Create Auth table
                statement = """
                        CREATE TABLE IF NOT EXISTS Auth (
                            authToken VARCHAR(36) PRIMARY KEY,
                            username VARCHAR(64) NOT NULL,
                            FOREIGN KEY (username) REFERENCES User(username)
                        )""";
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }

                // Create Game table
                statement = """
                        CREATE TABLE IF NOT EXISTS Game (
                            game_id INT PRIMARY KEY AUTO_INCREMENT,
                            white_username VARCHAR(64),
                            black_username VARCHAR(64),
                            game_name VARCHAR(64),
                            game_data BLOB,
                            FOREIGN KEY (white_username) REFERENCES User(username),
                            FOREIGN KEY (black_username) REFERENCES User(username)
                        )""";
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void clearDatabase() {
        try {
            try (var conn = DatabaseManager.getConnection()) {
                var statement = "TRUNCATE TABLE Auth";
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
                statement = "TRUNCATE TABLE Game";
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
                statement = "TRUNCATE TABLE User";
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
