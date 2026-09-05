1. Prerequisites

You need a JDK installed (17+; the project was built and tested on 21). Check with:

bash
java -version
javac -version 2. Compile

Unzip LibraryAutomationSystem-src.zip — you'll get a src/ folder. From the folder that contains src/:

Mac/Linux:

bash
find src -name "\*.java" > sources.txt
javac -d bin @sources.txt

Windows (PowerShell):

powershell
Get-ChildItem -Recurse -Filter \*.java src | ForEach-Object { $\_.FullName } > sources.txt
javac -d bin "@sources.txt"

This compiles everything into a bin/ folder.

3. Run
   bash
   java -cp bin com.library.api.ApiServer

You should see:

Library Automation System API running on http://localhost:8088
Serving front-end static files from: <path>/public

That second line is harmless — there's no public/ folder in this API-only package, so it just won't serve anything at /. The /api/... endpoints work regardless. The server pre-loads some demo data (3 members, 4 books, 2 active loans) on startup so you have something to query immediately. Leave this terminal running; open a new terminal/tab for the next step.

4. Connect to it

Any HTTP client works — curl, Postman, Insomnia, or a browser for GET requests.

Quick check in a browser: open http://localhost:8088/api/books — you should see a JSON array.

curl examples for every endpoint:

bash

# --- Members ---

curl http://localhost:8088/api/members
curl "http://localhost:8088/api/members?search=kasun"
curl -X POST http://localhost:8088/api/members -H "Content-Type: application/json" \
 -d '{"firstName":"Amal","lastName":"Perera","email":"amal@mail.com","phone":"0771112222"}'
curl -X PUT http://localhost:8088/api/members/1 -H "Content-Type: application/json" \
 -d '{"phone":"0779998888","email":"new@mail.com"}'
curl -X DELETE http://localhost:8088/api/members/1

# --- Books ---

curl http://localhost:8088/api/books
curl "http://localhost:8088/api/books?search=clean"
curl -X POST http://localhost:8088/api/books -H "Content-Type: application/json" \
 -d '{"isbn":"978-1234","title":"Refactoring","category":"Computer Science","author":"Martin Fowler","copies":2}'
curl -X PUT http://localhost:8088/api/books/1 -H "Content-Type: application/json" \
 -d '{"title":"Clean Code (2nd Ed)","category":"Computer Science","author":"Robert Martin"}'
curl -X DELETE http://localhost:8088/api/books/1

# --- Loans ---

curl http://localhost:8088/api/loans
curl "http://localhost:8088/api/loans?overdue=true"
curl -X POST http://localhost:8088/api/loans -H "Content-Type: application/json" \
 -d '{"memberId":1,"bookId":1}'
curl -X POST http://localhost:8088/api/loans/1/return

In Postman: set base URL http://localhost:8088, method + path as above, and for POST/PUT add header Content-Type: application/json with the same JSON body in the "raw" tab.

5. Stop the server

Ctrl+C in the terminal where it's running.

Troubleshooting
"Address already in use" — port 8088 is taken by something else. Either stop that process, or change PORT in ApiServer.java and recompile.
Compile errors about missing packages — make sure you ran javac from the directory containing src/, not from inside src/.
Connection refused from curl — the server isn't running, or you're hitting the wrong port.
