# CS725_A1

## Table of Contents
1. [List of Components](#list-of-components)
    1. [TCPServer](#TCPServer)
    2. [TCPClient](#TCPClient)
    3. [Authentication file](#authentication-file)
2. [How to setup TCPServer and run](#How-to-set-up-TCPServer-and-Run)
3. [How to setup TCPClient and run](#How-to-set-up-TCPClient-and-Run)
4. [Tests](#command-guide)
    1. [USER Command](#user-command)
    2. [PASS Command](#user-command)
    3. [ACCT Command](#pass-command)
    4. [TYPE Command](#type-command)
    5. [LIST Command](#list-command)
    6. [CDIR Command](#cdir-command)
    7. [KILL Command](#kill-command)
    8. [NAME Command](#name-command)
    9. [DONE Command](#done-command)
    10. [RETR Command](#retr-command)
    11. [STOR Command](#stor-command)
    
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

## Tests
The following gives examples of how the system should operate depending on input from the user

### USER
Attempting to sign in to a user should result in the following:
    ```
    USER userID
    FROM SERVER: +User-id valid, send account and password
    ```
    
If no password or account, it will skip straight to logged in.
If password or account is expected the following should print:
    ```
    USER userID
    FROM SERVER: +User-id valid, send account and password
    ```
    
If unknown user entered, the following should print:
    ```
    USER notExist
    FROM SERVER: Invalid Username
    ```

### PASS
If user has been entered and has a password associated with it, after entering password, the following should print:
    ```
    PASS password1
    FROM SERVER: + Password ok, send account
    ```

If no account is expected, will skill straight to logged in

### PASS
If user has been entered and has a password associated with it, after entering password, the following should print:
    ```
    ACCT account1
    FROM SERVER: +Account valid, send password
    ```
    
If no password is expected, will skill straight to logged in

### TYPE
TYPE must be followed by an A, B or C. If this is input correctly, the following will print:
    ```
    TYPE B
    FROM SERVER: +Using Binary
    ```
If input incorrectly, the following will print:
    ```
    TYPE D
    FROM SERVER: -Type not valid
    ```

The following commands can only be used after the user has logged in successfully. If user send invalid input, server will respond with negative response saying invalid input

### List
LIST must be followed by F or V. If this is input correctly, the following will print
If LIST F, only the file/directory names of the current directory will print for example:
    ```
    LIST F
    FROM SERVER: +C:\...\CS725_A1\src/Server/sftp
    test1-20200901150929.png
    testing.png
    ```
    
If LIST V, extra details of each file will be printed:
    ```
    LIST V
    FROM SERVER: +C:\...\CS725_A1\src/Server/sftp
    test1-20200901150929.png | Size:      1kBs | Last modified: Tue, 01 Sep 2020 15:09:29 NZST | Owner: DESKTOP-LPVUB0N\thoma
    testing.png | Size:      1kBs | Last modified: Tue, 01 Sep 2020 14:53:26 NZST | Owner: DESKTOP-LPVUB0N\thoma
    ```

