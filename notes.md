# My Notes


ternary operator - `<if> ? <then> : <else>`

`@Override`  
`Object.equals(obj1, obj2)` and `Object.hash(args)`  
For nested array: `Arrays.deepEquals` and `Arrays.deepHashCode`

declare an array - `public Type[] arr = new Type[Size];`  
initalize a nested array - `int[][] narr = {{1, 2}, {3, 4}};`  
`int[] copiedArr = Arrays.copyOf(arr, arr.length);`  
`Arrays.fill(arr, value); // int[] array`

loops: `for (int i; i < 10; i++) {}`, `for(int value : array) {}`,   
`while () {}`, `do {} while ()`, `break`, `continue`

Use stringbuilders for concatenation:   
`StringBuilder sb = new StringBuilder();`  
`sb.append("string")`  
`String str = sb.toString();`

`String.format("string %s %d", "a string", 12)`
- **`%s`** for strings.
- **`%d`** for decimal integers.
- **`%f`** for floating-point numbers.
- **`%b`** for booleans.
- **`%c`** for characters.

```
String result = switch (expression) {
    case 1 -> {
        doStuff;
        yield "hi";
    }
    default -> "Error";
}
```

or just to execute code:

```
switch (expression) {
    case var1 -> {};
    case var2 -> {};
};
```

# Phase 2

| Component | Sub-Component | Description |
| --- | --- | --- |
| Chess Client |  | A terminal based program that allows a user to play a game of chess. This includes actions to login, create, and play games. The client exchanges messages over the network with the chess server. |
| Chess Server |  | A command line program that accepts network requests from the chess client to login, create, and play games. Users and games are stored in the database. The server also sends game play commands to the chess clients that are participating in a specific game. |
|  | Server | Receives network requests and deserializes them into service objects. Calls service methods to satisfy the requests. |
|  | Services | Processes the business logic for the application. This includes registering and logging in users, creating, listing, and playing chess games. Calls the data access methods to retrieve and persist application data data. |
|  | DataAccess | Provides methods that persistently store and retrieve the application data. |
| Database |  | Stores data persistently. |

### API
| Endpoint | Description |
| --- | --- |
| Clear | Clears the database. Removes all users, games, and authTokens. |
| Register | Register a new user. |
| Login | Logs in an existing user (returns a new authToken). |
| Logout | Logs out the user represented by the provided authToken. |
| List Games | Verifies the provided authToken and gives a list of all games. |
| Create Game | Verifies the provided authToken and creates a new game. |
| Join Game | Verifies the provided authToken. Checks that the specified game exists, and if a color is specified, adds the caller as the requested color to the game. If no color is specified the user is joined as an observer. This request is idempotent. |

### Data Model Classes
| Object | Description |
| --- | --- |
| UserData | A user is registered and authenticated as a player or observer in the application. |
| AuthData | The association of a username and an authorization token that represents that the user has previously been authorized to use the application. |
| GameData | The information about the state of a game. This includes the players, the board, and the current state of the game. |

| Object   | Example                                                                                                 |
|----------|---------------------------------------------------------------------------------------------------------|
| UserData | `{"username": "joe", "password": "secret", "email": "j@cow.com"}`                                       |
| AuthData | `{"authToken": "zyz343sfze", "username": "joe"}`                                                        |
| GameData | `{"gameID": 3, "whiteUsername": "joe", "blackUsername": "sally", "gameName": "blitz", "game": "serialized data"}` |
