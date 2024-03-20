# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## API
Register - POST /user  
Login - POST /session  
Logout - DELETE /session  
List Games - GET /game  
Create Game - POST /game  
Join Game - PUT /game  
Clear Application - DELETE /db  

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

# Sequence Diagram
```
actor Client
participant Server
participant Services
participant DataAccess
database db

group #navy Registration #white
Client -> Server: [POST] /user\n{username, password, email}
Server -> Services: register(username, password, email)
Services -> DataAccess: getUser(username)
DataAccess -> db: SELECT username from user
DataAccess --> Services: null
Services -> DataAccess: createUser(username, password)
DataAccess -> db: INSERT username, password, email INTO user
Services -> DataAccess: createAuth(username)
DataAccess -> db: INSERT username, authToken INTO auth
DataAccess --> Services: authData
Services --> Server: authToken
Server --> Client: 200\n{authToken}
end

group #orange Login #white
Client -> Server: [POST] /session\n{username, password}
Server -> Services: login(username, password)
Services -> DataAccess: getUser(username)
DataAccess -> db:SELECT password from user
DataAccess --> Services: UserData
Services -> DataAccess: createAuth(username)
DataAccess -> db:INSERT username, authToken INTO auth
DataAccess --> Services: AuthData
Services --> Server: authToken
Server --> Client: 200\n{username, authToken}
end

group #green Logout #white
Client -> Server: [DELETE] /session\nauthToken
Server -> Services: logout(authToken)
Services -> DataAccess: deleteAuth(authToken)
DataAccess -> db: DELETE username, authToken FROM auth
DataAccess --> Services:success
Services --> Server:success
Server --> Client: 200
end


group #red List Games #white
Client -> Server: [GET] /game\nauthToken
Server -> Services: listGames(authToken)
Services -> DataAccess:getAuth(authToken)
DataAccess -> db: SELECT username, authToken FROM auth
DataAccess --> Services: AuthData
Services -> DataAccess:listGames()
DataAccess -> db: SELECT * FROM game
DataAccess -> SELECT games FROM games
DataAccess --> Services: gameData[]
Services --> Server: [gameID, whiteUsername, blackUsername, gameName][]
Server --> Client: 200\n{games}
end

group #purple Create Game #white
Client -> Server: [POST] /game\nauthToken\n{gameName}
Server -> Services: createGame(gameName, authToken)
Services -> DataAccess:getAuth(authToken)
DataAccess -> db: SELECT AuthData FROM auth
DataAccess --> Services: AuthData
Services -> DataAccess:createGame(gameName)
DataAccess -> db:INSERT gameName INTO game
DataAccess --> Services: GameData
Services --> Server: gameID
Server --> Client:200\n{gameID}
end

group #yellow Join Game #black
Client -> Server: [PUT] /game\nauthToken\n{ClientColor, gameID}
Server -> Services: joinGame(ClientColor, gameID, authToken)
Services -> DataAccess:getAuth(authToken)
DataAccess -> db: SELECT AuthData FROM auth
DataAccess --> Services:  AuthData
Services -> DataAccess:getGame(gameID)
DataAccess -> db:SELECT GameData FROM game 
DataAccess --> Services: GameData
Services -> DataAccess:updateGame(GameData)
DataAccess -> db:UPDATE gameData IN game
DataAccess --> Services:success
Services --> Server:success
Server --> Client:200
end

group #gray Clear application #white
Client -> Server: [DELETE] /db
Server -> Services:clearApp()
Services -> DataAccess:deleteAllAuths()
DataAccess -> db:DELETE * FROM auth
Services -> DataAccess:deleteAllGames()
DataAccess -> db:DELETE * FROM game
Services -> DataAccess:deleteAllUsers()
DataAccess -> db:DELETE * FROM user
DataAccess --> Services:success
Services --> Server:success
Server --> Client:200
end
```


