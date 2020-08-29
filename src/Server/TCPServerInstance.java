package Server;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TCPServerInstance{
    private final Socket socket;
    private Boolean running = true;
    private Boolean userV = false;
    private Boolean accountV = false;
    private Boolean passwordV = false;
    private Boolean fileExists = false;
    static File ftp = FileSystems.getDefault().getPath("src/Server/sftp/").toFile().getAbsoluteFile();
    private String sendType;
    String[] accounts;
    String user;
    String password;
    String account;
    BufferedReader inFromClient;
    DataOutputStream outToClient;
    private String directory = "";
    private Path pathToOldfile;
    private String existingFile;
    private boolean retrV;
    private String fileToRetr;
    private boolean cDirP = false;
    private boolean cDirA = false;
    private String directoryTemp;
    private String storMode;

    TCPServerInstance(Socket socket){
        this.socket = socket;
    }
    public void runInstance() throws Exception {
        inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outToClient = new DataOutputStream(socket.getOutputStream());
        new File(ftp.toString()).mkdirs();
        while (running){
            try {
                String[] clientInput = inFromClient.readLine().split(" ");
                if (clientInput[0].equals("DONE")) {
                    sendToClient("+(the message may be charge/accounting info)");
                    running = false;
                    socket.close();
                    break;
                } else {
                    mode(clientInput);
                }
            } catch (Exception ignored){}
        }
    }

    public void mode(String[] modeArgs) throws Exception {
        switch (modeArgs[0].toUpperCase()){
            case "USER":
                sendToClient(user(modeArgs[1]));
                break;
            case "ACCT":
                sendToClient(acct(modeArgs[1]));
                break;
            case "PASS":
                sendToClient(pass(modeArgs[1]));
                break;
            case "TYPE":
                type(modeArgs[1]);
                break;
            case "LIST":
                sendToClient(list(modeArgs[1]));
                break;
            case "CDIR":
                sendToClient(cdir(modeArgs[1]));
                break;
            case "KILL":
                sendToClient(kill(modeArgs[1]));
                break;
            case "NAME":
                sendToClient(name(modeArgs[1]));
                break;
            case "TOBE":
                sendToClient(tobe(modeArgs[1]));
                break;
            case "SEND":
                sendToClient(send());
                break;
            case "RETR":
                sendToClient(retr(modeArgs[1]));
                break;
            case "STOp":
                fileToRetr = null;
                retrV = false;
                sendToClient("+ok, RETR aborted");
                break;
            case "STOR":
                break;
            default:
                sendToClient("-invalid Command");
                break;
        }
    }
    

    public String user(String userInput) throws Exception {
        Boolean userExists = false;
        String[] lineAccounts = null;
        String linePassword = null;

        File authFile = new File("src/Server/authFile.txt");

        BufferedReader reader = new BufferedReader(new FileReader(authFile));
        String line = reader.readLine();
        while (line != null)
        {
            String[] userInfo = line.split(" ");
            String lineUsername = userInfo[0];
            if(lineUsername.equals(userInput)){
                userExists = true;
                if(userInfo.length == 2){
                    if(!(userInfo[1] == null)){
                        lineAccounts = userInfo[1].split(",");
                    }
                    passwordV = true;
                } else if(userInfo.length == 3){
                    if(!(userInfo[1] == null)){
                        lineAccounts = userInfo[1].split(",");
                    }
                    linePassword = userInfo[2];
                }
                if(userInfo.length == 1){
                    accountV = true;
                    passwordV = true;
                } else if(userInfo.length == 2){
                    accounts = lineAccounts;
                    password = null;
                    passwordV = true;
                } else if(userInfo.length == 3){
                    assert userInfo[1] != null;
                    if(userInfo[1].isEmpty()){
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
        if(!userExists){
            return "Invalid Username";
        }
        userV = true;
        user = userInput;
        if(passwordV && accountV){
            return "!" + userInput + " logged in";
        } else {
            if(accountV){
                return  "+User-id valid, send password";
            } else if(passwordV){
                return  "+User-id valid, send account";
            } else {
                return  "+User-id valid, send account and password";
            }
        }
    }

    public String acct(String userInput) {
        if(userV){
            for (String accountTest : accounts) {
                if(userInput.equals(accountTest)){
                    account = userInput;
                    accountV = true;
                    break;
                }
            }
        }
        if(passwordV && accountV){
            if(cDirA){
                String dir = changeDir();
                cDirA = false;
                return dir;
            }
            return "! Account valid, logged-in";
        } else if (accountV){
            return "+Account valid, send password";
        } else {
            return "-Invalid account, try again";
        }
    }

    public String pass(String userInput) {
        if(userV){
            if(password == null){
                passwordV = true;
            }
            if(userInput.equals(password)){
                passwordV = true;
            }
            if(accountV && passwordV){
                if(cDirP){
                    String dir = changeDir();
                    cDirA = false;
                    return dir;
                }
                return "! logged in";
            } else if(passwordV){
                return "+ Password ok, send account";
            }
        }
        return "-Wrong password, try again";
    }

    public void type(String userInput) throws Exception {
        switch (userInput){
            case "A":
                sendType = "A";
                sendToClient("+Using Ascii");
                break;
            case "B":
                sendType = "B";
                sendToClient("+Using Binary");
                break;
            case "C":
                sendType = "C";
                sendToClient("+Using Continuous");
                break;
            default:
                sendToClient("-Type not valid");
        }
    }

    public String list(String userInput) throws Exception {
        if(userV) {
            if ("F".equals(userInput)) {
                StringBuilder response;
                DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(directory));
                response = new StringBuilder("+" + directory + "\r\n");
                for (Path filePath : stream) {
                    response.append(filePath.getFileName()).append("\r\n");
                }
                return response.toString();
            }
            if ("V".equals(userInput)) {
                StringBuilder response;
                DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(directory));
                response = new StringBuilder("+" + directory + "\r\n");
                SimpleDateFormat DateFor = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
                for (Path filePath : stream) {
                    File file = new File(filePath.toString());
                    response.append(String.format("%6s",file.getName())).append(" | ");
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
        String cdDirectory = "src/Server/"+userInput;
        if(userV && passwordV && accountV){
            directoryTemp = cdDirectory;
            return changeDir();
        }

        if(userV){
            cdDirectory = "src/Server/"+userInput;
            File file = new File(cdDirectory);
            if (!file.isDirectory()){
                return "-Can't connect to directory because: " + cdDirectory + " is not a directory";
            }
            directoryTemp = cdDirectory;
            if(!accountV && !passwordV){
                cDirA = true;
                cDirP = true;
                return "+directory ok, send account/password";
            }
            if(!accountV){
                cDirA = true;
                return "+directory ok, send account/password";
            }
            if(!passwordV){
                cDirP = true;
                return "+directory ok, send account/password";
            }
            return"+directory ok, send account/password";
        }
        return "-Can't connect to directory because: unauthorised, please sign in";
    }

    private String changeDir() {
        File file = new File(directoryTemp);
        if (!file.isDirectory()){
            return "-Can't connect to directory because: " + directoryTemp + " is not a directory";
        }
        directory = directoryTemp;
        directoryTemp = null;
        return "!Changed working dir to " + directory;
    }

    public String kill(String userInput) throws Exception{
        if(userV){
            Path pathToFile = new File(directory+ "/" + userInput).toPath();
            try {
                Files.delete(pathToFile);
                return "+"+userInput+" deleted";
            } catch (NoSuchFileException e){
                return "-Not deleted because file does not exist";
            }
        }
        else return "-Not deleted because: unauthorised, please sign in";
    }

    public String name(String userInput) {
        if(userV){
            Path pathToOldFileTemp = new File(directory+ "/" + userInput).toPath();
            if(Files.exists(pathToOldFileTemp)){
                fileExists = true;
                existingFile = userInput;
                this.pathToOldfile = pathToOldFileTemp;
                return "+File exists";
            }
            return "-Can't find "+ userInput;
        }
        else return "-Not found because: unauthorised, please sign in";
    }

    public String tobe(String userInput) {
        if(userV && fileExists){
            Path newFileName = new File(directory+ "/" + userInput).toPath();
            if(Files.exists(newFileName)){
                return "-File wasn't renamed because file already exists";
            }
            File oldFile = new File(String.valueOf(pathToOldfile));
            oldFile.renameTo(new File(directory+ "/" + userInput));
            fileExists = false;
            String response = "+"+existingFile+" renamed to " + userInput;
            this.existingFile = null;
            this.pathToOldfile = null;
            return response;
        }
        if(!fileExists){
            return "-File wasn't renamed because file does not exist";
        }
        else return "-Not found because: unauthorised, please sign in";

    }

    public String retr(String userInput) {
        if(userV){
            Path pathToFileTemp = new File(directory+ "/" + userInput).toPath();
            if(Files.exists(pathToFileTemp)){
                long fileSize = new File(String.valueOf(pathToFileTemp)).length();
                fileToRetr = userInput;
                retrV = true;
                return "Size of file to send:"+ fileSize;
            }
            return "-Can't find "+ userInput;
        }
        else return "-Not found because: unauthorised, please sign in";
    }

    public String send() throws Exception{
        if(userV && retrV){
            Path pathToFile = new File(directory+ "/" + fileToRetr).toPath();
            File file = new File(String.valueOf(pathToFile));
            byte[] bytes = new byte[(int) file.length()];
            fileToRetr = null;
            try (BufferedInputStream bufferedStream = new BufferedInputStream(new FileInputStream(file))) {
                outToClient.flush();
                int p;
                while ((p = bufferedStream.read(bytes)) >= 0) {
                    outToClient.write(bytes, 0, p);
                }
                bufferedStream.close();
                outToClient.flush();
            } catch (IOException e){
                socket.close();
                running = false;
            }
        }
        if(userV){
            return "No file selected";
        }
        else return "-Not found because: unauthorised, please sign in";
    }

    private void sendToClient(String message) throws IOException {
        outToClient.writeBytes(message + "\0");
    }

    public void stor(String[] userInput) throws Exception{

        File file = new File(directory + userInput[3]);
        switch (userInput[1]){
            case "NEW":
                if (file.isFile()){
                    storMode = "NEW";
                    sendToClient("+File exists, will create new generation of file");
                } else {
                    storMode = "NEWC";
                    sendToClient("+File does not exist, will create new file");
                }
                break;
            case "OLD":
                if (file.isFile()){
                    storMode = "OLD";
                    sendToClient("+Will write over old file");
                } else {
                    storMode = "NEWC";
                    sendToClient("+Will create new file");
                }
                break;
            case "APP":
                if (file.isFile()){
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
        String[] resp = inFromClient.readLine().split(" ");


        A: while (true) {
            if (null == resp[0]) {
                sendToClient("-Invalid Client Response. Awaiting SIZE <number-of-bytes-in-file>. Send STOP to stop transfer.");
            } else {
                switch (resp[0]) {
                    case "SIZE":
                        try {
                            fileSize = Integer.parseInt(resp[1]);
                            if (SFTPServer.DEBUG) System.out.println("File: " + fileSize + "/Directory: " + dir.getFreeSpace());
                            if (dir.getFreeSpace() > fileSize) {
                                sendToClient("+ok, waiting for file");
                                break A;
                            } else {
                                sendToClient("-Not enough room, don't send it");
                                return;
                            }
                        }catch (NumberFormatException e){
                            sendToClient("-Invalid SIZE Argument. Could not convert " + resp[1] + " to a number.");
                        }
                    case "STOP":
                        storMode = "";
                        sendToClient("-Stopping transfer");
                        return;
                    default:
                        sendToClient("-Invalid Client Response. Awaiting SIZE <number-of-bytes-in-file>. Send STOP to stop transfer.");
                        break;
                }
            }
        }
        //find a different filename to save file as
        if ("NEW".equals(storMode)){
            while (file.isFile()) {
                String[] filename = filepath.split("\\.");
                SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
                filename[0] = filename[0] + "-" + DATE_FORMAT.format(new Date());
                filepath = filename[0] + "." + filename[1];
                file = new File(root.toString() + directory + filename[0] + "." + filename[1]);
            };
        }
        receiveFile(fileSize);
    }
}
