## Phase 5
REPL class - `new REPL(server, database);`
- Read Eval Print Loop
- read std in
- eval(http_input) -> output
- print(output)
- loop 
HTTP Server class - contains server facade which simplifies interacting with server through http requests and responses
Evaluator class - `new Eval(HTTPServer);`
Printer class - `new Printer();`
Main driver class - configuration of server/database

Tests call server facade


server facade:
makeRequest (method, path, request, response)
writeBody(request, httpURLConnection http)
- http.addRequestProperty("Content-Type", "application/json")
readBody(httpURLConnection, response)