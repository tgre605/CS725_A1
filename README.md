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

### LIST
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

### CDIR
CDIR must be followed by a directory that exists. It must be entered as though the user is starting at the root directory (ie. /src)
IF entered correctly, the following will print:
    ```
    CDIR sftp
    FROM SERVER: !Changed working dir to C:\...\CS725_A1\src/Server/sftp
    ```
 
If the directory does not exists, the following will print:
    ```
    CDIR sftp/test
    FROM SERVER: -Can't connect to directory because: C:\...\CS725_A1\src/Server/sftp/test is not a directory
    ```
 
### KILL
KILL must be followed by a filename that exists in the current directory. If entered correctly, the following will print:
    ```
    KILL test1.png
    FROM SERVER: +test1.png deleted
    ```

If the file does not exists, the following will print:
    ```
    KILL noExist.txt
    FROM SERVER: -Not deleted because file does not exist
    ```

### NAME
NAME must be followed by a filename that exists in the current directory. If entered correctly, the following will print:
    ```
    NAME test1.png
    FROM SERVER: +File exists
    ```

If the file does not exists, the following will print:
    ```
    NAME noExists.png
    FROM SERVER: -Can't find noExists.png
    ```
    
### DONE
DONE can be entered at any time and will close the connection between the client and server. Both sides will shut down. The following will print:
    ```
    DONE
    FROM SERVER: +Goodbye
    ```

### RETR
RETR must be followed by file that exists within currecnt directory. If entered correctly and in correct send mode for file requested, server will respond with:
    ```
    RETR testing.png
    <size of file in bytes>
    ```
    
User can then respond with SEND or STOP. SEND will result in the file being sent by the server and stored within the sftp directory on the client side and the following will print:
    ```
    SEND
    File testing.png was saved.
    ```
    
If user responds with STOP, the following will print:
    ```
    STOP
    FROM SERVER: +ok, RETR aborted
    ```

If incorrect type was selected, the following will print:
    ```
    RETR testing.png
    -Incorrect type selected
    ```

## STOR
STOR must be followed by {NEW | OLD | APP} and the name of the file to send from the client side
If NEW is selected, a new generation of the file will be saved on the server side with the filename having the addition of the current date/time. If the file does not exist yet, it will be created with the same name.
If old is selected, the file on the server side will be overwritten with the file from the client side. If the file does not exist yet, it will be created with the same name.
APP will append to the file on the remote side. If the file does not exist yet, it will be created with the same name.

Wehn STOR is called, the client side will check the file and send type to ensure it is correct. IF so, the server will check if the file exists already and tell the client. The client will then automatically tell the server the size of the file. If the server has enough space, the client will be notified. The client then automatically sends the file and the server tells the client it was saved.

Examples are shown below:

Correct send type:
    ```
    STOR NEW test1.png
    +Sending
    +File does not exist, will create new file. Sending SIZE 1873
    FROM SERVER: +ok, waiting for file
    FROM SERVER: +Saved C:\...\CS725_A1\src\Server\sftp\test1-20200901180508.png
    ```

Incorrect send type:
    ```
    STOR NEW test1.png
    -Incorrect type selected
    TYPE B
    FROM SERVER: +Using Binary
    STOR NEW test1.png
    +Sending
    +File does not exist, will create new file. Sending SIZE 1873
    FROM SERVER: +ok, waiting for file
    FROM SERVER: +Saved C:\...\CS725_A1\src\Server\sftp\test1-20200901180508.png
    ```
