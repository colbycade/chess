# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`     | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

### Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
```
# Sequence Diagram
actor Client
participant Server
participant RegistrationService
participant DataAccess
database db

group #navy Registration #white
Client -> Server: [POST] /user\n{username, password, email}
Server -> RegistrationService: register(username, password, email)
RegistrationService -> DataAccess: getUser(username)
DataAccess -> db: SELECT username from user
DataAccess --> RegistrationService: null
RegistrationService -> DataAccess: createUser(username, password)
DataAccess -> db: INSERT username, password, email INTO user
RegistrationService -> DataAccess: createAuth(username)
DataAccess -> db: INSERT username, authToken INTO auth
DataAccess --> RegistrationService: authToken
RegistrationService --> Server: authToken
Server --> Client: 200\n{authToken}
end

group #orange Login #white
Client -> Server: [POST] /session\n{username, password}
Server -> RegistrationService: login(username, password)
RegistrationService -> DataAccess: getUser(username)
DataAccess -> db:SELECT password from user
DataAccess --> RegistrationService: password
RegistrationService -> DataAccess: createAuth(username)
DataAccess -> db:INSERT username, authToken INTO auth
DataAccess --> RegistrationService: authToken
RegistrationService --> Server: authToken
Server --> Client: 200\n{username, authToken}
end


group #green Logout #white
Client -> Server: [DELETE] /session\nauthToken
Server -> RegistrationService: logout(authToken)
RegistrationService -> DataAccess: deleteAuth(authToken)
DataAccess -> db: DELETE username, authToken FROM auth
DataAccess --> RegistrationService: success
RegistrationService --> Server: success
Server --> Client: 200
end


group #red List Games #white
Client -> Server: [GET] /game\nauthToken
Server -> RegistrationService: verifyToken(authToken)
RegistrationService -> DataAccess:getAuth(authToken)
DataAccess -> db: SELECT username, authToken FROM auth
DataAccess --> RegistrationService: username, authToken
RegistrationService --> Server: AuthData
Server -> DataAccess:listGames()
DataAccess -> db: SELECT * FROM game
DataAccess -> SELECT games FROM games
DataAccess --> Server: gamesList
Server --> Client: 200\n{games}
end


group #purple Create Game #white
Client -> Server: [POST] /game\nauthToken\n{gameName}
Server -> RegistrationService: verifyToken(authToken)
RegistrationService -> DataAccess:getAuth(authToken)
DataAccess -> db: SELECT username, authToken FROM auth
DataAccess --> RegistrationService: username, authToken
RegistrationService --> Server: AuthData
Server -> DataAccess:createGame(gameName)
DataAccess -> db:INSERT gameName INTO game
DataAccess --> Server: gameID
Server --> Client:200\n{gameID}
end

group #yellow Join Game #black
Client -> Server: [PUT] /game\nauthToken\n{ClientColor, gameID}
Server -> RegistrationService: verifyToken(authToken)
RegistrationService -> DataAccess:getAuth(authToken)
DataAccess -> db: SELECT username, authToken FROM auth
DataAccess --> RegistrationService: username, authToken
RegistrationService --> Server: AuthData
Server -> DataAccess:updateGame(gameID, username)
DataAccess -> db:UPDATE game
Server --> Client:200
end

group #gray Clear application #white
Client -> Server: [DELETE] /db
Server -> DataAccess:clearApp()
DataAccess -> db:DELETE * FROM user
DataAccess -> db:DELETE * FROM auth
DataAccess -> db:DELETE * FROM game
Server --> Client:200
end
```
