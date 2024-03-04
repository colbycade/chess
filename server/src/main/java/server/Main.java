package server;

import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import dataAccess.UserDAO;
import dataAccess.inMemoryDatabase.MemoryAuthDAO;
import dataAccess.inMemoryDatabase.MemoryGameDAO;
import dataAccess.inMemoryDatabase.MemoryUserDAO;
import dataAccess.mySQLDatabase.MySQLAuthDAO;
import dataAccess.mySQLDatabase.MySQLGameDAO;
import dataAccess.mySQLDatabase.MySQLUserDAO;

public class Main {
    public static void main(String[] args) {
        // Get desired database type from system property or default to mysql
        String daoType = System.getProperty("userDaoType", "mysql");
        AuthDAO authDAO;
        GameDAO gameDAO;
        UserDAO userDAO;

        // Initialize DAOs based on the daoType
        switch (daoType) {
            case "mysql" -> {
                authDAO = new MySQLAuthDAO();
                gameDAO = new MySQLGameDAO();
                userDAO = new MySQLUserDAO();
            }
            case "memory" -> {
                authDAO = MemoryAuthDAO.getInstance();
                gameDAO = MemoryGameDAO.getInstance();
                userDAO = MemoryUserDAO.getInstance();
            }
            default -> throw new IllegalStateException("Unexpected value: " + daoType);
        }


        try {
            var port = 8080;
            if (args.length >= 1) {
                port = Integer.parseInt(args[0]);
            }

            var server = new Server(authDAO, gameDAO, userDAO);
            server.run(port);
            port = server.port();
            System.out.printf("Server started on port %d%n", port);
        } catch (Throwable ex) {
            System.out.printf("Unable to start server: %s%n", ex.getMessage());
        }
    }
}