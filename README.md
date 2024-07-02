# ♕ BYU CS 240 Chess

This is a project created for the BYU CS 240 (Advanced Programming Concepts) course.  

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and 
WebSocket, database persistence, unit testing, serialization, concurrency, and security.  

It would not be too difficult to create a front-end web app and
[deploy the server](https://github.com/softwareconstruction240/softwareconstruction/blob/main/instruction/aws-chess-server/aws-chess-server.md) to enable a complete web-based multiplayer chess game.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Basic Use
1. Install all dependencies as specified in the `pom.xml` files.
2. Run the server using the `client/src/main/java/Main` class.
3. Run multiple concurrent clients using the `server/src/main/java/server/Main` class.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
|----------------------------|-------------------------------------------------|
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

# API
| Endpoint                | HTTP Method | Path      | Header    | Body                        | Returns                                            |
|-------------------------|-------------|-----------|-----------|-----------------------------|----------------------------------------------------|
| Register                | POST        | /user     |           | {username, password, email} | authToken                                          |
| Login                   | POST        | /session  |           | {username, password}        | authToken                                          |
| Logout                  | DELETE      | /session  | authToken |                             | {}                                                 |
| List Games              | GET         | /game     | authToken |                             | {gameID, whiteUsername, blackUsername, gameName}[] |
| Create Game             | POST        | /game     | authToken | {gameName}                  | gameID                                             |
| Join Game               | PUT         | /game     | authToken | {ClientColor, gameID}       | {}                                                 |
| Clear Application       | DELETE      | /db       |           |                             | {}                                                 |

## Sequence Diagram
See the complete sequence diagram [here](https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWOZVYSgM536HHCkARYGGABBECE5cAJsOAAjYBxQxp8zJgDmUCAFdsMAMRpgVAJ4wASik1IOYKMKQQ0RgO4ALJGBSZEqUgBaAD4WakoALhgAbQAFAHkyABUAXRgAel1lKAAdNABvLMoTAFsUABoYXA4ON2hpSpQS4CQEAF9MCnDYELC2SQ4oqBs7HygACiKoUoqqpVr6xubWgEpO1nYOGF6hEXEBwZhNFDAAVWzJ7Jm13bEJKW3QtSiyAFEAGVe4JJgpmZgAGY6Eq-bKYW77B6BXpdfqcKJoXQIBDraibR4wCH3GpREDDYQoc6US7FYBlSrVBZQaQ3OSQmoY54wACSADk3pYfn8ybNKXVqUsWggWaykvFQbRYeidnTsYc8SgCaJdGAPCTpjzaXs5Yz5FE2RyuVceZVgCqPEkIABrdAisUwM2q8Gyg7bGEbAZRR0eW6ouFbaGhWGRB3my02tB+yhu0L+dBgKIAJgADMm8vlveH0B10NINNo9AZDNB+McYO8ILZXIZPN5fHGgkHWCG4olUhllDVnGh09zyXMavzpB1gz0m2jPTAEJWkGh1TMKfMh2spQMMViDlFjmcLn2UFq7q7es83p9vgOqdJAcCJVBndqj+6J-CYESoL7V5x1y6pLj8T5lVVedNXvQ8oSePUDVeTlbwXUNVSzVw2Xtb1QPpAMn39KJAJ9OQ-WlTCQ0za10CjHpegbBMYBTNMCj3U0wxItAczQPMtB0fQjG0FBbQrTQ9GYGsvB8PxkHjDFRyiaIBA+V4kleNJ0k7DhuzyYiIzIiSPRfad+JVcZ1PQFdtIDUIN1-VQUAQE4UBwgzGIjA90N1KIZM+eTYJNeCLSYmAADFLHiABZby0J1QM+k2CIOF0OV8LXCLJJiuLRxjeAxIwJNU0wXMNHYwsjGGK93lGGAAHEeS2IS61EgJmEIqApLKuTFM0Hk1Ic0jUswqKp1GCqyg4eyEKY4zn1MzEfxxbc7MMtAnPCiCXlk896O8xD-MCkLUPMhlEpM7DzQ-EzvwfX9kHsAbOHGBajyW8gVp+AAqTbgqOHkwruh6zx+NrBtekK-s4T6oR6ycgduaIUnir99u6KSgeZARKlrHw3zg+QEFAK10a8oHWR5FIoc0iLKKy2j8iBjgWLYgtOMMbBdCgbBrPgf8VCu9xhPrDL6vHGhGpiBJkla9qTE6nsCnxnkR2bMdIsnBUCSu8Zpf7Oaxv9U6wOmk5Zol27wNUPVvq+H4cNuAHQt2jD+d6i28M-CabYiJWfBVtX9xBvalqgmDPbtcUge922FZfK7jvGtLJPesokZJijeYiGj00RgQafzDiizMKzpzcGAACkIFncqeSMTHsdq8SGqk2JTnbdIgY6kaI3TSi4AgacoEqNPZe6LTxqiAArYu0BV9vO+gHueSRhiW6MmHnamwYZvNYafMckOXNN88HZEK2duX6ODpgGA9+ARftfQiJtw9meBENn3jYiU8zdLspLYCt6gZgLe4aw9+KBI5axlGdHE+hZDux5OMCOchH4TWeKcWIAhRAeQhnIEUsdfA22PoPZKBxL7-0iPgqQCdYxJxojlVimcCqGG0MACwiBFSwGANgFmhAnAuC5jVSiA8BZSTcnJBSGQ1Akztp6EA1k8CiDYTdQhZll4RGkFZGyohkQ4SGvAlygiPIvS-ttc08jJpgMGMo6yAFkRXU0X-JaOjXgwD0VtLBRiXZmNUciN81icHHj1HYhxVsph-zBvCEhNRCE11CVwbqic6rJ2yrmIAA). The code used is provided below:

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


