# CS725_A1

## Table of Contents
1. [List of Components](#list-of-components)
    1. [TCPServer](#TCPServer)
    2. [TCPClient](#TCPClient)
    3. [Authentication file](#authentication-file)
2. [How to setup TCPServer and run](#how-to-setup-TCPServer)
3. [How to setup TCPClient and run](#how-to-setup-TCPClient)
4. [Tests](#command-guide)
    1. [USER, ACCT and PASS Commands](#user-acct-and-pass-commands)
    2. [TYPE Command](#type-command)
    3. [LIST Command](#list-command)
    4. [CDIR Command](#cdir-command)
    5. [KILL Command](#kill-command)
    6. [NAME Command](#name-command)
    7. [DONE Command](#done-command)
    8. [RETR Command](#retr-command)
    9. [STOR Command](#stor-command)
    
## List of Components

### TCPServer
Thic class has the welcome socket code and handshakes with incoming client. It then send back a conneciton socket and starts up the server instance for use with designated port number.

### TCPClient
This class sends commands from client to server. When it is started it send handshake to server and receives connection socket for use with commands.

### Authentication file
This file holds the authentication data for logging into the server. The data for each user is stored on a single line separated by spaces and multiple accounts for a single user are split by a comma. Examples would be:
   ``` 
   user1  
   user2 account2 
   user3  password3
   user4 account4 password4
   user5 account5a,account5b password5
   //Take note of spaces after user2 with an account and no password, and user3 with a password and no account
   ```
   
## How to set up TCPServer and Run
Open terminal and navigate to CS725_A1/src directory

Enter: javac ./Server/*.java

Followed by: java Server.TCPServer

## How to set up TCPClient and run
Server must be running first following instructions above
Open terminal and navigate to CS725_A1/src directory

Enter: javac ./Client/TCPClient.java

Followed by: java Client.TCPClient
