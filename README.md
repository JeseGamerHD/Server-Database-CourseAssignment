### What is this project?
This project was done as a programming course assignment so it is **not really meant to be used for anything other than viewing/reference**. Even with the setup instructions a part of this project will not function as I am not authorized to share the external weather service jar file. However, if you wish to do some tinkering the setup should get it to work mostly.

This project implements a basic HTTPS server with a small SQLite database. The idea is that "users" can register, post messages containing information about sightseeing locations and recieve posted messages.

- The server accepts POST requests from registered users that have a JSON attached and checks if the JSON contains required information.
- The POST requests contain a posting date / timestamp and the server converts that to a proper ISO 8601 format when storing and attaching it to messages.
- The server attached the poster's nickname to the message.
- The user's password is stored encrypted in the database.
- The server sends the posted messages in a JSON array when the user does a GET request.
- The external weather service part would let the users to attach weather information based on coordinates in their messages. When a message with weather attached is GET requested, the server would do an XML POST request to the external service getting latest weather info and attach it to the message.

**TL;DR**:
Basically this project deals with database programming, data formats, the server part of client-server applications and a little bit of concurrency as those were the theme of the course.


### Setup instructions
**Step 1**: 
Generate a self-signed certificate</br>
`keytool -genkey -alias alias -keyalg RSA -keystore keystore.jks -keysize 2048`

**Step 2**: 
Copy the keystore file into the project folder (where pom.xml and README.md are located)

**Step 3**:
Open the launch.json from .vscode folder (or create your own if not using vscode). Now replace the args with the keystore file name and the password you gave it.

### Usage instructions

First open the cmd at the project's location:

Registering a new user:</br>
`curl -k -d "@user.json" https://localhost:8001/registration -H "Content-Type: application/json"`

Sending a message (with and without coordinate information):</br>
`curl -k -d "@message.json" https://localhost:8001/info -H "Content-Type: application/json" -u name:pass`</br>
`curl -k -d "@message2.json" https://localhost:8001/info -H "Content-Type: application/json" -u name:pass`

Requesting the messages:</br>
`curl -k https://localhost:8001/info -H "Content-Type: application/json" -u name:pass`

Note that you can also format the json into the curl, but using a file is easier/cleaner.