package Server;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class TCPServerInstance {
    private final Socket socket;
    private Boolean running = true;
    private Boolean userV = false;
    private Boolean accountV = false;
    private Boolean passwordV = false;
    private Boolean fileExists = false;
    Path currentRelativePath = Paths.get("");
    private final String root = currentRelativePath.toAbsolutePath().toString();
    static File ftp = FileSystems.getDefault().getPath("Server/sftp/").toFile().getAbsoluteFile();
    private String sendType;
    String[] accounts;
    String user;
    String password;
    String account;
    BufferedReader inFromClient;
    DataOutputStream outToClient;
    private String directory = root + "/Server";
    private Path pathToOldfile;
    private String existingFile;
    private boolean retrV;
    private String fileToRetr;
    private boolean cDirP = false;
    private boolean cDirA = false;
    private String directoryTemp;
    private String storMode;
    private String newFileS;
    private DataOutputStream binToClient;
    private DataInputStream binFromClient;

    public TCPServerInstance(Socket socket) {
        this.socket = socket;
    }

    public boolean runInstance() throws Exception {
        //initialise ascii/binary inputs/outputs
        inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outToClient = new DataOutputStream(socket.getOutputStream());
        binToClient = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        binFromClient = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        new File(ftp.toString()).mkdirs();
        sendToClient("+TGRE605 SFTP Service");
        while (running) {
            //Read client input and send input to mode function unless DONE is received
            String clientInputS = readFromClient(inFromClient);
            String[] clientInput = clientInputS.split(" ");
            if (clientInput[0].toUpperCase().equals("DONE")) {
                sendToClient("+Goodbye");
                running = false;
                socket.close();
                break;
            } else {
                mode(clientInput);
            }
        }
        return false;
    }

    //send input argument to correct function depending on argument
    public void mode(String[] modeArgs) throws Exception {
        switch (modeArgs[0].toUpperCase()) {
            case "USER" -> sendToClient(user(modeArgs[1]));
            case "ACCT" -> sendToClient(acct(modeArgs[1]));
            case "PASS" -> sendToClient(pass(modeArgs[1]));
            case "TYPE" -> type(modeArgs[1]);
            case "LIST" -> sendToClient(list(modeArgs[1]));
            case "CDIR" -> sendToClient(cdir(modeArgs[1]));
            case "KILL" -> sendToClient(kill(modeArgs[1]));
            case "NAME" -> sendToClient(name(modeArgs[1]));
            case "TOBE" -> sendToClient(tobe(modeArgs[1]));
            case "SEND" -> {
                //if send command is received, depending on if RETR has been called, execute send
                int resp = send();
                if (resp == 0) {
                } else if (resp == 1) {
                    sendToClient("No file selected");
                } else {
                    sendToClient("-Not found because: unauthorised, please sign in");
                }
            }
            case "RETR" -> sendToClient(retr(modeArgs[1]));
            case "STOP" -> {
                //if RETR has been called and then stopped, reset variables that allow send
                fileToRetr = null;
                retrV = false;
                sendToClient("+ok, RETR aborted");
            }
            case "STOR" -> stor(modeArgs);
            default -> sendToClient("-invalid Command");
        }
    }

    public String user(String userInput) throws Exception {
        boolean userExists = false;
        String[] lineAccounts = null;
        String linePassword = null;

        //read in file containing authentication details
        File authFile = new File(root + "/Server/authFile.txt");

        /*Read each line of auth file. If user matches user in auth file
         * save username, account and password to local variables.
         * If no account/password exists, assume verified user
         * if no matching user, tell client invalid input */
        BufferedReader reader = new BufferedReader(new FileReader(authFile));
        String line = reader.readLine();
        while (line != null) {
            String[] userInfo = line.split(" ");
            String lineUsername = userInfo[0];
            if (lineUsername.equals(userInput)) {
                userExists = true;
                if (userInfo.length == 2) {
                    if (!(userInfo[1] == null)) {
                        lineAccounts = userInfo[1].split(",");
                    }
                    passwordV = true;
                } else if (userInfo.length == 3) {
                    if (!(userInfo[1] == null)) {
                        lineAccounts = userInfo[1].split(",");
                    }
                    linePassword = userInfo[2];
                }
                if (userInfo.length == 1) {
                    accountV = true;
                    passwordV = true;
                } else if (userInfo.length == 2) {
                    accounts = lineAccounts;
                    password = null;
                    passwordV = true;
                } else if (userInfo.length == 3) {
                    assert userInfo[1] != null;
                    if (userInfo[1].isEmpty()) {
                        account = null;
                        accountV = true;
                    }
                    accounts = lineAccounts;
                    password = linePassword;
                }
                break;
            }
            line = reader.readLine();
        }
        if (!userExists) {
            return "Invalid Username";
        }
        userV = true;
        user = userInput;
        //If password/account exists, ask user to send password/account to be verified
        if (passwordV && accountV) {
            return "!" + userInput + " logged in";
        } else {
            if (accountV) {
                return "+User-id valid, send password";
            } else if (passwordV) {
                return "+User-id valid, send account";
            } else {
                return "+User-id valid, send account and password";
            }
        }
    }

    public String acct(String userInput) {
        /*If user exists, check account input if matches an account for that user
         * if matching, set variable account to user input*/
        if (userV) {
            for (String accountTest : accounts) {
                if (userInput.equals(accountTest)) {
                    account = userInput;
                    accountV = true;
                    break;
                }
            }
        }
        /*If attempting to change directory without being logged in
         * required log in to account, then immediately change directory*/
        if (passwordV && accountV) {
            if (cDirA) {
                String dir = changeDir();
                cDirA = false;
                return dir;
            }
            return "! Account valid, logged-in";
        } else if (accountV) {
            return "+Account valid, send password";
        } else {
            return "-Invalid account, try again";
        }
    }

    public String pass(String userInput) {
        if (userV) {
            //if user doesnt have password, set verified password to true
            if (password == null) {
                passwordV = true;
            }
            //if user input matches expected password, set verified password to true
            if (userInput.equals(password)) {
                passwordV = true;
            }
            /*If attempting to change directory without being logged in
             * required log in to password, then immediately change directory*/
            if (accountV && passwordV) {
                if (cDirP) {
                    String dir = changeDir();
                    cDirA = false;
                    return dir;
                }
                return "! logged in";
            } else if (passwordV) {
                return "+ Password ok, send account";
            }
        }
        return "-Wrong password, try again";
    }

    public void type(String userInput) throws Exception {
        //depending on user input, change made to ascii or binary for sending and receiving files
        switch (userInput) {
            case "A" -> {
                sendType = "A";
                sendToClient("+Using Ascii");
            }
            case "B" -> {
                sendType = "B";
                sendToClient("+Using Binary");
            }
            case "C" -> {
                sendType = "C";
                sendToClient("+Using Continuous");
            }
            default -> sendToClient("-Type not valid");
        }
    }

    public String list(String userInput) throws Exception {
        //if user send LIST F, only print file/directoy names on new lines
        if (userV) {
            if ("F".equals(userInput)) {
                StringBuilder response;
                DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(directory));
                response = new StringBuilder("+" + directory + "\r\n");
                for (Path filePath : stream) {
                    response.append(filePath.getFileName()).append("\r\n");
                }
                return response.toString();
            }
            //if user sends LIST V, print file/directory name, size, date of last modification and owner
            if ("V".equals(userInput)) {
                StringBuilder response;
                DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(directory));
                response = new StringBuilder("+" + directory + "\r\n");
                SimpleDateFormat DateFor = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
                for (Path filePath : stream) {
                    File file = new File(filePath.toString());
                    response.append(String.format("%6s", file.getName())).append(" | ");
                    response.append("Size: ").append(String.format("%6s", file.length() / 1000)).append("kBs").append(" | ");
                    response.append("Last modified: ").append(DateFor.format(new Date(file.lastModified()))).append(" | ");
                    FileOwnerAttributeView attr = Files.getFileAttributeView(file.toPath(), FileOwnerAttributeView.class);
                    response.append(String.format("Owner: " + "%6s", attr.getOwner().getName())).append("\r\n");
                }
                return response.toString();
            }
        }
        return "-Not found because: unauthorised, please sign in";
    }

    public String cdir(String userInput) {
        /*given correct input of real directory, change global vairable directory
         * to new directory name if user is logged in correctly */
        String cdDirectory = root + "/Server/" + userInput;
        if (userV && passwordV && accountV) {
            directoryTemp = cdDirectory;
            return changeDir();
        }
        //if use is not logged in, ask for password/account and immediately change directory if entered correctly
        if (userV) {
            cdDirectory = root + "/Server/" + userInput;
            File file = new File(cdDirectory);
            if (!file.isDirectory()) {
                return "-Can't connect to directory because: " + cdDirectory + " is not a directory";
            }
            directoryTemp = cdDirectory;
            if (!accountV && !passwordV) {
                cDirA = true;
                cDirP = true;
                return "+directory ok, send account/password";
            }
            if (!accountV) {
                cDirA = true;
                return "+directory ok, send account/password";
            }
            if (!passwordV) {
                cDirP = true;
                return "+directory ok, send account/password";
            }
            return "+directory ok, send account/password";
        }
        return "-Can't connect to directory because: unauthorised, please sign in";
    }

    //function for changing directory variable if directory exists
    private String changeDir() {
        File file = new File(directoryTemp);
        if (!file.isDirectory()) {
            return "-Can't connect to directory because: " + directoryTemp + " is not a directory";
        }
        directory = directoryTemp;
        directoryTemp = null;
        return "!Changed working dir to " + directory;
    }

    //if user is logged in, will deleted file if it exists in current directory
    public String kill(String userInput) throws Exception {
        if (userV) {
            Path pathToFile = new File(directory + "/" + userInput).toPath();
            try {
                Files.delete(pathToFile);
                return "+" + userInput + " deleted";
            } catch (NoSuchFileException e) {
                return "-Not deleted because file does not exist";
            }
        } else return "-Not deleted because: unauthorised, please sign in";
    }

    /*Checks to see if file exists in directory and if it does, set global variable
     * that shows that file exists. Set path to file that can be user in TOBE function */
    public String name(String userInput) {
        if (userV) {
            Path pathToOldFileTemp = new File(directory + "/" + userInput).toPath();
            if (Files.exists(pathToOldFileTemp)) {
                fileExists = true;
                existingFile = userInput;
                this.pathToOldfile = pathToOldFileTemp;
                return "+File exists";
            }
            return "-Can't find " + userInput;
        } else return "-Not found because: unauthorised, please sign in";
    }

    /*If NAME has been called correctly, user can input a desired name for the file.
     * If that name is not taken, it will be renamed*/
    public String tobe(String userInput) {
        if (userV && fileExists) {
            Path newFileName = new File(directory + "/" + userInput).toPath();
            if (Files.exists(newFileName)) {
                return "-File wasn't renamed because file already exists";
            }
            File oldFile = new File(String.valueOf(pathToOldfile));
            String fileType = oldFile.toString().split("\\.")[1];
            oldFile.renameTo(new File(directory + "/" + userInput + "." + fileType));
            fileExists = false;
            String response = "+" + existingFile + " renamed to " + userInput + "." + fileType;
            this.existingFile = null;
            this.pathToOldfile = null;
            return response;
        }
        if (!fileExists) {
            return "-File wasn't renamed because file does not exist";
        } else return "-Not found because: unauthorised, please sign in";

    }

    /* Given user input of file they want to retrieve, server will check
     * if file exists and if correct send type has been selected
     * If incorrect type, tell client to change mode
     * if correct type, set global value for file server will send in SEND command*/
    public String retr(String userInput) throws IOException {
        if (userV) {
            Path pathToFileTemp = new File(directory + "/" + userInput).toPath();
            if (Files.exists(pathToFileTemp)) {
                boolean binary = isBinary(new File(pathToFileTemp.toString()));
                if (binary) {
                    if (Objects.equals(sendType, "C") || Objects.equals(sendType, "B")) {
                    } else return "-Incorrect type selected";

                }
                if (!binary && !Objects.equals(sendType, "A")) {
                    return "-Incorrect type selected";
                }
                long fileSize = new File(String.valueOf(pathToFileTemp)).length();
                fileToRetr = userInput;
                retrV = true;
                return String.valueOf(fileSize);
            }
            return "-Can't find " + userInput;
        } else return "-Not found because: unauthorised, please sign in";
    }

    /*If RETR has been used correctly, file to retrieve will be set and global boolean
     * retrV will be set telling server it is ok to execute send command.*/
    public int send() throws Exception {
        if (userV && retrV) {
            Path pathToFile = new File(directory + "/" + fileToRetr).toPath();
            File file = new File(String.valueOf(pathToFile));
            byte[] bytes = new byte[(int) file.length()];
            fileToRetr = null;
            //If file is ascii type, use method bufStream
            if (Objects.equals(sendType, "A")) {
                bufStream(file, bytes, outToClient);
            } else {
                //if file is binary type, read in via FileInputStream and send byte by byte
                FileInputStream fileStream = new FileInputStream(file);
                int c;
                while ((c = fileStream.read()) >= 0) {
                    binToClient.write(c);
                }
                fileStream.close();
                binToClient.flush();
            }
            //set global variable fro retrV to false so RETR will have to be called again before SEND
            retrV = false;
            return 0;
        } else if (userV) {
            return 1;
        }
        return 2;
    }

    //if file is Ascii type, use BufferedInputStream to read in file and send byte by byte
    public static void bufStream(File file, byte[] bytes, DataOutputStream outToClient) throws IOException {
        BufferedInputStream bufferedStream = new BufferedInputStream(new FileInputStream(file));
        outToClient.flush();
        int p;
        while ((p = bufferedStream.read(bytes)) > 0) {
            outToClient.write(bytes, 0, p);
        }
        bufferedStream.close();
        outToClient.flush();
    }

    public void stor(String[] userInput) throws Exception {
        if (userInput.length != 3) {
            sendToClient("-Invalid input");
            return;
        }
        /*If user has input 3 argument for STOR command, check second argument to see how
         * they want the file stored via a switch case
         * If invalid STOR mode, send invalid to client*/
        if (userV) {
            int size;
            storMode = null;
            File file = new File(directory + "/" + userInput[2]);
            
            File directoryF = new File(directory);
            switch (userInput[1].toUpperCase()) {
                case "NEW":
                    if (file.isFile()) {
                        storMode = "NEW";
                        sendToClient("+File exists, will create new generation of file");
                    } else {
                        storMode = "NEWC";
                        sendToClient("+File does not exist, will create new file");
                    }
                    break;
                case "OLD":
                    if (file.isFile()) {
                        storMode = "OLD";
                        sendToClient("+Will write over old file");
                    } else {
                        storMode = "NEWC";
                        sendToClient("+Will create new file");
                    }
                    break;
                case "APP":
                    if (file.isFile()) {
                        storMode = "APP";
                        sendToClient("+Will append to file");
                    } else {
                        storMode = "NEWC";
                        sendToClient("+Will create file");
                    }
                    break;
                default:
                    sendToClient("-Invalid request");
                    return;
            }
            //Client will then send back the size of the file
            String[] resp = readFromClient(inFromClient).split(" ");

            while (true) {
                if (null == resp[0]) {
                    sendToClient("-Invalid Client Response. Awaiting SIZE. Send STOP to stop transfer.");
                } else {
                    if (resp[0].equals("SIZE")) {
                        size = Integer.parseInt(resp[1]);
                        //if enough free space, tell client ok to send file
                        if (directoryF.getFreeSpace() > size) {
                            sendToClient("+ok, waiting for file");
                            break;
                        } else {
                            sendToClient("-Couldn't save because not enough free space");
                            return;
                        }
                    } else {
                        sendToClient("-Invalid input, STOR aborted");
                        storMode = null;
                        return;
                    }

                }
            }
            //If STOR mode selected is NEW, create new extension to add to file name based on date
            if (Objects.equals(storMode, "NEW")) {
                SimpleDateFormat dateFor = new SimpleDateFormat("yyyyMMddHHmmss");
                String[] directoryFS = file.toString().split("\\.", 2);
                file = new File(directoryFS[0] + "-" + dateFor.format(new Date()) + "." + directoryFS[1]);
            }
            newFileS = file.toString();
            fileIntake(size);
        } else {
            sendToClient("-Not found because: unauthorised, please sign in");
        }

    }

    /*If send type selected is correct, file will be created on server side
     * with the name of the original file of new file name.
     * If incorrect type selected, user will be told to change type*/
    private void fileIntake(int size) throws IOException {
        File file = new File(newFileS);
        if (Objects.equals(sendType, "A")) {
            BufferedOutputStream bufferedStream = new BufferedOutputStream(new FileOutputStream(file, "APP".equals(storMode)));
            for (int i = 0; i < size; i++) {
                bufferedStream.write(inFromClient.read());
            }
            sendToClient("+Saved " + file);
        } else if (Objects.equals(sendType, "B")) {
            int e;
            int i = 0;
            byte[] bytes = new byte[(int) size];
            FileOutputStream fileOutputStream = new FileOutputStream(file, "APP".equals(storMode));
            while (i < size) {
                e = binFromClient.read(bytes);
                fileOutputStream.write(bytes, 0, e);
                i += e;
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            sendToClient("+Saved " + file);
        }
        else sendToClient("Incorrect send mode");
    }

    //Function for checking if a selected file is of binary type
    private boolean isBinary(File file) throws IOException {
        FileInputStream in;
        in = new FileInputStream(file);
        int size = in.available();
        if (size > 64) size = 64;
        byte[] data = new byte[size];
        in.read(data);
        in.close();

        int ascii = 0;
        int binary = 0;

        //iterate through bytes
        for (byte b : data) {
            //if byte is of binary type, return true
            if (b < 0x09) {
                return true;
            }

            //if ascii symbol exists, add to ascii counter
            if (b == 0x09 || b == 0x0A || b == 0x0C || b == 0x0D) {
                ascii++;
            } else if (b >= 0x20 && b <= 0x7E) {
                ascii++;
            } else {
                binary++;
            }
        }

        if (binary == 0) return false;

        return 100 * binary / (ascii + binary) > 95;

    }

    //method of outputting to client by appending \0 on end of line
    private void sendToClient(String message) throws IOException {
        outToClient.writeBytes(message + "\0");
    }

    /*method of reading from client by reading char by char and checking if \0 has come as the end of line
     * If end of line reached but not end of input keep reading*/
    private static String readFromClient(BufferedReader inFromClient) throws IOException {
        StringBuilder text = new StringBuilder();
        var character = 0;
        while (true) {
            character = inFromClient.read();
            if ((char) character == '\0' && text.length() > 0) {
                break;
            }
            if ((char) character != '\0') {
                text.append((char) character);
            }
        }
        return text.toString();
    }
}
