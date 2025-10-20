package server;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.inmemorydb.MemoryAuthDAO;
import dataaccess.inmemorydb.MemoryGameDAO;
import dataaccess.inmemorydb.MemoryUserDAO;
import dataaccess.mysqldb.MySQLAuthDAO;
import dataaccess.mysqldb.MySQLGameDAO;
import dataaccess.mysqldb.MySQLUserDAO;

public class Main {
    public static void main(String[] args) {
        // Get desired database type from system property or default to mysql
        // When running, provide as VM option: -DuserDaoType=memory or -DuserDaoType=mysql
        String daoType = System.getProperty("DaoType", "mysql");
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